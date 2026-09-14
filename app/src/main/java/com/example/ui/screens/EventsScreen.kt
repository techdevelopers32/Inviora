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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun EventsScreen(
  events: List<EventEntity>,
  currentEventId: String?,
  onSelectEvent: (EventEntity) -> Unit,
  onCreateEventClick: () -> Unit,
  onEditEventClick: (EventEntity) -> Unit,
  onDeleteEventClick: (EventEntity) -> Unit,
  onPreviewEventClick: (EventEntity) -> Unit,
  modifier: Modifier = Modifier
) {
  var eventToDelete by remember { mutableStateOf<EventEntity?>(null) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(IvoryBg)
      .padding(horizontal = 20.dp, vertical = 16.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "EVENTS",
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          letterSpacing = 2.sp,
          color = GoldDark
        )
        Text(
          text = "All Celebrations",
          fontFamily = FontFamily.Serif,
          fontSize = 20.sp,
          color = CharcoalPrimary
        )
      }

      ElevatedButton(
        onClick = onCreateEventClick,
        colors = ButtonDefaults.elevatedButtonColors(
          containerColor = GoldPrimary,
          contentColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.testTag("events_create_button")
      ) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "New Event",
          fontFamily = FontFamily.Serif,
          fontWeight = FontWeight.SemiBold,
          fontSize = 13.sp
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    if (events.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 40.dp),
        contentAlignment = Alignment.Center
      ) {
        Text("No events created yet. Tap 'New Event' to begin.", color = CharcoalMuted, fontSize = 13.sp)
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        items(events, key = { it.id }) { event ->
          val isCurrent = event.id == currentEventId

          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .border(
                width = if (isCurrent) 1.2.dp else 0.5.dp,
                color = if (isCurrent) GoldPrimary else IvoryBorder,
                shape = RoundedCornerShape(12.dp)
              )
              .clickable { onSelectEvent(event) },
            colors = CardDefaults.cardColors(
              containerColor = if (isCurrent) IvorySurfaceLight else IvorySurface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
          ) {
            Column(modifier = Modifier.padding(18.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                if (isCurrent) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Default.CheckCircle,
                      contentDescription = null,
                      tint = Color(0xFF2E6F40),
                      modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = "ACTIVE EVENT",
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold,
                      letterSpacing = 1.5.sp,
                      color = Color(0xFF2E6F40)
                    )
                  }
                } else {
                  Text(
                    text = event.eventType.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = GoldDark
                  )
                }

                Row {
                  IconButton(
                    onClick = { onEditEventClick(event) },
                    modifier = Modifier.size(28.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.Edit,
                      contentDescription = "Edit Event",
                      tint = CharcoalMuted,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                  Spacer(modifier = Modifier.width(4.dp))
                  IconButton(
                    onClick = { eventToDelete = event },
                    modifier = Modifier.size(28.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.DeleteOutline,
                      contentDescription = "Delete Event",
                      tint = CharcoalMuted,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              Text(
                text = event.title,
                fontFamily = FontFamily.Serif,
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                color = CharcoalPrimary
              )

              if (event.primaryDate.isNotBlank() || event.primaryVenue.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = listOf(event.primaryDate, event.primaryVenue)
                    .filter { it.isNotBlank() }
                    .joinToString(" • "),
                  fontSize = 11.sp,
                  color = CharcoalSecondary
                )
              }

              val subEventCount = event.getSubEvents().size
              if (subEventCount > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "$subEventCount Sub-event${if (subEventCount > 1) "s" else ""}",
                  fontSize = 11.sp,
                  color = GoldDark,
                  fontWeight = FontWeight.Medium
                )
              }

              Spacer(modifier = Modifier.height(14.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                OutlinedButton(
                  onClick = { onPreviewEventClick(event) },
                  shape = RoundedCornerShape(6.dp),
                  colors = ButtonDefaults.outlinedButtonColors(contentColor = CharcoalPrimary),
                  modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                ) {
                  Icon(Icons.Default.Visibility, contentDescription = null, tint = GoldDark, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Preview", fontSize = 11.sp)
                }

                if (!isCurrent) {
                  ElevatedButton(
                    onClick = { onSelectEvent(event) },
                    colors = ButtonDefaults.elevatedButtonColors(
                      containerColor = GoldPrimary,
                      contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                      .weight(1.2f)
                      .height(38.dp)
                  ) {
                    Text("Set as Active", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  // Confirm delete dialog
  eventToDelete?.let { evt ->
    AlertDialog(
      onDismissRequest = { eventToDelete = null },
      title = {
        Text("Delete Event", fontFamily = FontFamily.Serif, color = CharcoalPrimary)
      },
      text = {
        Text(
          "Are you sure you want to delete '${evt.title}'? All associated guest links will also be removed.",
          color = CharcoalSecondary,
          fontSize = 13.sp
        )
      },
      confirmButton = {
        TextButton(
          onClick = {
            onDeleteEventClick(evt)
            eventToDelete = null
          }
        ) {
          Text("Delete", color = Color(0xFFB71C1C), fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { eventToDelete = null }) {
          Text("Cancel", color = CharcoalMuted)
        }
      },
      containerColor = IvorySurface,
      shape = RoundedCornerShape(12.dp)
    )
  }
}
