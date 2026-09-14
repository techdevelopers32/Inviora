package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
import kotlinx.coroutines.launch

@Composable
fun AnimationLibraryScreen(
  animations: List<AnimationExperienceEntity>,
  allEvents: List<EventEntity> = emptyList(),
  currentEvent: EventEntity? = null,
  allDesigns: List<DesignTemplateEntity> = emptyList(),
  currentDesign: DesignTemplateEntity? = null,
  onCreateAnimationClick: () -> Unit = {},
  onPreviewAnimation: (AnimationExperienceEntity) -> Unit,
  onApplyAnimationToEvent: ((AnimationExperienceEntity, EventEntity) -> Unit)? = null,
  checkEventsUsingAnimation: suspend (String) -> List<EventEntity>,
  onDeleteAnimation: (String, Boolean) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  var animPendingDelete by remember { mutableStateOf<AnimationExperienceEntity?>(null) }
  var dependentEvents by remember { mutableStateOf<List<EventEntity>>(emptyList()) }
  var selectedCategory by remember { mutableStateOf("All") }
  var animToApply by remember { mutableStateOf<AnimationExperienceEntity?>(null) }

  val categories = listOf(
    "All",
    "Curtains & Portals",
    "Royal Gates & Crests",
    "Florals & Nature",
    "Envelopes & Seals",
    "Celestial & Glow",
    "Scrolls & Heritage"
  )

  val filteredAnimations = remember(animations, selectedCategory) {
    when (selectedCategory) {
      "Curtains & Portals" -> animations.filter {
        it.animationType in listOf("velvet_curtain", "silk_drape", "velvet_drapes_tassel", "royal_velvet_curtain", "palace_doors", "jaali_screen", "mughal_arch")
      }
      "Royal Gates & Crests" -> animations.filter {
        it.animationType in listOf("golden_gates", "royal_gate", "royal_crest_unfold", "royal_carriage")
      }
      "Florals & Nature" -> animations.filter {
        it.animationType in listOf("botanical_wreath", "floral_bloom", "jasmine_garland", "botanical_reveal", "botanical_shadow")
      }
      "Envelopes & Seals" -> animations.filter {
        it.animationType in listOf("wax_seal", "ribbon_untie", "envelope_seal", "card_emerge", "golden_ribbon")
      }
      "Celestial & Glow" -> animations.filter {
        it.animationType in listOf("celestial_stars", "golden_dust", "lantern_illumination", "crystal_chandelier")
      }
      "Scrolls & Heritage" -> animations.filter {
        it.animationType in listOf("parchment_scroll", "royal_scroll", "folding_triptych")
      }
      else -> animations
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(IvoryBg)
      .padding(horizontal = 20.dp, vertical = 16.dp)
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
          text = "Animation Library",
          fontFamily = FontFamily.Serif,
          fontSize = 20.sp,
          color = CharcoalPrimary
        )
        Text(
          text = "${animations.size} design-independent cinematic reveal experiences",
          fontSize = 11.sp,
          color = CharcoalMuted
        )
      }

      OutlinedButton(
        onClick = onCreateAnimationClick,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldDark),
        modifier = Modifier.testTag("create_new_animation_button")
      ) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Custom", fontFamily = FontFamily.Serif, fontSize = 11.sp)
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Category Filter Chips
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      categories.forEach { cat ->
        val isSelected = (selectedCategory == cat)
        val count = if (cat == "All") animations.size else when (cat) {
          "Curtains & Portals" -> animations.count { it.animationType in listOf("velvet_curtain", "silk_drape", "velvet_drapes_tassel", "royal_velvet_curtain", "palace_doors", "jaali_screen", "mughal_arch") }
          "Royal Gates & Crests" -> animations.count { it.animationType in listOf("golden_gates", "royal_gate", "royal_crest_unfold", "royal_carriage") }
          "Florals & Nature" -> animations.count { it.animationType in listOf("botanical_wreath", "floral_bloom", "jasmine_garland", "botanical_reveal", "botanical_shadow") }
          "Envelopes & Seals" -> animations.count { it.animationType in listOf("wax_seal", "ribbon_untie", "envelope_seal", "card_emerge", "golden_ribbon") }
          "Celestial & Glow" -> animations.count { it.animationType in listOf("celestial_stars", "golden_dust", "lantern_illumination", "crystal_chandelier") }
          "Scrolls & Heritage" -> animations.count { it.animationType in listOf("parchment_scroll", "royal_scroll", "folding_triptych") }
          else -> 0
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) GoldPrimary else IvorySurfaceLight)
            .border(
              width = if (isSelected) 1.dp else 0.5.dp,
              color = if (isSelected) GoldDark else IvoryBorderStrong,
              shape = RoundedCornerShape(20.dp)
            )
            .clickable { selectedCategory = cat }
            .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
          Text(
            text = if (cat == "All") "All ($count)" else "$cat ($count)",
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else CharcoalPrimary
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    if (filteredAnimations.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 40.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(Icons.Default.Animation, contentDescription = null, tint = GoldDark, modifier = Modifier.size(36.dp))
          Spacer(modifier = Modifier.height(12.dp))
          Text("No animations in this category", fontFamily = FontFamily.Serif, fontSize = 16.sp, color = CharcoalPrimary)
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        items(filteredAnimations, key = { it.id }) { anim ->
          val isActiveOnCurrent = (currentEvent != null && currentEvent.animationId == anim.id)

          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .border(
                width = if (isActiveOnCurrent) 1.5.dp else 0.5.dp,
                color = if (isActiveOnCurrent) GoldPrimary else IvoryBorder,
                shape = RoundedCornerShape(10.dp)
              ),
            colors = CardDefaults.cardColors(
              containerColor = if (isActiveOnCurrent) IvorySurfaceLight else IvorySurface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isActiveOnCurrent) 2.dp else 1.dp)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    Icons.Default.Animation,
                    contentDescription = null,
                    tint = GoldDark,
                    modifier = Modifier.size(18.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = anim.name,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = CharcoalPrimary
                  )
                }

                if (isActiveOnCurrent) {
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(4.dp))
                      .background(GoldPrimary.copy(alpha = 0.15f))
                      .border(0.5.dp, GoldPrimary, RoundedCornerShape(4.dp))
                      .padding(horizontal = 6.dp, vertical = 2.dp)
                  ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Icon(Icons.Default.Star, contentDescription = null, tint = GoldDark, modifier = Modifier.size(10.dp))
                      Spacer(modifier = Modifier.width(3.dp))
                      Text(
                        text = "Active on Event",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldDark
                      )
                    }
                  }
                } else {
                  IconButton(
                    onClick = {
                      scope.launch {
                        dependentEvents = checkEventsUsingAnimation(anim.id)
                        animPendingDelete = anim
                      }
                    },
                    modifier = Modifier.size(28.dp)
                  ) {
                    Icon(
                      Icons.Default.DeleteOutline,
                      contentDescription = "Delete Animation",
                      tint = CharcoalMuted,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(6.dp))

              Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "Type: ${anim.animationType}",
                  fontSize = 11.sp,
                  color = CharcoalSecondary
                )
                Text(text = "•", fontSize = 11.sp, color = IvoryBorder)
                Text(
                  text = "${anim.motionTimingMs}ms",
                  fontSize = 11.sp,
                  color = CharcoalSecondary
                )
                Text(text = "•", fontSize = 11.sp, color = IvoryBorder)
                Text(
                  text = anim.lightingMood,
                  fontSize = 11.sp,
                  color = CharcoalMuted
                )
              }

              Spacer(modifier = Modifier.height(12.dp))

              // Action Buttons Row: Play Preview & Apply to Event
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
              ) {
                // Apply to Event Button
                if (isActiveOnCurrent) {
                  OutlinedButton(
                    onClick = { /* already active */ },
                    enabled = false,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(32.dp)
                  ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GoldDark, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Current Selection", fontSize = 11.sp, color = GoldDark)
                  }
                } else {
                  OutlinedButton(
                    onClick = {
                      if (allEvents.size <= 1 && currentEvent != null) {
                        onApplyAnimationToEvent?.invoke(anim, currentEvent)
                        Toast.makeText(context, "Applied '${anim.name}' to ${currentEvent.title}", Toast.LENGTH_SHORT).show()
                      } else if (allEvents.isNotEmpty()) {
                        animToApply = anim
                      } else {
                        Toast.makeText(context, "No events created yet. Create an event first.", Toast.LENGTH_SHORT).show()
                      }
                    },
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldDark),
                    modifier = Modifier.height(32.dp)
                  ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = GoldDark, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Apply to Event", fontSize = 11.sp)
                  }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Play Preview Button
                ElevatedButton(
                  onClick = { onPreviewAnimation(anim) },
                  colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = GoldPrimary,
                    contentColor = Color.White
                  ),
                  shape = RoundedCornerShape(6.dp),
                  modifier = Modifier.height(32.dp)
                ) {
                  Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(15.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Play Experience", fontSize = 11.sp)
                }
              }
            }
          }
        }
      }
    }
  }

  // DIALOG: Pick which event to apply this animation to
  animToApply?.let { anim ->
    var chosenEventId by remember { mutableStateOf(currentEvent?.id ?: allEvents.firstOrNull()?.id ?: "") }

    AlertDialog(
      onDismissRequest = { animToApply = null },
      title = {
        Text("Apply Animation to Event", fontFamily = FontFamily.Serif, fontSize = 16.sp, color = CharcoalPrimary)
      },
      text = {
        Column {
          Text(
            text = "Select which event will use the '${anim.name}' reveal animation:",
            fontSize = 12.sp,
            color = CharcoalMuted
          )
          Spacer(modifier = Modifier.height(10.dp))

          allEvents.forEach { evt ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { chosenEventId = evt.id }
                .padding(vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              RadioButton(
                selected = (chosenEventId == evt.id),
                onClick = { chosenEventId = evt.id },
                colors = RadioButtonDefaults.colors(selectedColor = GoldPrimary)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Column {
                Text(evt.title, fontSize = 13.sp, color = CharcoalPrimary, fontWeight = FontWeight.Medium)
                Text(evt.eventType, fontSize = 11.sp, color = CharcoalMuted)
              }
            }
          }
        }
      },
      confirmButton = {
        ElevatedButton(
          onClick = {
            val targetEvent = allEvents.firstOrNull { it.id == chosenEventId } ?: currentEvent
            if (targetEvent != null) {
              onApplyAnimationToEvent?.invoke(anim, targetEvent)
              Toast.makeText(context, "Applied '${anim.name}' to ${targetEvent.title}", Toast.LENGTH_SHORT).show()
            }
            animToApply = null
          },
          colors = ButtonDefaults.elevatedButtonColors(containerColor = GoldPrimary, contentColor = Color.White)
        ) {
          Text("Confirm & Apply", fontSize = 12.sp)
        }
      },
      dismissButton = {
        TextButton(onClick = { animToApply = null }) {
          Text("Cancel", color = CharcoalMuted)
        }
      },
      containerColor = IvorySurface,
      shape = RoundedCornerShape(12.dp)
    )
  }

  // Delete confirmation dialog
  animPendingDelete?.let { anim ->
    AlertDialog(
      onDismissRequest = { animPendingDelete = null },
      title = {
        Text("Delete Animation Experience", fontFamily = FontFamily.Serif, color = CharcoalPrimary)
      },
      text = {
        Column {
          Text(
            text = "Are you sure you want to delete '${anim.name}'?",
            fontSize = 13.sp,
            color = CharcoalPrimary
          )
          if (dependentEvents.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = "Warning: This animation is active on ${dependentEvents.size} event(s): ${dependentEvents.joinToString { it.title }}.",
              fontSize = 12.sp,
              color = Color(0xFFB71C1C)
            )
          }
        }
      },
      confirmButton = {
        TextButton(
          onClick = {
            onDeleteAnimation(anim.id, dependentEvents.isNotEmpty())
            animPendingDelete = null
          }
        ) {
          Text("Delete", color = Color(0xFFB71C1C), fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { animPendingDelete = null }) {
          Text("Cancel", color = CharcoalMuted)
        }
      },
      containerColor = IvorySurface,
      shape = RoundedCornerShape(12.dp)
    )
  }
}
