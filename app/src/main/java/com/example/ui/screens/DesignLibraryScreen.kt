package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.DesignTemplateEntity
import com.example.data.model.EventEntity
import com.example.ui.theme.CharcoalMuted
import com.example.ui.theme.CharcoalPrimary
import com.example.ui.theme.CharcoalSecondary
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.IvoryBg
import com.example.ui.theme.IvoryBorder
import com.example.ui.theme.IvorySurface
import com.example.ui.theme.IvorySurfaceLight
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DesignLibraryScreen(
  designs: List<DesignTemplateEntity>,
  onCreateDesignClick: () -> Unit,
  onPreviewDesign: (DesignTemplateEntity) -> Unit,
  onRenameDesign: (String, String) -> Unit,
  onToggleFavorite: (String, Boolean) -> Unit,
  checkEventsUsingDesign: suspend (String) -> List<EventEntity>,
  onDeleteDesign: (String, Boolean) -> Unit,
  modifier: Modifier = Modifier
) {
  val scope = rememberCoroutineScope()
  var designPendingDelete by remember { mutableStateOf<DesignTemplateEntity?>(null) }
  var designToRename by remember { mutableStateOf<DesignTemplateEntity?>(null) }
  var newNameInput by remember { mutableStateOf("") }
  var dependentEvents by remember { mutableStateOf<List<EventEntity>>(emptyList()) }

  val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(IvoryBg)
      .padding(horizontal = 20.dp, vertical = 16.dp)
      .testTag("design_library_screen")
  ) {
    // Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "CREATIVE STUDIO",
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          letterSpacing = 2.sp,
          color = GoldDark
        )
        Text(
          text = "Design Library",
          fontFamily = FontFamily.Serif,
          fontSize = 20.sp,
          color = CharcoalPrimary
        )
      }

      ElevatedButton(
        onClick = onCreateDesignClick,
        colors = ButtonDefaults.elevatedButtonColors(
          containerColor = GoldPrimary,
          contentColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.testTag("upload_new_design_button")
      ) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Upload Design", fontFamily = FontFamily.Serif, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    if (designs.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(Icons.Default.Palette, contentDescription = null, tint = GoldDark, modifier = Modifier.size(40.dp))
          Spacer(modifier = Modifier.height(12.dp))
          Text("No invitation designs yet", fontFamily = FontFamily.Serif, fontSize = 16.sp, color = CharcoalPrimary)
          Spacer(modifier = Modifier.height(4.dp))
          Text("Upload your prepared clean artwork to reuse across events.", fontSize = 12.sp, color = CharcoalMuted)
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        items(designs, key = { it.id }) { design ->
          val createdDateStr = remember(design.createdAt) {
            dateFormat.format(Date(design.createdAt))
          }

          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .border(0.5.dp, IvoryBorder, RoundedCornerShape(10.dp))
              .testTag("design_card_${design.id}"),
            colors = CardDefaults.cardColors(containerColor = IvorySurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              // Artwork Thumbnail
              Box(
                modifier = Modifier
                  .size(72.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(IvorySurfaceLight)
                  .border(0.5.dp, IvoryBorder, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
              ) {
                if (design.imagePath.isNotBlank()) {
                  val file = File(design.imagePath)
                  AsyncImage(
                    model = if (file.exists()) file else design.imagePath,
                    contentDescription = design.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                  )
                } else {
                  Icon(Icons.Default.Palette, contentDescription = null, tint = GoldDark, modifier = Modifier.size(24.dp))
                }
              }

              Spacer(modifier = Modifier.width(14.dp))

              // Design Details
              Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = design.name,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = CharcoalPrimary,
                    modifier = Modifier.weight(1f)
                  )
                  IconButton(
                    onClick = { onToggleFavorite(design.id, !design.isFavorite) },
                    modifier = Modifier.size(32.dp)
                  ) {
                    Icon(
                      imageVector = if (design.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                      contentDescription = "Favorite",
                      tint = if (design.isFavorite) GoldPrimary else CharcoalMuted,
                      modifier = Modifier.size(18.dp)
                    )
                  }
                }

                Text(
                  text = "ID: ${design.id}",
                  fontSize = 10.sp,
                  color = CharcoalMuted,
                  letterSpacing = 0.5.sp
                )

                Text(
                  text = "Created: $createdDateStr",
                  fontSize = 10.sp,
                  color = CharcoalSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Actions: Preview, Rename, Delete
                Row(
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  ElevatedButton(
                    onClick = { onPreviewDesign(design) },
                    colors = ButtonDefaults.elevatedButtonColors(
                      containerColor = IvorySurfaceLight,
                      contentColor = CharcoalPrimary
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(30.dp)
                  ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, tint = GoldDark, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Preview", fontSize = 10.sp)
                  }

                  ElevatedButton(
                    onClick = {
                      designToRename = design
                      newNameInput = design.name
                    },
                    colors = ButtonDefaults.elevatedButtonColors(
                      containerColor = IvorySurfaceLight,
                      contentColor = CharcoalPrimary
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(30.dp)
                  ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = CharcoalMuted, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Rename", fontSize = 10.sp)
                  }

                  IconButton(
                    onClick = {
                      scope.launch {
                        dependentEvents = checkEventsUsingDesign(design.id)
                        designPendingDelete = design
                      }
                    },
                    modifier = Modifier.size(30.dp)
                  ) {
                    Icon(
                      Icons.Default.DeleteOutline,
                      contentDescription = "Delete",
                      tint = CharcoalMuted,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  // Rename Dialog
  designToRename?.let { dsg ->
    AlertDialog(
      onDismissRequest = { designToRename = null },
      title = { Text("Rename Design", fontFamily = FontFamily.Serif, color = CharcoalPrimary) },
      text = {
        Column {
          Text("Enter new name for this invitation design:", fontSize = 12.sp, color = CharcoalSecondary)
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = newNameInput,
            onValueChange = { newNameInput = it },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = GoldPrimary,
              unfocusedBorderColor = IvoryBorder
            )
          )
        }
      },
      confirmButton = {
        TextButton(
          onClick = {
            if (newNameInput.isNotBlank()) {
              onRenameDesign(dsg.id, newNameInput.trim())
            }
            designToRename = null
          }
        ) {
          Text("Save", color = GoldDark, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { designToRename = null }) {
          Text("Cancel", color = CharcoalMuted)
        }
      },
      containerColor = IvorySurface,
      shape = RoundedCornerShape(12.dp)
    )
  }

  // Delete Confirmation Dialog
  designPendingDelete?.let { dsg ->
    AlertDialog(
      onDismissRequest = { designPendingDelete = null },
      title = { Text("Delete Design", fontFamily = FontFamily.Serif, color = CharcoalPrimary) },
      text = {
        Column {
          Text("Are you sure you want to delete '${dsg.name}'?", fontSize = 13.sp, color = CharcoalPrimary)
          if (dependentEvents.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Warning: This design is currently assigned to ${dependentEvents.size} event(s): ${dependentEvents.joinToString { it.title }}.",
              fontSize = 12.sp,
              color = Color(0xFFB71C1C)
            )
          }
        }
      },
      confirmButton = {
        TextButton(
          onClick = {
            onDeleteDesign(dsg.id, dependentEvents.isNotEmpty())
            designPendingDelete = null
          }
        ) {
          Text("Delete", color = Color(0xFFB71C1C), fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { designPendingDelete = null }) {
          Text("Cancel", color = CharcoalMuted)
        }
      },
      containerColor = IvorySurface,
      shape = RoundedCornerShape(12.dp)
    )
  }
}
