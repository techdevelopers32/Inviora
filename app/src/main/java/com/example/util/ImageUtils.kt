package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.math.max

object ImageUtils {

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
