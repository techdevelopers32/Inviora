package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.IvoryBg
import kotlinx.coroutines.launch

@Composable
fun RoyalDoorsScene(
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
          animationSpec = tween(durationMillis = 2600, easing = FastOutSlowInEasing)
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
      .testTag("royal_doors_scene")
  ) {
    val screenWidth = maxWidth
    val screenHeight = maxHeight
    val progress = openProgress.value

    // 1. INNER INVITATION CONTENT (revealed behind doors)
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp, vertical = 24.dp),
      contentAlignment = Alignment.Center
    ) {
      val cardScale = 0.82f + (progress * 0.18f)
      val cardAlpha = (progress * 1.6f).coerceIn(0f, 1f)

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

    // 2. WARM LIGHT SHAFT
    if (progress > 0f && progress < 0.95f) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .alpha(1f - (progress - 0.4f).coerceAtLeast(0f) * 2f)
          .background(
            Brush.radialGradient(
              colors = listOf(
                Color(0x66F3E5AB),
                Color(0x33D4AF37),
                Color.Transparent
              ),
              center = Offset(screenWidth.value / 2, screenHeight.value * 0.45f),
              radius = 900f
            )
          )
      )
    }

    // 3. 3D DOUBLE PALACE DOORS
    if (progress < 0.99f) {
      Row(
        modifier = Modifier.fillMaxSize()
      ) {
        // Left Door Panel (opens swinging outward to left)
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .graphicsLayer {
              cameraDistance = 16 * density
              rotationY = -82f * progress
              transformOrigin = TransformOrigin(0f, 0.5f)
              shadowElevation = 18f
            }
            .background(
              Brush.horizontalGradient(
                listOf(
                  Color(0xFF140E0A),
                  Color(0xFF261912),
                  Color(0xFF1B110B),
                  Color(0xFF2E1F16)
                )
              )
            )
            .border(width = 1.dp, color = GoldDark.copy(alpha = 0.5f))
            .padding(16.dp)
        ) {
          PalaceDoorFiligree(isLeft = true)
        }

        // Right Door Panel (opens swinging outward to right)
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .graphicsLayer {
              cameraDistance = 16 * density
              rotationY = 82f * progress
              transformOrigin = TransformOrigin(1f, 0.5f)
              shadowElevation = 18f
            }
            .background(
              Brush.horizontalGradient(
                listOf(
                  Color(0xFF2E1F16),
                  Color(0xFF1B110B),
                  Color(0xFF261912),
                  Color(0xFF140E0A)
                )
              )
            )
            .border(width = 1.dp, color = GoldDark.copy(alpha = 0.5f))
            .padding(16.dp)
        ) {
          PalaceDoorFiligree(isLeft = false)
        }
      }
    }

    // 4. TAP TO OPEN PROMPT
    if (progress < 0.1f) {
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
              color = Color(0xAA000000),
              shape = RoundedCornerShape(24.dp)
            )
            .border(0.5.dp, GoldPrimary, RoundedCornerShape(24.dp))
            .padding(horizontal = 24.dp, vertical = 14.dp)
            .shadow(10.dp)
        ) {
          Icon(
            imageVector = Icons.Default.TouchApp,
            contentDescription = null,
            tint = GoldLight,
            modifier = Modifier.size(28.dp)
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "TAP TO OPEN DOORS",
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
private fun PalaceDoorFiligree(isLeft: Boolean) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .border(0.5.dp, GoldDark.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
      .padding(12.dp),
    contentAlignment = if (isLeft) Alignment.CenterEnd else Alignment.CenterStart
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.fillMaxHeight()
    ) {
      Text(
        text = "❖",
        color = GoldDark,
        fontSize = 14.sp
      )

      // Brass Door Handle / Ring
      Box(
        modifier = Modifier
          .size(28.dp)
          .border(2.dp, GoldLight, RoundedCornerShape(14.dp))
          .background(Color(0x33D4AF37), RoundedCornerShape(14.dp))
      )

      Text(
        text = "❖",
        color = GoldDark,
        fontSize = 14.sp
      )
    }
  }
}
