package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.DesignTemplateEntity
import com.example.ui.theme.CharcoalMuted
import com.example.ui.theme.CharcoalPrimary
import com.example.ui.theme.CharcoalSecondary
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.IvoryBg
import com.example.ui.theme.IvoryBorder
import com.example.ui.theme.IvoryBorderStrong
import com.example.ui.theme.IvorySurface
import com.example.ui.theme.IvorySurfaceLight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

@Composable
fun CreateDesignScreen(
  onSaveDesign: (DesignTemplateEntity) -> Unit,
  onCancel: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  var designName by remember { mutableStateOf("") }
  var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
  var savedImagePath by remember { mutableStateOf<String?>(null) }
  var isFavorite by remember { mutableStateOf(false) }
  var isSaving by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  // Android Photo Picker
  val photoPicker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    if (uri != null) {
      selectedImageUri = uri
      errorMessage = null
      scope.launch(Dispatchers.IO) {
        try {
          val path = copyUriToInternalStorage(context, uri)
          withContext(Dispatchers.Main) {
            savedImagePath = path
          }
        } catch (e: Exception) {
          withContext(Dispatchers.Main) {
            errorMessage = "Could not load image: ${e.message}"
          }
        }
      }
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(IvoryBg)
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 20.dp, vertical = 16.dp)
      .testTag("create_design_screen")
  ) {
    // Header Bar
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
          onClick = onCancel,
          modifier = Modifier.testTag("design_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = CharcoalPrimary
          )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Column {
          Text(
            text = "Upload Design",
            fontFamily = FontFamily.Serif,
            fontSize = 20.sp,
            color = CharcoalPrimary
          )
          Text(
            text = "Add clean artwork to reusable library",
            fontSize = 11.sp,
            color = CharcoalMuted
          )
        }
      }

      ElevatedButton(
        onClick = {
          if (designName.isBlank()) {
            errorMessage = "Please enter a name for your design (e.g. 'Mehndi Gold')."
            return@ElevatedButton
          }
          if (savedImagePath.isNullOrBlank()) {
            errorMessage = "Please upload an invitation artwork image."
            return@ElevatedButton
          }

          isSaving = true
          val designId = "design_" + UUID.randomUUID().toString().take(8)
          val entity = DesignTemplateEntity(
            id = designId,
            name = designName.trim(),
            imagePath = savedImagePath ?: "",
            referenceDrawable = "",
            themeStyle = "CUSTOM_UPLOAD",
            accentColorHex = "#D4AF37",
            fontStyle = "Serif Calligraphic",
            ornamentStyle = "Custom Artwork",
            prompt = "",
            isFavorite = isFavorite,
            createdAt = System.currentTimeMillis()
          )
          onSaveDesign(entity)
        },
        enabled = !isSaving && (savedImagePath != null),
        colors = ButtonDefaults.elevatedButtonColors(
          containerColor = GoldPrimary,
          contentColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.testTag("save_design_to_library_button")
      ) {
        Text("Save to Library", fontFamily = FontFamily.Serif, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Information Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = IvorySurfaceLight),
      border = androidx.compose.foundation.BorderStroke(0.5.dp, IvoryBorderStrong),
      shape = RoundedCornerShape(8.dp)
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Text(
          text = "CLEAN DESIGN WORKFLOW",
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.5.sp,
          color = GoldDark
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Prepare your invitation artwork externally with all fixed names and dates removed. Upload the clean artwork here; Inviora will overlay your event details and greetings over this design.",
          fontSize = 12.sp,
          lineHeight = 17.sp,
          color = CharcoalSecondary
        )
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Design Name Input
    Text(
      text = "Design Name",
      fontSize = 12.sp,
      fontWeight = FontWeight.SemiBold,
      color = CharcoalPrimary
    )
    Spacer(modifier = Modifier.height(6.dp))
    OutlinedTextField(
      value = designName,
      onValueChange = {
        designName = it
        errorMessage = null
      },
      placeholder = { Text("e.g. Mehndi Gold, Baraat Royal, Walima Elegant", fontSize = 13.sp, color = CharcoalMuted) },
      modifier = Modifier
        .fillMaxWidth()
        .testTag("design_name_input"),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = GoldPrimary,
        unfocusedBorderColor = IvoryBorderStrong,
        focusedContainerColor = IvorySurface,
        unfocusedContainerColor = IvorySurface
      ),
      shape = RoundedCornerShape(8.dp),
      singleLine = true
    )

    Spacer(modifier = Modifier.height(18.dp))

    // Quick Name Suggestions Chips
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      listOf("Mehndi Gold", "Baraat Royal", "Walima Elegant", "Floral Atelier").forEach { suggestion ->
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(IvorySurface)
            .border(0.5.dp, IvoryBorder, RoundedCornerShape(12.dp))
            .clickable { designName = suggestion }
            .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
          Text(suggestion, fontSize = 10.sp, color = CharcoalSecondary)
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Artwork Upload Area
    Text(
      text = "Artwork Image",
      fontSize = 12.sp,
      fontWeight = FontWeight.SemiBold,
      color = CharcoalPrimary
    )
    Spacer(modifier = Modifier.height(8.dp))

    if (savedImagePath != null) {
      // Preview of uploaded image
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .border(1.dp, GoldPrimary, RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = IvorySurface)
      ) {
        Column(
          modifier = Modifier.padding(12.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          AsyncImage(
            model = File(savedImagePath!!),
            contentDescription = "Uploaded artwork",
            contentScale = ContentScale.Fit,
            modifier = Modifier
              .fillMaxWidth()
              .height(300.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(Color.Black.copy(alpha = 0.05f))
          )

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GoldDark, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Artwork ready", fontSize = 12.sp, color = CharcoalPrimary, fontWeight = FontWeight.Medium)
            }

            ElevatedButton(
              onClick = {
                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
              },
              colors = ButtonDefaults.elevatedButtonColors(
                containerColor = IvorySurfaceLight,
                contentColor = CharcoalPrimary
              ),
              shape = RoundedCornerShape(6.dp)
            ) {
              Text("Replace Image", fontSize = 11.sp)
            }
          }
        }
      }
    } else {
      // Empty state / Upload trigger
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(220.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(IvorySurface)
          .border(1.2.dp, GoldDark.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
          .clickable {
            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
          }
          .padding(20.dp)
          .testTag("upload_artwork_picker"),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Box(
            modifier = Modifier
              .size(54.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(IvorySurfaceLight)
              .border(0.5.dp, GoldPrimary, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.AddPhotoAlternate,
              contentDescription = "Select Image",
              tint = GoldDark,
              modifier = Modifier.size(28.dp)
            )
          }

          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "Select Clean Invitation Artwork",
            fontFamily = FontFamily.Serif,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = CharcoalPrimary
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "PNG or JPG from device storage",
            fontSize = 11.sp,
            color = CharcoalMuted
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Favorite Toggle
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(8.dp))
        .background(IvorySurface)
        .border(0.5.dp, IvoryBorder, RoundedCornerShape(8.dp))
        .padding(horizontal = 14.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
          contentDescription = null,
          tint = if (isFavorite) GoldPrimary else CharcoalMuted,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text("Add to Favorites", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = CharcoalPrimary)
          Text("Pin to top of design selector", fontSize = 11.sp, color = CharcoalMuted)
        }
      }

      Switch(
        checked = isFavorite,
        onCheckedChange = { isFavorite = it },
        colors = SwitchDefaults.colors(
          checkedThumbColor = GoldLight,
          checkedTrackColor = GoldDark
        )
      )
    }

    // Error Notice
    if (errorMessage != null) {
      Spacer(modifier = Modifier.height(14.dp))
      Text(
        text = errorMessage!!,
        fontSize = 12.sp,
        color = Color(0xFFB71C1C),
        modifier = Modifier
          .fillMaxWidth()
          .background(Color(0xFFFFEBEE), RoundedCornerShape(6.dp))
          .padding(10.dp)
      )
    }

    Spacer(modifier = Modifier.height(32.dp))
  }
}

private suspend fun copyUriToInternalStorage(context: Context, uri: Uri): String {
  return withContext(Dispatchers.IO) {
    val dir = File(context.filesDir, "designs")
    if (!dir.exists()) dir.mkdirs()
    val filename = "design_${System.currentTimeMillis()}.jpg"
    val destFile = File(dir, filename)

    context.contentResolver.openInputStream(uri)?.use { input ->
      FileOutputStream(destFile).use { output ->
        input.copyTo(output)
      }
    }
    destFile.absolutePath
  }
}
