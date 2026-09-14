package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.IvoryBg
import com.example.ui.theme.VelvetCrimson
import com.example.ui.theme.VelvetCrimsonDeep
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun LuxuryCurtainScene(
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
      .testTag("luxury_curtain_scene")
  ) {
    val screenWidth = maxWidth
    val screenHeight = maxHeight

    // 1. REVEALED INVITATION CONTENT (Background layer)
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp, vertical = 24.dp),
      contentAlignment = Alignment.Center
    ) {
      val progress = openProgress.value
      val cardScale = 0.85f + (progress * 0.15f)
      val cardAlpha = (progress * 1.5f).coerceIn(0f, 1f)

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
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
            border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(GoldPrimary, GoldDark))),
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

    // 2. WARM GOLDEN STAGE LIGHTING GLOW (Visible as curtains part)
    if (openProgress.value > 0f && openProgress.value < 0.95f) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .alpha(1f - (openProgress.value - 0.5f).coerceAtLeast(0f) * 2f)
          .background(
            Brush.radialGradient(
              colors = listOf(
                Color(0x55E5C07B),
                Color(0x22D4AF37),
                Color.Transparent
              ),
              center = Offset(screenWidth.value / 2, screenHeight.value * 0.4f),
              radius = 800f
            )
          )
      )
    }

    // 3. CURTAIN PANELS (Left & Right)
    val curtainOffset = (screenWidth.value * openProgress.value).dp

    // Left Curtain Panel
    Box(
      modifier = Modifier
        .fillMaxHeight()
        .width(screenWidth / 2)
        .align(Alignment.CenterStart)
        .offset { IntOffset(x = -curtainOffset.roundToPx(), y = 0) }
        .shadow(elevation = 12.dp)
        .background(
          Brush.horizontalGradient(
            listOf(
              VelvetCrimsonDeep,
              VelvetCrimson,
              VelvetCrimsonDeep,
              VelvetCrimson,
              VelvetCrimsonDeep,
              Color(0xFF280308)
            )
          )
        )
    ) {
      // Golden fringe on bottom
      CurtainFringe(modifier = Modifier.align(Alignment.BottomCenter))
    }

    // Right Curtain Panel
    Box(
      modifier = Modifier
        .fillMaxHeight()
        .width(screenWidth / 2)
        .align(Alignment.CenterEnd)
        .offset { IntOffset(x = curtainOffset.roundToPx(), y = 0) }
        .shadow(elevation = 12.dp)
        .background(
          Brush.horizontalGradient(
            listOf(
              Color(0xFF280308),
              VelvetCrimsonDeep,
              VelvetCrimson,
              VelvetCrimsonDeep,
              VelvetCrimson,
              VelvetCrimsonDeep
            )
          )
        )
    ) {
      CurtainFringe(modifier = Modifier.align(Alignment.BottomCenter))
    }

    // 4. TOP PELMET / VALANCE DRAPERY
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(56.dp)
        .align(Alignment.TopCenter)
        .shadow(16.dp)
        .background(
          Brush.verticalGradient(
            listOf(VelvetCrimsonDeep, VelvetCrimson, Color(0xFF280308))
          )
        )
    ) {
      CurtainFringe(modifier = Modifier.align(Alignment.BottomCenter))
    }

    // 5. TAP TO REVEAL PROMPT OVERLAY (Visible when closed)
    if (openProgress.value < 0.1f) {
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
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier
            .background(
              color = Color(0x99000000),
              shape = RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 24.dp, vertical = 14.dp)
            .shadow(8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.TouchApp,
            contentDescription = null,
            tint = GoldLight,
            modifier = Modifier.size(28.dp)
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "TAP TO REVEAL",
            fontFamily = FontFamily.Serif,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 2.5.sp,
            color = GoldLight
          )
        }
      }
    }
  }
}

@Composable
private fun CurtainFringe(modifier: Modifier = Modifier) {
  Canvas(
    modifier = modifier
      .fillMaxWidth()
      .height(10.dp)
  ) {
    drawRect(
      brush = Brush.verticalGradient(
        listOf(GoldDark, GoldLight, GoldDark)
      )
    )
  }
}
