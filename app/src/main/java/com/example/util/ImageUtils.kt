package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import kotlin.math.max

object ImageUtils {

  /**
   * Reads a local image file, safely downsamples it if necessary so max(width, height) <= maxDimension,
   * compresses it as JPEG with the specified quality (default 82), and returns a Base64 Data URI:
   * "data:image/jpeg;base64,<BASE64_DATA>"
   * Throws FileNotFoundException or IllegalArgumentException if the file is missing, empty, or unreadable.
   * Avoids loading unnecessarily large bitmaps into memory and never substitutes a placeholder image.
   */
  fun encodeFileToDataUri(
    file: File,
    maxDimension: Int = 1280,
    quality: Int = 82
  ): String {
    if (!file.exists() || !file.isFile) {
      throw FileNotFoundException("Image file does not exist: ${file.absolutePath}")
    }
    if (file.length() == 0L) {
      throw IllegalArgumentException("Image file is empty: ${file.absolutePath}")
    }

    val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.absolutePath, boundsOptions)

    val rawWidth = boundsOptions.outWidth
    val rawHeight = boundsOptions.outHeight
    if (rawWidth <= 0 || rawHeight <= 0) {
      throw IllegalArgumentException("Failed to decode image bounds from: ${file.absolutePath}")
    }

    var sampleSize = 1
    val maxSide = max(rawWidth, rawHeight)
    while (maxSide / (sampleSize * 2) >= maxDimension) {
      sampleSize *= 2
    }

    val decodeOptions = BitmapFactory.Options().apply {
      inSampleSize = sampleSize
    }

    val decodedBitmap = BitmapFactory.decodeFile(file.absolutePath, decodeOptions)
      ?: throw IllegalArgumentException("Failed to decode bitmap from: ${file.absolutePath}")

    // Downscale precisely if still larger than maxDimension
    val scaledBitmap = if (decodedBitmap.width > maxDimension || decodedBitmap.height > maxDimension) {
      val ratio = maxDimension.toFloat() / max(decodedBitmap.width, decodedBitmap.height)
      val targetWidth = (decodedBitmap.width * ratio).toInt().coerceAtLeast(1)
      val targetHeight = (decodedBitmap.height * ratio).toInt().coerceAtLeast(1)
      val scaled = Bitmap.createScaledBitmap(decodedBitmap, targetWidth, targetHeight, true)
      if (scaled != decodedBitmap) {
        decodedBitmap.recycle()
      }
      scaled
    } else {
      decodedBitmap
    }

    val outputStream = ByteArrayOutputStream()
    val success = scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality.coerceIn(1, 100), outputStream)
    scaledBitmap.recycle()

    if (!success) {
      throw IllegalStateException("Failed to compress image to JPEG format")
    }

    val bytes = outputStream.toByteArray()
    val base64 = try {
      Base64.encodeToString(bytes, Base64.NO_WRAP)
    } catch (e: Throwable) {
      java.util.Base64.getEncoder().encodeToString(bytes)
    }

    return "data:image/jpeg;base64,$base64"
  }

  /**
   * Reads an image Uri, resizes it down if larger than maxDimension to keep requests fast and reliable,
   * compresses to JPEG, and converts to Base64 string for Gemini API multimodal payload.
   */
  suspend fun uriToBase64(
    context: Context,
    uri: Uri,
    maxDimension: Int = 1024
  ): String? = withContext(Dispatchers.IO) {
    try {
      // Read all bytes once to prevent issues with non-seekable streams or temporary URIs
      val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        ?: return@withContext null
      if (bytes.isEmpty()) return@withContext null

      val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
      BitmapFactory.decodeByteArray(bytes, 0, bytes.size, boundsOptions)

      val rawWidth = boundsOptions.outWidth
      val rawHeight = boundsOptions.outHeight
      if (rawWidth <= 0 || rawHeight <= 0) return@withContext null

      var sampleSize = 1
      val maxSide = max(rawWidth, rawHeight)
      while (maxSide / sampleSize > maxDimension * 2) {
        sampleSize *= 2
      }

      val decodeOptions = BitmapFactory.Options().apply {
        inSampleSize = sampleSize
      }

      val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions)
        ?: return@withContext null

      // Scale down precisely if needed
      val scaledBitmap = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
        val ratio = maxDimension.toFloat() / max(bitmap.width, bitmap.height)
        val targetWidth = (bitmap.width * ratio).toInt().coerceAtLeast(1)
        val targetHeight = (bitmap.height * ratio).toInt().coerceAtLeast(1)
        Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
      } else {
        bitmap
      }

      val outputStream = ByteArrayOutputStream()
      scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
      val byteArray = outputStream.toByteArray()
      Base64.encodeToString(byteArray, Base64.NO_WRAP)
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }
}
