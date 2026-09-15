package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.DesignTemplateEntity
import com.example.data.model.EventEntity
import com.example.data.model.EventPage
import com.example.data.model.GuestEntity
import com.example.data.model.SubEvent
import com.example.data.model.TextLayerConfig
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.IvoryBg
import com.example.ui.theme.IvoryBorder
import com.example.ui.theme.IvoryBorderStrong
import com.example.ui.theme.IvorySurface
import com.example.ui.theme.IvorySurfaceLight
import java.io.File

/**
 * MultiPageInvitationView:
 * Handles sequencing across multiple pages of an event for both Master Preview
 * and personalized Guest Preview.
 * The uploaded invitation design is the primary visual priority with zero fading or heavy overlays.
 */
@Composable
fun MultiPageInvitationView(
  pages: List<EventPage>,
  designsMap: Map<String, DesignTemplateEntity>,
  event: EventEntity? = null,
  guest: GuestEntity? = null,
  guestName: String? = null,
  modifier: Modifier = Modifier
) {
  if (pages.isEmpty()) {
    Box(
      modifier = modifier
        .fillMaxSize()
        .background(IvoryBg),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "No invitation pages created for this event.",
        fontFamily = FontFamily.Serif,
        color = Color.Gray,
        fontSize = 15.sp
      )
    }
    return
  }

  var currentPageIndex by remember { mutableIntStateOf(0) }
  val activeIndex = currentPageIndex.coerceIn(0, pages.size - 1)
  val currentPage = pages[activeIndex]
  val currentDesign = designsMap[currentPage.designId] ?: designsMap[event?.designId] ?: designsMap.values.firstOrNull()

  Column(
    modifier = modifier
      .fillMaxSize()
      .testTag("multi_page_invitation_view"),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Top Page Selector Chips if multiple pages
    if (pages.size > 1) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        pages.forEachIndexed { idx, p ->
          val isSelected = (idx == activeIndex)
          val chipLabel = p.pageName.ifBlank { "Page ${idx + 1}" }

          Box(
            modifier = Modifier
              .padding(horizontal = 4.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(
                if (isSelected) GoldPrimary else IvorySurfaceLight
              )
              .border(
                1.dp,
                if (isSelected) GoldDark else IvoryBorderStrong,
                RoundedCornerShape(16.dp)
              )
              .clickable { currentPageIndex = idx }
              .padding(horizontal = 12.dp, vertical = 6.dp)
              .testTag("page_chip_$idx")
          ) {
            Text(
              text = chipLabel,
              fontSize = 12.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              color = if (isSelected) Color.White else Color(0xFF4A443D)
            )
          }
        }
      }
    }

    // Main Invitation Card Viewport with scrolling support
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 16.dp, vertical = 10.dp),
      contentAlignment = Alignment.TopCenter
    ) {
      InvitationPageCard(
        page = currentPage,
        design = currentDesign,
        event = event,
        guest = guest,
        guestName = guestName ?: guest?.name,
        modifier = Modifier.fillMaxWidth(0.96f)
      )
    }

    // Bottom Navigation Bar for paging
    if (pages.size > 1) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(IvorySurface)
          .border(1.dp, IvoryBorder)
          .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedButton(
          onClick = { if (currentPageIndex > 0) currentPageIndex-- },
          enabled = currentPageIndex > 0,
          border = ButtonDefaults.outlinedButtonBorder,
          colors = ButtonDefaults.outlinedButtonColors(
            contentColor = GoldDark
          ),
          modifier = Modifier.testTag("prev_page_button")
        ) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous")
          Spacer(modifier = Modifier.width(4.dp))
          Text("Prev")
        }

        Text(
          text = "${activeIndex + 1} of ${pages.size}",
          fontSize = 13.sp,
          fontWeight = FontWeight.Medium,
          fontFamily = FontFamily.Serif,
          color = Color(0xFF5C544C)
        )

        ElevatedButton(
          onClick = { if (currentPageIndex < pages.size - 1) currentPageIndex++ },
          enabled = currentPageIndex < pages.size - 1,
          colors = ButtonDefaults.elevatedButtonColors(
            containerColor = GoldPrimary,
            contentColor = Color.White
          ),
          modifier = Modifier.testTag("next_page_button")
        ) {
          Text("Next")
          Spacer(modifier = Modifier.width(4.dp))
          Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
        }
      }
    }
  }
}

/**
 * InvitationPageCard:
 * Renders the single page of an invitation.
 *
 * CRITICAL REQUIREMENTS:
 * 1. The uploaded design is the primary background with full visual fidelity (NO opaque or heavy fading overlay).
 * 2. Arabic Bismillah appears first, English translation directly underneath.
 * 3. Groom & Bride names appear on EVERY wedding page.
 * 4. Each text element uses normalized proportional positioning (xPercent, yPercent).
 * 5. Text readability is achieved with localized text shadow and crisp typography.
 */
@Composable
fun InvitationPageCard(
  page: EventPage,
  design: DesignTemplateEntity?,
  event: EventEntity? = null,
  guest: GuestEntity? = null,
  guestName: String? = null,
  isEditable: Boolean = false,
  onlyGuestNameEditable: Boolean = false,
  selectedLayerId: String? = null,
  onSelectLayer: ((String) -> Unit)? = null,
  onUpdateLayer: ((TextLayerConfig) -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val hasUploadedImage = design != null && (design.imagePath.isNotBlank() || design.referenceDrawable.isNotBlank())
  val resolvedLayers = remember(page, event, guest, guestName) {
    TextLayerConfig.buildResolvedLayers(event, page, guest, guestName)
  }

  Card(
    modifier = modifier
      .shadow(elevation = 10.dp, shape = RoundedCornerShape(14.dp))
      .testTag("invitation_page_card_${page.id}"),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (hasUploadedImage) Color.Transparent else IvorySurface
    )
  ) {
    BoxWithConstraints(
      modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(0.68f) // Standard portrait luxury card aspect ratio (~5x7 inch)
        .border(1.dp, IvoryBorderStrong, RoundedCornerShape(14.dp))
    ) {
      val density = LocalDensity.current
      val cardWidthDp = maxWidth
      val cardHeightDp = maxHeight
      val cardWidthPx = with(density) { cardWidthDp.toPx() }
      val cardHeightPx = with(density) { cardHeightDp.toPx() }

      // 1. PRIMARY VISUAL BACKGROUND: Uploaded design artwork
      if (hasUploadedImage) {
        val imgFile = File(design!!.imagePath)
        val drawableResId = remember(design.referenceDrawable) {
          if (design.referenceDrawable.isNotBlank()) {
            context.resources.getIdentifier(design.referenceDrawable, "drawable", context.packageName)
          } else 0
        }
        val model: Any = when {
          imgFile.exists() -> imgFile
          design.imagePath.isNotBlank() -> design.imagePath
          drawableResId != 0 -> drawableResId
          else -> design.imagePath
        }
        AsyncImage(
          model = model,
          contentDescription = design.name,
          contentScale = ContentScale.Crop,
          modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(14.dp))
        )
      } else {
        // Fallback for blank template: Elegant Ivory texture with decorative border
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(IvorySurface)
            .padding(10.dp)
            .border(1.dp, GoldDark.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
        )
      }

      // 2. TEXT LAYERS: Positioned directly on top of the uploaded artwork
      Box(
        modifier = Modifier.fillMaxSize()
      ) {
        resolvedLayers.forEach { layer ->
          if (layer.isVisible && layer.text.isNotBlank()) {
            val isGuestEditableLayer = (layer.id == "guest_name" || layer.id == "guest_note")
            val canEdit = if (onlyGuestNameEditable) isGuestEditableLayer else isEditable
            RenderProportionalTextLayer(
              layer = layer,
              cardWidthPx = cardWidthPx,
              cardHeightPx = cardHeightPx,
              cardWidthDp = cardWidthDp,
              isEditable = canEdit,
              isSelected = (selectedLayerId == layer.id),
              onSelect = { onSelectLayer?.invoke(layer.id) },
              onDragDelta = { dxNorm, dyNorm ->
                onUpdateLayer?.invoke(
                  layer.copy(
                    xPercent = (layer.xPercent + dxNorm).coerceIn(0.08f, 0.92f),
                    yPercent = (layer.yPercent + dyNorm).coerceIn(0.03f, 0.97f)
                  )
                )
              }
            )
          }
        }
      }
    }
  }
}

/**
 * RenderProportionalTextLayer:
 * Renders a single text element at its normalized (xPercent, yPercent) position
 * with high-contrast text shadowing so the artwork remains 100% visible and the text remains readable.
 */
@Composable
private fun RenderProportionalTextLayer(
  layer: TextLayerConfig,
  cardWidthPx: Float,
  cardHeightPx: Float,
  cardWidthDp: androidx.compose.ui.unit.Dp,
  isEditable: Boolean,
  isSelected: Boolean,
  onSelect: () -> Unit,
  onDragDelta: (Float, Float) -> Unit
) {
  var elemWidthPx by remember { mutableIntStateOf(0) }
  var elemHeightPx by remember { mutableIntStateOf(0) }

  val targetXPx = (cardWidthPx * layer.xPercent) - (elemWidthPx / 2f)
  val targetYPx = (cardHeightPx * layer.yPercent) - (elemHeightPx / 2f)

  val parsedColor = parseHexColor(layer.colorHex)
  val isLightColor = isColorLight(parsedColor)
  // High-contrast shadow gives crisp legibility over any background without covering the artwork
  val textShadow = Shadow(
    color = if (isLightColor) Color.Black.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.85f),
    blurRadius = 8f,
    offset = Offset(1f, 1f)
  )

  Box(
    modifier = Modifier
      .offset {
        IntOffset(targetXPx.toInt().coerceAtLeast(0), targetYPx.toInt().coerceAtLeast(0))
      }
      .onSizeChanged {
        elemWidthPx = it.width
        elemHeightPx = it.height
      }
      .then(
        if (isEditable) {
          Modifier
            .pointerInput(layer.id) {
              detectDragGestures { change, dragAmount ->
                change.consume()
                val dxNorm = dragAmount.x / cardWidthPx
                val dyNorm = dragAmount.y / cardHeightPx
                onDragDelta(dxNorm, dyNorm)
              }
            }
            .clickable { onSelect() }
        } else Modifier
      )
      .then(
        if (isEditable && isSelected) {
          Modifier
            .border(1.5.dp, GoldPrimary, RoundedCornerShape(6.dp))
            .background(GoldPrimary.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp)
        } else if (isEditable) {
          Modifier
            .border(0.5.dp, Color.Gray.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp, vertical = 2.dp)
        } else {
          Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        }
      )
  ) {
    Text(
      text = layer.text,
      fontFamily = when {
        layer.fontFamily.equals("Serif", ignoreCase = true) -> FontFamily.Serif
        layer.fontFamily.equals("Cursive", ignoreCase = true) -> FontFamily.Cursive
        layer.fontFamily.equals("Monospace", ignoreCase = true) -> FontFamily.Monospace
        layer.fontFamily.equals("SansSerif", ignoreCase = true) -> FontFamily.SansSerif
        layer.id == "bismillah_arabic" || layer.id == "couple_names" || layer.id == "bismillah_english" || layer.id == "greeting" -> FontFamily.Serif
        else -> FontFamily.SansSerif
      },
      fontSize = layer.fontSizeSp.sp,
      fontWeight = when (layer.fontWeight) {
        "Bold" -> FontWeight.Bold
        "SemiBold" -> FontWeight.SemiBold
        "Medium" -> FontWeight.Medium
        else -> FontWeight.Normal
      },
      fontStyle = if (layer.fontStyle == "Italic") FontStyle.Italic else FontStyle.Normal,
      color = parsedColor,
      textAlign = when (layer.alignment) {
        "Left", "Start" -> TextAlign.Start
        "Right", "End" -> TextAlign.End
        else -> TextAlign.Center
      },
      style = TextStyle(shadow = textShadow),
      modifier = Modifier.widthIn(max = (cardWidthDp * 0.88f))
    )
  }
}

/**
 * TextLayerEditorToolbar:
 * A rich, visual, tactile toolbar enabling users to move, resize, align, and customize
 * text elements directly on top of the uploaded invitation design.
 */
@Composable
fun TextLayerEditorToolbar(
  selectedLayer: TextLayerConfig,
  allLayers: List<TextLayerConfig>,
  onSelectLayer: (String) -> Unit,
  onUpdateLayer: (TextLayerConfig) -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = IvorySurface),
    shape = RoundedCornerShape(12.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, IvoryBorderStrong)
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      // 1. Selector Row for all layers
      Text(
        text = "ACTIVE TEXT ELEMENT",
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
        color = GoldDark
      )
      Spacer(modifier = Modifier.height(6.dp))

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState())
          .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        allLayers.forEach { layer ->
          val isSel = (layer.id == selectedLayer.id)
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(if (isSel) GoldPrimary else IvorySurfaceLight)
              .border(1.dp, if (isSel) GoldDark else IvoryBorder, RoundedCornerShape(6.dp))
              .clickable { onSelectLayer(layer.id) }
              .padding(horizontal = 8.dp, vertical = 5.dp)
          ) {
            Text(
              text = layer.label,
              fontSize = 10.sp,
              fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
              color = if (isSel) Color.White else Color(0xFF4A443D)
            )
          }
        }
      }

      HorizontalDivider(color = IvoryBorder, thickness = 0.8.dp)
      Spacer(modifier = Modifier.height(8.dp))

      // 2. Formatting & Position controls
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Font Size Controls [- 14sp +]
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text("Size:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4A443D))
          Spacer(modifier = Modifier.width(6.dp))
          OutlinedButton(
            onClick = {
              if (selectedLayer.fontSizeSp > 8) {
                onUpdateLayer(selectedLayer.copy(fontSizeSp = selectedLayer.fontSizeSp - 1))
              }
            },
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            modifier = Modifier.size(width = 34.dp, height = 30.dp)
          ) {
            Text("–", fontSize = 14.sp)
          }
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "${selectedLayer.fontSizeSp}sp",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = GoldDark,
            modifier = Modifier.width(36.dp),
            textAlign = TextAlign.Center
          )
          Spacer(modifier = Modifier.width(4.dp))
          OutlinedButton(
            onClick = {
              if (selectedLayer.fontSizeSp < 36) {
                onUpdateLayer(selectedLayer.copy(fontSizeSp = selectedLayer.fontSizeSp + 1))
              }
            },
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            modifier = Modifier.size(width = 34.dp, height = 30.dp)
          ) {
            Text("+", fontSize = 14.sp)
          }
        }

        // Color Presets [Dark / Gold / White]
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          ColorSwatch(
            colorHex = "#1E1A16",
            isSelected = selectedLayer.colorHex.equals("#1E1A16", ignoreCase = true),
            onSelect = { onUpdateLayer(selectedLayer.copy(colorHex = "#1E1A16")) }
          )
          ColorSwatch(
            colorHex = "#8C6D23",
            isSelected = selectedLayer.colorHex.equals("#8C6D23", ignoreCase = true),
            onSelect = { onUpdateLayer(selectedLayer.copy(colorHex = "#8C6D23")) }
          )
          ColorSwatch(
            colorHex = "#FFFFFF",
            isSelected = selectedLayer.colorHex.equals("#FFFFFF", ignoreCase = true),
            onSelect = { onUpdateLayer(selectedLayer.copy(colorHex = "#FFFFFF")) }
          )
        }

        // Visibility Toggle
        IconButton(
          onClick = { onUpdateLayer(selectedLayer.copy(isVisible = !selectedLayer.isVisible)) },
          modifier = Modifier.size(32.dp)
        ) {
          Icon(
            imageVector = if (selectedLayer.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
            contentDescription = "Toggle visibility",
            tint = if (selectedLayer.isVisible) GoldDark else Color.Gray,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // 3. Directional Nudge buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Position Nudge:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4A443D))

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          NudgeButton(label = "↑") {
            onUpdateLayer(selectedLayer.copy(yPercent = (selectedLayer.yPercent - 0.03f).coerceAtLeast(0.04f)))
          }
          NudgeButton(label = "↓") {
            onUpdateLayer(selectedLayer.copy(yPercent = (selectedLayer.yPercent + 0.03f).coerceAtMost(0.96f)))
          }
          NudgeButton(label = "←") {
            onUpdateLayer(selectedLayer.copy(xPercent = (selectedLayer.xPercent - 0.04f).coerceAtLeast(0.08f)))
          }
          NudgeButton(label = "→") {
            onUpdateLayer(selectedLayer.copy(xPercent = (selectedLayer.xPercent + 0.04f).coerceAtMost(0.92f)))
          }
          NudgeButton(label = "Center") {
            onUpdateLayer(selectedLayer.copy(xPercent = 0.5f))
          }
        }
      }
    }
  }
}

@Composable
private fun ColorSwatch(
  colorHex: String,
  isSelected: Boolean,
  onSelect: () -> Unit
) {
  val parsed = parseHexColor(colorHex)
  Box(
    modifier = Modifier
      .size(24.dp)
      .clip(CircleShape)
      .background(parsed)
      .border(
        width = if (isSelected) 2.dp else 1.dp,
        color = if (isSelected) GoldPrimary else Color.Gray.copy(alpha = 0.5f),
        shape = CircleShape
      )
      .clickable { onSelect() },
    contentAlignment = Alignment.Center
  ) {
    if (isSelected) {
      Icon(
        Icons.Default.Check,
        contentDescription = null,
        tint = if (isColorLight(parsed)) Color.Black else Color.White,
        modifier = Modifier.size(14.dp)
      )
    }
  }
}

@Composable
private fun NudgeButton(label: String, onClick: () -> Unit) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(4.dp))
      .background(IvorySurfaceLight)
      .border(1.dp, IvoryBorderStrong, RoundedCornerShape(4.dp))
      .clickable { onClick() }
      .padding(horizontal = 8.dp, vertical = 4.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      color = Color(0xFF2C2621)
    )
  }
}

private fun parseHexColor(hex: String): Color {
  return try {
    val clean = hex.removePrefix("#")
    val intVal = clean.toLong(16)
    if (clean.length == 6) {
      Color((0xFF000000 or intVal).toInt())
    } else {
      Color(intVal.toInt())
    }
  } catch (_: Exception) {
    Color(0xFF1E1A16)
  }
}

private fun isColorLight(color: Color): Boolean {
  val luminance = (0.299 * color.red + 0.587 * color.green + 0.114 * color.blue)
  return luminance > 0.55
}

/**
 * Backward-compatible helper for legacy screens calling InvitationCardRenderer directly
 */
@Composable
fun InvitationCardRenderer(
  event: EventEntity,
  design: DesignTemplateEntity?,
  guestName: String? = null,
  filteredSubEvents: List<SubEvent>? = null,
  modifier: Modifier = Modifier
) {
  val pages = event.getPages()
  val designsMap = if (design != null) mapOf(design.id to design) else emptyMap()

  MultiPageInvitationView(
    pages = pages,
    designsMap = designsMap,
    event = event,
    guestName = guestName,
    modifier = modifier
  )
}
