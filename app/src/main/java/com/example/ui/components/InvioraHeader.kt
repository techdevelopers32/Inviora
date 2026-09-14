package com.example.ui.components

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CharcoalMuted
import com.example.ui.theme.CharcoalPrimary
import com.example.ui.theme.GoldDark
import com.example.ui.theme.IvoryBg
import com.example.ui.theme.IvoryBorder

@Composable
fun InvioraHeader(
  onMenuClick: () -> Unit,
  modifier: Modifier = Modifier,
  actionButton: (@Composable () -> Unit)? = null
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .background(IvoryBg)
      .statusBarsPadding()
      .border(width = 0.5.dp, color = IvoryBorder)
      .padding(horizontal = 8.dp, vertical = 8.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      // Hamburger aligned vertically on top-left
      IconButton(
        onClick = onMenuClick,
        modifier = Modifier
          .size(48.dp)
          .testTag("hamburger_menu_button")
      ) {
        Icon(
          imageVector = Icons.Default.Menu,
          contentDescription = "Menu",
          tint = CharcoalPrimary,
          modifier = Modifier.size(24.dp)
        )
      }

      // Centered Brand & Tagline block
      Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Text(
          text = "Inviora",
          fontFamily = FontFamily.Serif,
          fontWeight = FontWeight.Normal,
          fontSize = 24.sp,
          letterSpacing = 2.5.sp,
          color = CharcoalPrimary,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
          text = "Moments, beautifully revealed.",
          fontFamily = FontFamily.SansSerif,
          fontWeight = FontWeight.Normal,
          fontSize = 11.sp,
          letterSpacing = 0.8.sp,
          color = CharcoalMuted,
          maxLines = 1,
          softWrap = false,
          textAlign = TextAlign.Center
        )
      }

      // Right action or balancer to keep title strictly centered
      if (actionButton != null) {
        actionButton()
      } else {
        Spacer(modifier = Modifier.width(48.dp))
      }
    }
  }
}

