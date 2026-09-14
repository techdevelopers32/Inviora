package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import com.example.data.model.AnimationExperienceEntity
import com.example.data.model.DesignTemplateEntity
import com.example.data.model.EventEntity
import com.example.data.model.GuestEntity
import com.example.ui.components.CinematicRevealHost
import com.example.ui.components.MultiPageInvitationView
import com.example.ui.theme.CharcoalMuted
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.IvoryBg
import com.example.ui.theme.IvoryBorder
import com.example.ui.theme.IvoryBorderStrong
import com.example.ui.theme.IvorySurface
import com.example.ui.theme.IvorySurfaceLight

@Composable
fun GuestExperienceScreen(
  event: EventEntity,
  guest: GuestEntity?,
  allDesigns: List<DesignTemplateEntity>,
  animation: AnimationExperienceEntity?,
  onClose: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isMasterPreview = (guest == null)
  val token = guest?.uniqueToken ?: "master_preview"
  val hostedUrl = "https://yourusername.github.io/inviora/invite/$token"
  val animType = animation?.animationType ?: "CURTAIN"

  // Check if link is active/valid
  val isLinkValid = isMasterPreview || (guest?.active == true)

  // Filter pages to strictly what this guest was invited to
  val allPages = event.getPages()
  val pagesToShow = if (isMasterPreview) {
    allPages
  } else {
    guest!!.getInvitedPages(allPages)
  }

  val designsMap = allDesigns.associateBy { it.id }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(IvoryBg)
      .statusBarsPadding()
      .testTag("guest_experience_screen")
  ) {
    // Top Simulated Hosted Link Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(IvorySurface)
        .border(0.5.dp, IvoryBorder)
        .padding(horizontal = 12.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Box(
        modifier = Modifier
          .weight(1f)
          .clip(RoundedCornerShape(6.dp))
          .background(IvorySurfaceLight)
          .border(0.5.dp, IvoryBorderStrong, RoundedCornerShape(6.dp))
          .padding(horizontal = 12.dp, vertical = 6.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Lock, contentDescription = null, tint = GoldDark, modifier = Modifier.size(12.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = hostedUrl,
            fontSize = 11.sp,
            color = GoldDark,
            maxLines = 1
          )
        }
      }

      Spacer(modifier = Modifier.width(8.dp))

      IconButton(
        onClick = onClose,
        modifier = Modifier
          .size(36.dp)
          .testTag("close_guest_experience_button")
      ) {
        Icon(Icons.Default.Close, contentDescription = "Close", tint = CharcoalMuted)
      }
    }

    // Main Interactive Opening & Reveal Stage
    Box(
      modifier = Modifier
        .fillMaxSize()
        .weight(1f)
    ) {
      if (!isLinkValid) {
        // Inactive / Invalidated Token Screen
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF141210)),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
          ) {
            Icon(
              imageVector = Icons.Default.ErrorOutline,
              contentDescription = null,
              tint = Color(0xFFC7828E),
              modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = "Invitation Link Expired",
              fontFamily = FontFamily.Serif,
              fontSize = 20.sp,
              color = GoldLight
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "This personalized invitation token is no longer active. Please contact the host for a renewed link.",
              fontFamily = FontFamily.SansSerif,
              fontSize = 13.sp,
              color = Color(0xFFA8A096),
              textAlign = TextAlign.Center
            )
          }
        }
      } else {
        // Active: Run selected cinematic reveal animation
        CinematicRevealHost(
          animationType = animType,
          isOpenInitially = false
        ) {
          MultiPageInvitationView(
            pages = pagesToShow,
            designsMap = designsMap,
            event = event,
            guest = guest,
            guestName = guest?.name
          )
        }
      }
    }
  }
}
