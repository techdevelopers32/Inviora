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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
fun HomeScreen(
  currentEvent: EventEntity?,
  currentDesign: DesignTemplateEntity?,
  currentAnimation: AnimationExperienceEntity?,
  guestCount: Int,
  onCreateEventClick: () -> Unit,
  onContinueWorkingClick: (EventEntity) -> Unit,
  onSwitchEventClick: () -> Unit,
  onPreviewMasterClick: (EventEntity) -> Unit,
  onManageGuestsClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(IvoryBg)
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 20.dp, vertical = 16.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {

    // Primary Action: + Create Event
    ElevatedButton(
      onClick = onCreateEventClick,
      colors = ButtonDefaults.elevatedButtonColors(
        containerColor = GoldPrimary,
        contentColor = Color.White
      ),
      shape = RoundedCornerShape(10.dp),
      modifier = Modifier
        .fillMaxWidth()
        .height(52.dp)
        .shadow(4.dp, RoundedCornerShape(10.dp))
        .testTag("create_event_button")
    ) {
      Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = "Create Event",
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        letterSpacing = 0.8.sp
      )
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Active Event Showcase Card
    if (currentEvent != null) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, IvoryBorderStrong, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = IvorySurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(14.dp)
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(IvorySurfaceLight)
                .border(0.5.dp, GoldPrimary, RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = "ACTIVE EVENT",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.6.sp,
                color = GoldDark
              )
            }

            Text(
              text = "Switch Event",
              fontSize = 12.sp,
              color = GoldDark,
              fontWeight = FontWeight.Medium,
              modifier = Modifier
                .clickable { onSwitchEventClick() }
                .padding(4.dp)
                .testTag("switch_event_button")
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          Text(
            text = currentEvent.title,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Normal,
            fontSize = 24.sp,
            lineHeight = 30.sp,
            color = CharcoalPrimary
          )

          if (currentEvent.eventType.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = currentEvent.eventType,
              fontSize = 12.sp,
              color = GoldDark,
              fontWeight = FontWeight.Medium
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Date & Venue Details
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(
              imageVector = Icons.Default.CalendarMonth,
              contentDescription = null,
              tint = GoldPrimary,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = currentEvent.primaryDate.ifBlank { "Date to be announced" },
              fontSize = 12.sp,
              color = CharcoalSecondary
            )
          }

          Spacer(modifier = Modifier.height(6.dp))

          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(
              imageVector = Icons.Default.People,
              contentDescription = null,
              tint = GoldPrimary,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "$guestCount Personalized Invitation Links",
              fontSize = 12.sp,
              color = CharcoalSecondary
            )
          }

          Spacer(modifier = Modifier.height(18.dp))

          // Linked Design & Animation Pills
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // Design Badge
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(IvorySurfaceLight)
                .border(0.5.dp, IvoryBorder, RoundedCornerShape(8.dp))
                .padding(10.dp)
            ) {
              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.ColorLens, contentDescription = null, tint = GoldDark, modifier = Modifier.size(12.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("DESIGN", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GoldDark, letterSpacing = 1.sp)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = currentDesign?.name ?: "Royal Gold",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Medium,
                  color = CharcoalPrimary,
                  maxLines = 1
                )
              }
            }

            // Animation Badge
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(IvorySurfaceLight)
                .border(0.5.dp, IvoryBorder, RoundedCornerShape(8.dp))
                .padding(10.dp)
            ) {
              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.Animation, contentDescription = null, tint = GoldDark, modifier = Modifier.size(12.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("ANIMATION", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GoldDark, letterSpacing = 1.sp)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = currentAnimation?.name ?: "Velvet Curtain",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Medium,
                  color = CharcoalPrimary,
                  maxLines = 1
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          // Action Buttons: Master Preview & Manage Guests
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedButton(
              onClick = { onPreviewMasterClick(currentEvent) },
              shape = RoundedCornerShape(8.dp),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = CharcoalPrimary),
              modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .testTag("home_master_preview_button")
            ) {
              Icon(Icons.Default.Visibility, contentDescription = null, tint = GoldDark, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Master Preview", fontSize = 12.sp)
            }

            Button(
              onClick = { onContinueWorkingClick(currentEvent) },
              colors = ButtonDefaults.buttonColors(
                containerColor = GoldPrimary,
                contentColor = Color.White
              ),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .testTag("continue_working_button")
            ) {
              Text("Edit Event", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
              Spacer(modifier = Modifier.width(4.dp))
              Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
            }
          }
        }
      }
    } else {
      // Empty state
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, IvoryBorder, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = IvorySurface),
        shape = RoundedCornerShape(14.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = GoldDark, modifier = Modifier.size(40.dp))
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "Begin Your First Celebration",
            fontFamily = FontFamily.Serif,
            fontSize = 18.sp,
            color = CharcoalPrimary
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Create an event, combine reusable designs and animations, and share personalized invites.",
            fontSize = 12.sp,
            color = CharcoalMuted,
            textAlign = TextAlign.Center
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Studio Workflow Guidance Card
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .border(0.5.dp, IvoryBorder, RoundedCornerShape(12.dp)),
      colors = CardDefaults.cardColors(containerColor = IvorySurfaceLight),
      shape = RoundedCornerShape(12.dp)
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        Text(
          text = "THE INVIORA STUDIO PHILOSOPHY",
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.6.sp,
          color = GoldDark
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "Inviora is not a catalogue of generic templates. Create custom designs from physical references, pair them with cinematic reveal animations, and deliver unforgettable moments to each guest.",
          fontSize = 12.sp,
          lineHeight = 18.sp,
          color = CharcoalSecondary
        )
      }
    }

    Spacer(modifier = Modifier.height(30.dp))
  }
}
