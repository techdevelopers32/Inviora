package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
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

@Composable
fun SettingsDialog(
  onDismiss: () -> Unit
) {
  val hasGeminiKey = try {
    val key = BuildConfig.GEMINI_API_KEY
    !key.isNullOrBlank() && key != "MY_GEMINI_API_KEY"
  } catch (_: Exception) {
    false
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = IvorySurface,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Key, contentDescription = null, tint = GoldDark, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.size(8.dp))
        Text("Studio Settings", fontFamily = FontFamily.Serif, color = CharcoalPrimary)
      }
    },
    text = {
      Column {
        Text(
          text = "GEMINI AI ACCELERATION",
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.5.sp,
          color = GoldDark
        )
        Spacer(modifier = Modifier.height(6.dp))

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(IvorySurfaceLight)
            .border(0.5.dp, IvoryBorderStrong, RoundedCornerShape(8.dp))
            .padding(12.dp)
        ) {
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("API Key Status", fontSize = 12.sp, color = CharcoalPrimary)
              Text(
                text = if (hasGeminiKey) "Configured" else "Studio Default",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (hasGeminiKey) Color(0xFF2E6F40) else GoldDark
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = if (hasGeminiKey)
                "Using custom API key from environment secrets."
              else
                "Powered by platform Gemini multi-modal synthesis.",
              fontSize = 11.sp,
              color = CharcoalMuted
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "DATABASE & STORAGE",
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.5.sp,
          color = GoldDark
        )
        Spacer(modifier = Modifier.height(6.dp))

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(IvorySurfaceLight)
            .border(0.5.dp, IvoryBorderStrong, RoundedCornerShape(8.dp))
            .padding(12.dp)
        ) {
          Text(
            text = "Local SQLite Room database v2. All designs, animation experiences, events, and personalized guest tokens persist offline.",
            fontSize = 11.sp,
            color = CharcoalSecondary,
            lineHeight = 16.sp
          )
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Done", color = GoldDark, fontWeight = FontWeight.SemiBold)
      }
    },
    shape = RoundedCornerShape(12.dp)
  )
}

@Composable
fun AboutDialog(
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = IvorySurface,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Info, contentDescription = null, tint = GoldDark, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.size(8.dp))
        Text("About Inviora", fontFamily = FontFamily.Serif, color = CharcoalPrimary)
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Icon(
          imageVector = Icons.Default.AutoAwesome,
          contentDescription = null,
          tint = GoldPrimary,
          modifier = Modifier.size(36.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "INVIORA",
          fontFamily = FontFamily.Serif,
          fontSize = 18.sp,
          fontWeight = FontWeight.SemiBold,
          letterSpacing = 3.sp,
          color = CharcoalPrimary
        )
        Text(
          text = "Moments, beautifully revealed.",
          fontSize = 12.sp,
          color = GoldDark,
          fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "Inviora is an invitation creation platform. Users synthesize bespoke digital invitation designs and cinematic reveal experiences from their own reference photographs, link them to multi-event celebrations, personalize each guest's token, and share invitations seamlessly via WhatsApp.",
          fontSize = 12.sp,
          color = CharcoalSecondary,
          textAlign = TextAlign.Center,
          lineHeight = 17.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "Version 2.0 • Light Luxury Edition",
          fontSize = 10.sp,
          color = CharcoalMuted,
          letterSpacing = 1.sp
        )
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Close", color = GoldDark, fontWeight = FontWeight.SemiBold)
      }
    },
    shape = RoundedCornerShape(12.dp)
  )
}
