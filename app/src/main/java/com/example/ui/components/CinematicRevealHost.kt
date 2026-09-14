package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CharcoalPrimary
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.IvoryBg
import com.example.ui.theme.IvorySurface
import com.example.ui.theme.VelvetCrimson
import com.example.ui.theme.VelvetCrimsonDeep
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun CinematicRevealHost(
  animationType: String,
  isOpenInitially: Boolean = false,
  cardContent: @Composable () -> Unit
) {
  var isOpen by remember { mutableStateOf(isOpenInitially) }
  val openProgress = remember { Animatable(if (isOpenInitially) 1f else 0f) }
  val scope = rememberCoroutineScope()

  fun triggerOpen() {
    if (!isOpen) {
      isOpen = true
      scope.launch {
        openProgress.animateTo(
          targetValue = 1f,
          animationSpec = tween(durationMillis = 2400, easing = FastOutSlowInEasing)
        )
      }
    }
  }

  fun reset() {
    scope.launch {
      isOpen = false
      openProgress.snapTo(0f)
    }
  }

  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF0F0E11))
      .testTag("cinematic_reveal_host")
  ) {
    val fullWidth = maxWidth
    val fullHeight = maxHeight
    val p = openProgress.value

    // LAYER 1: The Invitation Card Canvas (Reveals underneath)
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp, vertical = 20.dp)
        .graphicsLayer {
          alpha = (p * 1.5f).coerceIn(0f, 1f)
          scaleX = 0.90f + (0.10f * p)
          scaleY = 0.90f + (0.10f * p)
        },
      contentAlignment = Alignment.Center
    ) {
      cardContent()
    }

    // LAYER 2: The Cinematic Reveal Physics Overlays (Active when p < 1f)
    if (p < 0.999f) {
      when (animationType) {
        "PALACE_DOORS", "PALACE_FRETWORK", "GILDED_ARCH", "DOUBLE_DOORS" -> {
          DoorsOrGatesOverlay(
            progress = p,
            fullWidth = fullWidth,
            isFretwork = (animationType == "PALACE_FRETWORK"),
            isArch = (animationType == "GILDED_ARCH"),
            isDoubleDoors = (animationType == "DOUBLE_DOORS")
          )
        }
        "WAX_SEAL", "RIBBON_UNTIE", "MONOGRAM_STAMP", "CARD_EMERGE" -> {
          EnvelopeSealOverlay(
            progress = p,
            isRibbon = (animationType == "RIBBON_UNTIE"),
            isMonogram = (animationType == "MONOGRAM_STAMP"),
            isCardEmerge = (animationType == "CARD_EMERGE")
          )
        }
        "SILK_DRAPE", "COUTURE_VEIL" -> {
          SilkVeilOverlay(progress = p, isVeil = (animationType == "COUTURE_VEIL"))
        }
        "BOTANICAL_GARLAND", "ORIGAMI_BLOOM", "FLORAL_BLOOM" -> {
          BotanicalOrFloralOverlay(
            progress = p,
            isOrigami = (animationType == "ORIGAMI_BLOOM"),
            isFloral = (animationType == "FLORAL_BLOOM")
          )
        }
        "STARLIGHT_GLOW", "ROSE_GOLD_SHIMMER", "SOFT_GLOW", "GOLDEN_LIGHT", "CINEMATIC_SHADOW" -> {
          CelestialGlowOverlay(
            progress = p,
            isRoseGold = (animationType == "ROSE_GOLD_SHIMMER"),
            isSoftGlow = (animationType == "SOFT_GLOW"),
            isGoldenLight = (animationType == "GOLDEN_LIGHT"),
            isShadow = (animationType == "CINEMATIC_SHADOW")
          )
        }
        "FOLDING_TRIPTYCH", "PAPER_UNFOLD", "SCROLL_UNROLL" -> {
          FoldingTriptychOverlay(
            progress = p,
            fullWidth = fullWidth,
            isScroll = (animationType == "SCROLL_UNROLL")
          )
        }
        "GOLD_CREST", "BAROQUE_FRAME", "KEEPSAKE_BOX", "CRYSTAL_GLASS" -> {
          RoyalCrestFrameOverlay(
            progress = p,
            isBox = (animationType == "KEEPSAKE_BOX"),
            isCrystal = (animationType == "CRYSTAL_GLASS")
          )
        }
        "EMERALD_VELVET" -> {
          VelvetCurtainsOverlay(progress = p, fullWidth = fullWidth, isEmerald = true)
        }
        else -> {
          // Default: Imperial Crimson Velvet Curtain
          VelvetCurtainsOverlay(progress = p, fullWidth = fullWidth, isEmerald = false)
        }
      }

      // Tap-to-Reveal Prompt when closed
      if (!isOpen) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .clickable(
              interactionSource = remember { MutableInteractionSource() },
              indication = null,
              onClick = { triggerOpen() }
            ),
          contentAlignment = Alignment.Center
        ) {
          TapToOpenSeal(onClick = { triggerOpen() })
        }
      }
    }

    // Replay / Reset floating control when fully revealed
    if (p > 0.95f) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .align(Alignment.BottomCenter)
          .padding(bottom = 12.dp),
        contentAlignment = Alignment.Center
      ) {
        OutlinedButton(
          onClick = { reset() },
          colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Black.copy(alpha = 0.55f),
            contentColor = GoldLight
          ),
          border = androidx.compose.foundation.BorderStroke(0.8.dp, GoldPrimary.copy(alpha = 0.6f)),
          shape = RoundedCornerShape(20.dp),
          modifier = Modifier.height(32.dp).testTag("replay_reveal_button")
        ) {
          Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(13.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Replay Reveal", fontSize = 11.sp, fontFamily = FontFamily.Serif)
        }
      }
    }
  }
}

// -------------------------------------------------------------
// OVERLAY 1: Velvet Curtains (Crimson or Emerald)
// -------------------------------------------------------------
@Composable
private fun VelvetCurtainsOverlay(
  progress: Float,
  fullWidth: androidx.compose.ui.unit.Dp,
  isEmerald: Boolean
) {
  val halfWidth = fullWidth / 2
  val leftOffset = -(halfWidth.value * progress)
  val rightOffset = (halfWidth.value * progress)

  val darkBase = if (isEmerald) Color(0xFF071F14) else VelvetCrimsonDeep
  val midColor = if (isEmerald) Color(0xFF0F3D29) else VelvetCrimson
  val lightColor = if (isEmerald) Color(0xFF1E5E41) else Color(0xFF9E1F30)

  // Left Curtain Wing
  Box(
    modifier = Modifier
      .fillMaxHeight()
      .width(halfWidth)
      .offset { IntOffset((leftOffset * density).roundToInt(), 0) }
      .background(
        Brush.horizontalGradient(
          0.0f to darkBase,
          0.3f to midColor,
          0.6f to lightColor,
          0.85f to midColor,
          1.0f to darkBase
        )
      )
  )

  // Right Curtain Wing
  Box(
    modifier = Modifier
      .fillMaxHeight()
      .width(halfWidth)
      .offset { IntOffset((halfWidth.value * density + rightOffset * density).roundToInt(), 0) }
      .background(
        Brush.horizontalGradient(
          0.0f to darkBase,
          0.15f to midColor,
          0.4f to lightColor,
          0.7f to midColor,
          1.0f to darkBase
        )
      )
  )
}

// -------------------------------------------------------------
// OVERLAY 2: Monumental Doors / Cathedral Arch / Palace Fretwork
// -------------------------------------------------------------
@Composable
private fun DoorsOrGatesOverlay(
  progress: Float,
  fullWidth: androidx.compose.ui.unit.Dp,
  isFretwork: Boolean,
  isArch: Boolean,
  isDoubleDoors: Boolean = false
) {
  val halfWidth = fullWidth / 2
  val leftAngle = -80f * progress
  val rightAngle = 80f * progress

  val bgLeft = if (isDoubleDoors) Color(0xFFF7F4EC) else if (isArch) Color(0xFF1A1713) else if (isFretwork) Color(0xFF1E140F) else Color(0xFF12100E)
  val bgRight = if (isDoubleDoors) Color(0xFFF0ECE2) else if (isArch) Color(0xFF161310) else if (isFretwork) Color(0xFF18100C) else Color(0xFF0E0C0A)

  // Left Portal Panel
  Box(
    modifier = Modifier
      .fillMaxHeight()
      .width(halfWidth)
      .graphicsLayer {
        rotationY = leftAngle
        cameraDistance = 12f * density
        alpha = (1f - (progress * 0.7f)).coerceIn(0f, 1f)
      }
      .background(bgLeft)
      .border(1.dp, GoldDark.copy(alpha = 0.5f))
  ) {
    DoorDetails(isArch = isArch, isFretwork = isFretwork)
  }

  // Right Portal Panel
  Box(
    modifier = Modifier
      .fillMaxHeight()
      .width(halfWidth)
      .offset(x = halfWidth)
      .graphicsLayer {
        rotationY = rightAngle
        cameraDistance = 12f * density
        alpha = (1f - (progress * 0.7f)).coerceIn(0f, 1f)
      }
      .background(bgRight)
      .border(1.dp, GoldDark.copy(alpha = 0.5f))
  ) {
    DoorDetails(isArch = isArch, isFretwork = isFretwork)
  }
}

@Composable
private fun DoorDetails(isArch: Boolean, isFretwork: Boolean) {
  Canvas(modifier = Modifier.fillMaxSize()) {
    val w = size.width
    val h = size.height
    val gold = Color(0xFFD4AF37)
    // Draw ornate decorative panels
    drawRect(
      color = gold.copy(alpha = 0.35f),
      topLeft = Offset(w * 0.15f, h * 0.12f),
      size = androidx.compose.ui.geometry.Size(w * 0.7f, h * 0.35f),
      style = Stroke(width = 2f)
    )
    drawRect(
      color = gold.copy(alpha = 0.35f),
      topLeft = Offset(w * 0.15f, h * 0.52f),
      size = androidx.compose.ui.geometry.Size(w * 0.7f, h * 0.35f),
      style = Stroke(width = 2f)
    )
  }
}

// -------------------------------------------------------------
// OVERLAY 3: Wax Seal / Ribbon / Monogram Envelope / Pocket Card Slide
// -------------------------------------------------------------
@Composable
private fun EnvelopeSealOverlay(
  progress: Float,
  isRibbon: Boolean,
  isMonogram: Boolean,
  isCardEmerge: Boolean = false
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .graphicsLayer {
        if (isCardEmerge) {
          translationY = progress * 150f
          alpha = (1f - progress * 1.3f).coerceIn(0f, 1f)
        } else {
          alpha = (1f - progress * 1.2f).coerceIn(0f, 1f)
          scaleX = 1f + progress * 0.15f
          scaleY = 1f + progress * 0.15f
        }
      }
      .background(if (isCardEmerge) Color(0xFF0F172A).copy(alpha = 0.94f) else Color(0xFF1E1B18).copy(alpha = 0.94f)),
    contentAlignment = Alignment.Center
  ) {
    // Envelope body
    Box(
      modifier = Modifier
        .size(width = 290.dp, height = 210.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(
          Brush.verticalGradient(
            listOf(Color(0xFF2C2723), Color(0xFF1F1B18))
          )
        )
        .border(1.dp, GoldDark.copy(alpha = 0.6f), RoundedCornerShape(8.dp)),
      contentAlignment = Alignment.Center
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val flapPath = Path().apply {
          moveTo(0f, 0f)
          lineTo(w / 2f, h * 0.6f)
          lineTo(w, 0f)
          close()
        }
        drawPath(flapPath, Color(0xFF38322D))
        drawPath(flapPath, Color(0xFFD4AF37).copy(alpha = 0.5f), style = Stroke(width = 1.5f))
      }

      // Center Seal / Ribbon
      Box(
        modifier = Modifier
          .size(68.dp)
          .shadow(8.dp, CircleShape)
          .background(if (isRibbon) GoldPrimary else if (isCardEmerge) Color(0xFF1B263B) else Color(0xFF8B1E28), CircleShape)
          .border(2.dp, GoldLight, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = if (isRibbon) "✦" else if (isMonogram) "I" else if (isCardEmerge) "✉" else "★",
          fontFamily = FontFamily.Serif,
          fontSize = 24.sp,
          color = if (isRibbon) Color(0xFF1A1610) else GoldLight
        )
      }
    }
  }
}

// -------------------------------------------------------------
// OVERLAY 4: Silk Drape or Couture Veil
// -------------------------------------------------------------
@Composable
private fun SilkVeilOverlay(progress: Float, isVeil: Boolean) {
  val lift = -progress * 1.3f
  val baseColor = if (isVeil) Color(0xFFF9F6F0).copy(alpha = 0.85f) else Color(0xFFD6C09C)

  Box(
    modifier = Modifier
      .fillMaxSize()
      .graphicsLayer {
        translationY = lift * size.height
        alpha = (1f - progress * 1.1f).coerceIn(0f, 1f)
      }
      .background(
        Brush.verticalGradient(
          listOf(
            baseColor,
            Color(0xFFC4AD82),
            Color(0xFF8F784E)
          )
        )
      )
  )
}

// -------------------------------------------------------------
// OVERLAY 5: Botanical Garland or Origami Bloom or Floral Bloom
// -------------------------------------------------------------
@Composable
private fun BotanicalOrFloralOverlay(
  progress: Float,
  isOrigami: Boolean,
  isFloral: Boolean = false
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .graphicsLayer {
        alpha = (1f - progress * 1.15f).coerceIn(0f, 1f)
        scaleX = 1f + progress * 0.4f
        scaleY = 1f + progress * 0.4f
        rotationZ = progress * (if (isFloral) -20f else 15f)
      }
      .background(if (isFloral) Color(0xFF1A1215).copy(alpha = 0.95f) else Color(0xFF0F1410).copy(alpha = 0.95f)),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.size(240.dp)) {
      val r = size.minDimension / 2.2f
      val center = Offset(size.width / 2f, size.height / 2f)
      val petalColor = if (isFloral) Color(0xFFD48B96) else if (isOrigami) Color(0xFFE5C07B) else Color(0xFF7A9373)

      for (i in 0 until 8) {
        val angle = (i * 45f) * (Math.PI / 180f).toFloat()
        val x = center.x + cos(angle) * r
        val y = center.y + sin(angle) * r
        drawCircle(
          color = petalColor.copy(alpha = 0.65f),
          radius = if (isFloral) 34f else 28f,
          center = Offset(x, y)
        )
      }
      drawCircle(
        color = Color(0xFFD4AF37),
        radius = 42f,
        center = center,
        style = Stroke(width = 3f)
      )
    }
  }
}

// -------------------------------------------------------------
// OVERLAY 6: Celestial Starlight / Rose Gold / Golden Light / Shadow
// -------------------------------------------------------------
@Composable
private fun CelestialGlowOverlay(
  progress: Float,
  isRoseGold: Boolean,
  isSoftGlow: Boolean,
  isGoldenLight: Boolean = false,
  isShadow: Boolean = false
) {
  val tint = when {
    isShadow -> Color(0xFF242220)
    isGoldenLight -> Color(0xFFFFD56B)
    isRoseGold -> Color(0xFFC7828E)
    isSoftGlow -> Color(0xFFE8D5B5)
    else -> Color(0xFFE5C07B)
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .graphicsLayer {
        alpha = (1f - progress * 1.3f).coerceIn(0f, 1f)
        scaleX = 1f + progress * 0.2f
        scaleY = 1f + progress * 0.2f
      }
      .background(
        Brush.radialGradient(
          listOf(
            tint.copy(alpha = if (isShadow) 0.95f else 0.85f),
            Color(0xFF141210).copy(alpha = 0.96f)
          )
        )
      )
  )
}

// -------------------------------------------------------------
// OVERLAY 7: Folding Triptych Gatefold / Scroll Unroll
// -------------------------------------------------------------
@Composable
private fun FoldingTriptychOverlay(
  progress: Float,
  fullWidth: androidx.compose.ui.unit.Dp,
  isScroll: Boolean = false
) {
  val halfWidth = fullWidth / 2
  val leftFold = -90f * progress
  val rightFold = 90f * progress

  Box(
    modifier = Modifier
      .fillMaxHeight()
      .width(halfWidth)
      .graphicsLayer {
        rotationY = leftFold
        alpha = (1f - progress * 0.8f).coerceIn(0f, 1f)
      }
      .background(if (isScroll) Color(0xFFD8C3A5) else Color(0xFF24201D))
      .border(1.dp, GoldDark.copy(alpha = 0.4f))
  )

  Box(
    modifier = Modifier
      .fillMaxHeight()
      .width(halfWidth)
      .offset(x = halfWidth)
      .graphicsLayer {
        rotationY = rightFold
        alpha = (1f - progress * 0.8f).coerceIn(0f, 1f)
      }
      .background(if (isScroll) Color(0xFFC8B395) else Color(0xFF1E1A17))
      .border(1.dp, GoldDark.copy(alpha = 0.4f))
  )
}

// -------------------------------------------------------------
// OVERLAY 8: Royal Crest / Baroque Frame / Keepsake Box / Crystal Glass
// -------------------------------------------------------------
@Composable
private fun RoyalCrestFrameOverlay(
  progress: Float,
  isBox: Boolean,
  isCrystal: Boolean = false
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .graphicsLayer {
        alpha = (1f - progress * 1.25f).coerceIn(0f, 1f)
        scaleX = 1f + progress * 0.25f
        scaleY = 1f + progress * 0.25f
      }
      .background(if (isCrystal) Color(0xFF1E293B).copy(alpha = 0.90f) else Color(0xFF121114).copy(alpha = 0.96f)),
    contentAlignment = Alignment.Center
  ) {
    Box(
      modifier = Modifier
        .size(280.dp, 360.dp)
        .border(4.dp, GoldPrimary, RoundedCornerShape(12.dp))
        .padding(8.dp)
        .border(1.dp, GoldLight.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = if (isCrystal) "✧" else if (isBox) "♔" else "⚜",
        fontFamily = FontFamily.Serif,
        fontSize = 48.sp,
        color = GoldPrimary
      )
    }
  }
}

// -------------------------------------------------------------
// Tap To Open Seal Prompt
// -------------------------------------------------------------
@Composable
private fun TapToOpenSeal(onClick: () -> Unit) {
  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.05f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulseScale"
  )

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Box(
      modifier = Modifier
        .size(92.dp)
        .scale(pulseScale)
        .shadow(16.dp, CircleShape)
        .clip(CircleShape)
        .background(
          Brush.radialGradient(
            listOf(GoldLight, GoldPrimary, GoldDark)
          )
        )
        .border(2.5.dp, Color(0xFFFFF6D6), CircleShape)
        .clickable(onClick = onClick)
        .testTag("tap_to_open_invitation"),
      contentAlignment = Alignment.Center
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
          imageVector = Icons.Default.TouchApp,
          contentDescription = null,
          tint = Color(0xFF1C1814),
          modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = "OPEN",
          fontFamily = FontFamily.Serif,
          fontWeight = FontWeight.Bold,
          fontSize = 11.sp,
          letterSpacing = 1.8.sp,
          color = Color(0xFF1C1814)
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    Text(
      text = "Tap to Reveal Invitation",
      fontFamily = FontFamily.Serif,
      fontSize = 13.sp,
      letterSpacing = 1.2.sp,
      color = GoldLight,
      modifier = Modifier
        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
        .padding(horizontal = 14.dp, vertical = 6.dp)
    )
  }
}
