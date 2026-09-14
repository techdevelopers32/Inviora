package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AntiqueIvory
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.IvoryBg
import com.example.ui.theme.WaxSealRuby
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun WaxSealEnvelopeScene(
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
          animationSpec = tween(durationMillis = 2500, easing = FastOutSlowInEasing)
        )
      }
    }
  }

  fun triggerReplay() {
    scope.launch {
      openProgress.animateTo(0f, tween(600))
      isOpen = false
    }
  }

  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
      .background(IvoryBg)
      .testTag("wax_seal_envelope_scene")
  ) {
    val screenHeight = maxHeight
    val progress = openProgress.value

    // 1. REVEALED INVITATION CARD (Sliding out and expanding)
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp, vertical = 24.dp),
      contentAlignment = Alignment.Center
    ) {
      val cardScale = 0.85f + (progress * 0.15f)
      val cardAlpha = (progress * 1.5f).coerceIn(0f, 1f)
      val cardOffsetY = ((1f - progress) * 80f).dp

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .offset { IntOffset(0, cardOffsetY.roundToPx()) }
          .graphicsLayer {
            scaleX = cardScale
            scaleY = cardScale
            alpha = cardAlpha
          },
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        cardContent()

        if (progress > 0.8f) {
          Spacer(modifier = Modifier.height(20.dp))
          OutlinedButton(
            onClick = { triggerReplay() },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldLight),
            border = ButtonDefaults.outlinedButtonBorder.copy(
              brush = Brush.linearGradient(listOf(GoldPrimary, GoldDark))
            ),
            modifier = Modifier.testTag("replay_animation_button")
          ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.size(8.dp))
            Text(
              "Replay Experience",
              fontSize = 12.sp,
              fontFamily = FontFamily.Serif,
              letterSpacing = 1.sp
            )
          }
        }
      }
    }

    // 2. ENVELOPE BASE & FLAP (Fades away as card takes over)
    if (progress < 0.95f) {
      val envelopeAlpha = 1f - (progress * 1.2f).coerceIn(0f, 1f)

      Box(
        modifier = Modifier
          .fillMaxSize()
          .alpha(envelopeAlpha),
        contentAlignment = Alignment.Center
      ) {
        // Main Envelope Body
        Box(
          modifier = Modifier
            .width(340.dp)
            .height(240.dp)
            .shadow(16.dp, RoundedCornerShape(8.dp))
            .background(AntiqueIvory, RoundedCornerShape(8.dp))
            .border(1.dp, GoldDark.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
        ) {
          // Decorative texture lines
          EnvelopeBackLines()

          // Top Flap (Rotates back in 3D)
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(120.dp)
              .align(Alignment.TopCenter)
              .graphicsLayer {
                cameraDistance = 14 * density
                rotationX = -135f * progress
                transformOrigin = TransformOrigin(0.5f, 0f)
              }
          ) {
            EnvelopeFlapShape()
          }

          // Wax Seal (breaks and fades)
          val sealAlpha = 1f - (progress * 2f).coerceIn(0f, 1f)
          if (sealAlpha > 0f) {
            WaxSealEmblem(
              modifier = Modifier
                .align(Alignment.Center)
                .alpha(sealAlpha)
                .shadow(12.dp, RoundedCornerShape(26.dp))
            )
          }
        }
      }
    }

    // 3. TAP TO BREAK SEAL PROMPT
    if (progress < 0.1f) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = { triggerOpen() }
          ),
        contentAlignment = Alignment.BottomCenter
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier
            .padding(bottom = 60.dp)
            .background(
              color = Color(0xDD000000),
              shape = RoundedCornerShape(24.dp)
            )
            .border(0.5.dp, GoldPrimary, RoundedCornerShape(24.dp))
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .shadow(10.dp)
        ) {
          Icon(
            imageVector = Icons.Default.TouchApp,
            contentDescription = null,
            tint = GoldLight,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "TAP TO BREAK WAX SEAL",
            fontFamily = FontFamily.Serif,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 2.sp,
            color = GoldLight
          )
        }
      }
    }
  }
}

@Composable
private fun WaxSealEmblem(modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .size(56.dp)
      .background(WaxSealRuby, RoundedCornerShape(28.dp))
      .border(1.5.dp, GoldPrimary, RoundedCornerShape(28.dp))
      .padding(3.dp)
      .border(0.5.dp, GoldLight.copy(alpha = 0.5f), RoundedCornerShape(25.dp)),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = "I",
      fontFamily = FontFamily.Serif,
      fontWeight = FontWeight.Bold,
      fontSize = 26.sp,
      color = GoldLight
    )
  }
}

@Composable
private fun EnvelopeBackLines() {
  Canvas(modifier = Modifier.fillMaxSize()) {
    val w = size.width
    val h = size.height
    // Fold lines meeting in center
    drawLine(
      color = Color(0x228F7432),
      start = Offset(0f, h),
      end = Offset(w / 2, h * 0.55f),
      strokeWidth = 2f
    )
    drawLine(
      color = Color(0x228F7432),
      start = Offset(w, h),
      end = Offset(w / 2, h * 0.55f),
      strokeWidth = 2f
    )
  }
}

@Composable
private fun EnvelopeFlapShape() {
  Canvas(modifier = Modifier.fillMaxSize()) {
    val w = size.width
    val h = size.height
    val path = Path().apply {
      moveTo(0f, 0f)
      lineTo(w, 0f)
      lineTo(w / 2, h)
      close()
    }
    drawPath(
      path = path,
      brush = Brush.verticalGradient(
        listOf(AntiqueIvory, Color(0xFFEBE6DC))
      )
    )
    drawPath(
      path = path,
      color = Color(0x44D4AF37),
      style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
    )
  }
}
