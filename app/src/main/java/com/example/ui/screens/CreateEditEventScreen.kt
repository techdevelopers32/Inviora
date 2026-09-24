package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.AnimationExperienceEntity
import com.example.data.model.DesignTemplateEntity
import com.example.data.model.EventEntity
import com.example.data.model.EventPage
import com.example.data.model.TextLayerConfig
import com.example.data.model.getDefaultGreetingForPage
import com.example.ui.components.InvitationPageCard
import com.example.ui.components.TextLayerEditorToolbar
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
import java.io.File
import java.util.UUID

@Composable
fun CreateEditEventScreen(
  existingEvent: EventEntity?,
  allDesigns: List<DesignTemplateEntity>,
  allAnimations: List<AnimationExperienceEntity>,
  onSaveEvent: (EventEntity) -> Unit,
  onCancel: () -> Unit,
  onPreviewMaster: (EventEntity, DesignTemplateEntity?, AnimationExperienceEntity?) -> Unit,
  onDeletePage: ((eventId: String, pageId: String) -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  var title by remember { mutableStateOf(existingEvent?.title ?: "") }
  var eventType by remember { mutableStateOf(existingEvent?.eventType ?: "Wedding") }
  var groomName by remember { mutableStateOf(existingEvent?.groomName ?: "") }
  var brideName by remember { mutableStateOf(existingEvent?.brideName ?: "") }
  var hostNames by remember { mutableStateOf(existingEvent?.hostNames ?: "") }
  var selectedAnimationId by remember {
    mutableStateOf(existingEvent?.animationId ?: allAnimations.firstOrNull()?.id ?: "anim_velvet_curtain")
  }

  var pagePendingDelete by remember { mutableStateOf<EventPage?>(null) }

  val isWedding = eventType.equals("Wedding", ignoreCase = true)

  // Multi-Page Invitation List
  val pages = remember {
    mutableStateListOf<EventPage>().apply {
      if (existingEvent != null && existingEvent.getPages().isNotEmpty()) {
        addAll(existingEvent.getPages())
      } else {
        val defaultDesignId = allDesigns.firstOrNull()?.id ?: ""
        if (isWedding) {
          add(
            EventPage(
              id = "page_" + UUID.randomUUID().toString().take(8),
              eventId = existingEvent?.id ?: "",
              pageName = "Mehndi",
              designId = allDesigns.getOrNull(0)?.id ?: defaultDesignId,
              greeting = getDefaultGreetingForPage("Wedding", "Mehndi"),
              eventTitle = com.example.data.model.EventDateParser.formatCoupleNames(groomName, brideName).ifBlank { "Wedding Celebration" },
              date = "December 17, 2026",
              time = "7:00 PM",
              venue = "Royal Garden Pavilions",
              additionalDetails = "Traditional attire requested",
              pageOrder = 0,
              showBismillah = true
            )
          )
          add(
            EventPage(
              id = "page_" + UUID.randomUUID().toString().take(8),
              eventId = existingEvent?.id ?: "",
              pageName = "Baraat",
              designId = allDesigns.getOrNull(1 % allDesigns.size.coerceAtLeast(1))?.id ?: defaultDesignId,
              greeting = getDefaultGreetingForPage("Wedding", "Baraat"),
              eventTitle = com.example.data.model.EventDateParser.formatCoupleNames(groomName, brideName).ifBlank { "Wedding Celebration" },
              date = "December 18, 2026",
              time = "7:30 PM",
              venue = "Grand Palace Ballroom",
              additionalDetails = "Dinner to follow ceremony",
              pageOrder = 1,
              showBismillah = true
            )
          )
          add(
            EventPage(
              id = "page_" + UUID.randomUUID().toString().take(8),
              eventId = existingEvent?.id ?: "",
              pageName = "Walima",
              designId = allDesigns.getOrNull(2 % allDesigns.size.coerceAtLeast(1))?.id ?: defaultDesignId,
              greeting = getDefaultGreetingForPage("Wedding", "Walima"),
              eventTitle = com.example.data.model.EventDateParser.formatCoupleNames(groomName, brideName).ifBlank { "Wedding Celebration" },
              date = "December 20, 2026",
              time = "8:00 PM",
              venue = "The Ritz Banquets",
              additionalDetails = "Valet parking available",
              pageOrder = 2,
              showBismillah = true
            )
          )
        } else {
          add(
            EventPage(
              id = "page_" + UUID.randomUUID().toString().take(8),
              eventId = existingEvent?.id ?: "",
              pageName = "Celebration",
              designId = defaultDesignId,
              greeting = "Cordially invite you to celebrate with us",
              eventTitle = title.ifBlank { "Celebration of Moments" },
              date = "December 18, 2026",
              time = "7:00 PM",
              venue = "Grand Ballroom",
              additionalDetails = "Dinner & Reception to follow",
              pageOrder = 0,
              showBismillah = false
            )
          )
        }
      }
    }
  }

  // Active page being edited in the Page Editor
  var editingPage by remember { mutableStateOf<EventPage?>(null) }
  var singlePagePreview by remember { mutableStateOf<EventPage?>(null) }

  fun buildEvent(): EventEntity {
    val evtId = existingEvent?.id ?: ("evt_" + UUID.randomUUID().toString().take(8))
    val computedTitle = if (title.isNotBlank()) {
      title
    } else if (isWedding && groomName.isNotBlank() && brideName.isNotBlank()) {
      "$groomName & $brideName Wedding"
    } else {
      "Celebration of Moments"
    }

    val updatedPages = pages.mapIndexed { idx, p ->
      p.copy(
        eventId = evtId,
        pageOrder = idx,
        eventTitle = if (p.eventTitle.isBlank()) computedTitle else p.eventTitle
      )
    }

    return EventEntity(
      id = evtId,
      title = computedTitle,
      eventType = eventType,
      groomName = groomName.trim(),
      brideName = brideName.trim(),
      hostNames = hostNames.trim(),
      primaryVenue = updatedPages.firstOrNull()?.venue ?: "",
      primaryDate = updatedPages.firstOrNull()?.date ?: "",
      notes = "",
      designId = updatedPages.firstOrNull()?.designId ?: "",
      animationId = selectedAnimationId,
      subEventsJson = "[]",
      pagesJson = EventPage.listToJson(updatedPages),
      includeTraditionalGreeting = isWedding,
      traditionalBismillahText = "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ",
      traditionalGreetingText = "",
      invitationWording = ""
    )
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(IvoryBg)
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 20.dp, vertical = 16.dp)
      .testTag("create_edit_event_screen")
  ) {
    // Header Bar
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
          onClick = onCancel,
          modifier = Modifier.testTag("event_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = CharcoalPrimary
          )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = if (existingEvent == null) "Create Event" else "Edit Event",
          fontFamily = FontFamily.Serif,
          fontSize = 20.sp,
          color = CharcoalPrimary
        )
      }

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(
          onClick = {
            val evt = buildEvent()
            val firstDesign = allDesigns.find { it.id == evt.designId } ?: allDesigns.firstOrNull()
            val anim = allAnimations.find { it.id == selectedAnimationId } ?: allAnimations.firstOrNull()
            onPreviewMaster(evt, firstDesign, anim)
          },
          colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldDark),
          border = androidx.compose.foundation.BorderStroke(0.8.dp, GoldPrimary),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.testTag("preview_master_button")
        ) {
          Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Master Preview", fontSize = 11.sp, fontFamily = FontFamily.Serif)
        }

        ElevatedButton(
          onClick = { onSaveEvent(buildEvent()) },
          colors = ButtonDefaults.elevatedButtonColors(
            containerColor = GoldPrimary,
            contentColor = Color.White
          ),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.testTag("save_event_button")
        ) {
          Text("Save Event", fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Event Type Selector Chips
    Text(text = "Event Type", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CharcoalPrimary)
    Spacer(modifier = Modifier.height(8.dp))
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      val eventTypes = listOf("Wedding", "Birthday", "Anniversary", "Dinner Party", "Graduation", "Corporate Gala", "Engagement", "Religious")
      items(eventTypes) { type ->
        val isSelected = (type == eventType)
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) GoldPrimary else IvorySurface)
            .border(0.5.dp, if (isSelected) GoldLight else IvoryBorder, RoundedCornerShape(16.dp))
            .clickable { eventType = type }
            .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
          Text(
            text = type,
            fontSize = 11.sp,
            color = if (isSelected) Color.White else CharcoalSecondary,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // WEDDING SPECIFIC: Groom and Bride Names (Mandatory for wedding, displayed on every page)
    if (isWedding) {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = IvorySurface),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.6f))
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Favorite, contentDescription = null, tint = GoldDark, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "COUPLE INFORMATION (Appears on every page)",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.2.sp,
              color = GoldDark
            )
          }
          Spacer(modifier = Modifier.height(10.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text("Groom's Name *", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = CharcoalPrimary)
              Spacer(modifier = Modifier.height(4.dp))
              OutlinedTextField(
                value = groomName,
                onValueChange = {
                  groomName = it
                  if (title.isBlank() || title.endsWith("Wedding")) {
                    title = if (groomName.isNotBlank() && brideName.isNotBlank()) "$groomName & $brideName Wedding" else title
                  }
                },
                placeholder = { Text("e.g. Ali", fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("groom_name_input"),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = GoldPrimary,
                  unfocusedBorderColor = IvoryBorderStrong,
                  focusedContainerColor = IvorySurfaceLight,
                  unfocusedContainerColor = IvorySurfaceLight
                )
              )
            }

            Column(modifier = Modifier.weight(1f)) {
              Text("Bride's Name *", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = CharcoalPrimary)
              Spacer(modifier = Modifier.height(4.dp))
              OutlinedTextField(
                value = brideName,
                onValueChange = {
                  brideName = it
                  if (title.isBlank() || title.endsWith("Wedding")) {
                    title = if (groomName.isNotBlank() && brideName.isNotBlank()) "$groomName & $brideName Wedding" else title
                  }
                },
                placeholder = { Text("e.g. Ayesha", fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("bride_name_input"),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = GoldPrimary,
                  unfocusedBorderColor = IvoryBorderStrong,
                  focusedContainerColor = IvorySurfaceLight,
                  unfocusedContainerColor = IvorySurfaceLight
                )
              )
            }
          }
        }
      }
      Spacer(modifier = Modifier.height(16.dp))
    }

    // Event Title
    Text(text = "Event Title", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CharcoalPrimary)
    Spacer(modifier = Modifier.height(6.dp))
    OutlinedTextField(
      value = title,
      onValueChange = { title = it },
      placeholder = {
        Text(
          if (isWedding) "e.g. Ali & Ayesha Wedding" else "e.g. Sophia's 30th Birthday Celebration",
          fontSize = 13.sp,
          color = CharcoalMuted
        )
      },
      modifier = Modifier
        .fillMaxWidth()
        .testTag("event_title_input"),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = GoldPrimary,
        unfocusedBorderColor = IvoryBorderStrong,
        focusedContainerColor = IvorySurface,
        unfocusedContainerColor = IvorySurface
      ),
      shape = RoundedCornerShape(8.dp)
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Host Names (Optional)
    Text(text = "Host Names (Optional)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CharcoalPrimary)
    Spacer(modifier = Modifier.height(6.dp))
    OutlinedTextField(
      value = hostNames,
      onValueChange = { hostNames = it },
      placeholder = { Text("e.g. Mr. & Mrs. Tariq Khan", fontSize = 13.sp, color = CharcoalMuted) },
      modifier = Modifier.fillMaxWidth(),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = GoldPrimary,
        unfocusedBorderColor = IvoryBorderStrong,
        focusedContainerColor = IvorySurface,
        unfocusedContainerColor = IvorySurface
      ),
      shape = RoundedCornerShape(8.dp)
    )

    Spacer(modifier = Modifier.height(22.dp))

    // SECTION: Opening Animation Selector
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text("OPENING ANIMATION", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = GoldDark)
        Text("Select cinematic reveal experience (20 available)", fontSize = 11.sp, color = CharcoalMuted)
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      items(allAnimations) { anim ->
        val isSelected = (anim.id == selectedAnimationId)
        Card(
          modifier = Modifier
            .width(140.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(if (isSelected) 1.5.dp else 0.5.dp, if (isSelected) GoldPrimary else IvoryBorder, RoundedCornerShape(8.dp))
            .clickable { selectedAnimationId = anim.id },
          colors = CardDefaults.cardColors(containerColor = if (isSelected) IvorySurfaceLight else IvorySurface)
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.Animation, contentDescription = null, tint = GoldDark, modifier = Modifier.size(16.dp))
              if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = GoldDark, modifier = Modifier.size(14.dp))
              }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(anim.name, fontFamily = FontFamily.Serif, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CharcoalPrimary, maxLines = 1)
            Text(anim.lightingMood, fontSize = 10.sp, color = CharcoalMuted)
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(28.dp))
    HorizontalDivider(color = IvoryBorder)
    Spacer(modifier = Modifier.height(20.dp))

    // SECTION: Multi-Page Event System
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text("INVITATION PAGES", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = GoldDark)
        Text("Each page has its own uploaded design, greeting & details", fontSize = 11.sp, color = CharcoalMuted)
      }

      ElevatedButton(
        onClick = {
          val defaultPageName = if (isWedding) {
            when (pages.size) {
              0 -> "Mehndi"
              1 -> "Baraat"
              2 -> "Walima"
              else -> "Ceremony ${pages.size + 1}"
            }
          } else {
            "Page ${pages.size + 1}"
          }
          val newPage = EventPage(
            id = "page_" + UUID.randomUUID().toString().take(8),
            eventId = existingEvent?.id ?: "",
            pageName = defaultPageName,
            designId = allDesigns.getOrNull(pages.size % allDesigns.size.coerceAtLeast(1))?.id ?: "",
            greeting = getDefaultGreetingForPage(eventType, defaultPageName),
            eventTitle = if (isWedding) com.example.data.model.EventDateParser.formatCoupleNames(groomName, brideName).ifBlank { title.ifBlank { "Celebration" } } else title.ifBlank { "Celebration" },
            date = "December 19, 2026",
            time = "8:00 PM",
            venue = "Imperial Banquet Hall",
            additionalDetails = "",
            pageOrder = pages.size,
            showBismillah = isWedding
          )
          pages.add(newPage)
        },
        colors = ButtonDefaults.elevatedButtonColors(
          containerColor = IvorySurfaceLight,
          contentColor = CharcoalPrimary
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.testTag("add_invitation_page_button")
      ) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Add Page", fontSize = 11.sp)
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // List of Pages
    pages.forEachIndexed { index, page ->
      val assignedDesign = allDesigns.find { it.id == page.designId }

      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 6.dp)
          .clip(RoundedCornerShape(10.dp))
          .border(0.8.dp, IvoryBorder, RoundedCornerShape(10.dp))
          .testTag("event_page_card_$index"),
        colors = CardDefaults.cardColors(containerColor = IvorySurface)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Page Design Preview thumbnail
            Box(
              modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(IvorySurfaceLight)
                .border(0.5.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
              contentAlignment = Alignment.Center
            ) {
              if (assignedDesign?.imagePath?.isNotBlank() == true) {
                val file = File(assignedDesign.imagePath)
                AsyncImage(
                  model = if (file.exists()) file else assignedDesign.imagePath,
                  contentDescription = assignedDesign.name,
                  contentScale = ContentScale.Crop,
                  modifier = Modifier.fillMaxSize()
                )
              } else {
                Icon(Icons.Default.Palette, contentDescription = null, tint = GoldDark, modifier = Modifier.size(24.dp))
              }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Page ${index + 1}: ${page.pageName}",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = CharcoalPrimary
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "Design: ${assignedDesign?.name ?: "Default Theme"}",
                fontSize = 11.sp,
                color = GoldDark
              )
              if (page.date.isNotBlank()) {
                Text(
                  text = "${page.date} • ${page.time}",
                  fontSize = 11.sp,
                  color = CharcoalSecondary
                )
              }
              if (page.venue.isNotBlank()) {
                Text(
                  text = page.venue,
                  fontSize = 10.sp,
                  color = CharcoalMuted,
                  maxLines = 1
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Actions: [ Edit & Position ]   [ Preview ]   [ 🗑 Delete ]
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            ElevatedButton(
              onClick = { editingPage = page },
              colors = ButtonDefaults.elevatedButtonColors(
                containerColor = IvorySurfaceLight,
                contentColor = CharcoalPrimary
              ),
              shape = RoundedCornerShape(6.dp),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
              modifier = Modifier
                .height(32.dp)
                .testTag("edit_page_button_$index")
            ) {
              Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(12.dp), tint = GoldDark)
              Spacer(modifier = Modifier.width(4.dp))
              Text("Edit & Position", fontSize = 11.sp)
            }

            ElevatedButton(
              onClick = { singlePagePreview = page },
              colors = ButtonDefaults.elevatedButtonColors(
                containerColor = IvorySurfaceLight,
                contentColor = CharcoalPrimary
              ),
              shape = RoundedCornerShape(6.dp),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
              modifier = Modifier
                .height(32.dp)
                .testTag("preview_page_button_$index")
            ) {
              Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(12.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Preview", fontSize = 11.sp)
            }

            // Third action beside Edit & Preview: [ 🗑 Delete ]
            ElevatedButton(
              onClick = { pagePendingDelete = page },
              colors = ButtonDefaults.elevatedButtonColors(
                containerColor = IvorySurfaceLight,
                contentColor = Color(0xFFC62828)
              ),
              shape = RoundedCornerShape(6.dp),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
              modifier = Modifier
                .height(32.dp)
                .testTag("delete_page_button_$index")
            ) {
              Icon(
                Icons.Default.Delete,
                contentDescription = "Delete Page",
                modifier = Modifier.size(14.dp),
                tint = Color(0xFFC62828)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text("Delete", fontSize = 11.sp, color = Color(0xFFC62828), fontWeight = FontWeight.Medium)
            }
          }
        }
      }
    }

    // Confirmation Dialog for Deleting Page
    if (pagePendingDelete != null) {
      AlertDialog(
        onDismissRequest = { pagePendingDelete = null },
        icon = {
          Icon(
            Icons.Default.DeleteOutline,
            contentDescription = null,
            tint = Color(0xFFC62828),
            modifier = Modifier.size(28.dp)
          )
        },
        title = {
          Text(
            text = "Delete ${pagePendingDelete?.pageName ?: "this page"}?",
            fontFamily = FontFamily.Serif,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = CharcoalPrimary
          )
        },
        text = {
          Column {
            Text(
              text = if (pages.size > 1) {
                "This page may currently be selected for some guests. Deleting it will remove this page from those guests' invitations."
              } else {
                "Deleting this page will reset it to a fresh invitation page and remove it from guests' selections."
              },
              fontSize = 12.sp,
              color = CharcoalMuted,
              lineHeight = 17.sp
            )
          }
        },
        confirmButton = {
          ElevatedButton(
            onClick = {
              val pageIdToDelete = pagePendingDelete!!.id
              pages.removeAll { it.id == pageIdToDelete }
              if (pages.isEmpty()) {
                val defaultDesign = allDesigns.firstOrNull()?.id ?: "design_royal_gold"
                pages.add(
                  EventPage(
                    id = UUID.randomUUID().toString(),
                    eventId = existingEvent?.id ?: "",
                    pageName = if (isWedding) "Ceremony" else "Invitation",
                    designId = defaultDesign,
                    pageOrder = 0,
                    showBismillah = isWedding
                  )
                )
              } else {
                val reordered = pages.mapIndexed { idx, p -> p.copy(pageOrder = idx) }
                pages.clear()
                pages.addAll(reordered)
              }
              if (existingEvent != null) {
                onDeletePage?.invoke(existingEvent.id, pageIdToDelete)
              }
              pagePendingDelete = null
            },
            colors = ButtonDefaults.elevatedButtonColors(
              containerColor = Color(0xFFC62828),
              contentColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.testTag("confirm_delete_page_button")
          ) {
            Text("Delete Page", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          TextButton(
            onClick = { pagePendingDelete = null },
            modifier = Modifier.testTag("cancel_delete_page_button")
          ) {
            Text("Cancel", color = CharcoalMuted)
          }
        },
        containerColor = IvorySurface,
        shape = RoundedCornerShape(12.dp)
      )
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Bottom Master Preview & Save Buttons
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      OutlinedButton(
        onClick = {
          val evt = buildEvent()
          val firstDesign = allDesigns.find { it.id == evt.designId } ?: allDesigns.firstOrNull()
          val anim = allAnimations.find { it.id == selectedAnimationId } ?: allAnimations.firstOrNull()
          onPreviewMaster(evt, firstDesign, anim)
        },
        modifier = Modifier.weight(1f),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary),
        shape = RoundedCornerShape(8.dp)
      ) {
        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Master Preview", fontFamily = FontFamily.Serif, fontSize = 13.sp)
      }

      ElevatedButton(
        onClick = { onSaveEvent(buildEvent()) },
        modifier = Modifier.weight(1f),
        colors = ButtonDefaults.elevatedButtonColors(containerColor = GoldPrimary, contentColor = Color.White),
        shape = RoundedCornerShape(8.dp)
      ) {
        Text("Save Event", fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
      }
    }

    Spacer(modifier = Modifier.height(40.dp))
  }

  // -------------------------------------------------------------
  // DIALOG: Page Editor (Content, Bismillah, Greetings & Visual Positioning)
  // -------------------------------------------------------------
  editingPage?.let { curPage ->
    val tempEvent = buildEvent()
    PageEditorDialog(
      page = curPage,
      event = tempEvent,
      allDesigns = allDesigns,
      onDismiss = { editingPage = null },
      onSave = { updatedPage ->
        val idx = pages.indexOfFirst { it.id == updatedPage.id }
        if (idx != -1) {
          pages[idx] = updatedPage
        }
        editingPage = null
      }
    )
  }

  // -------------------------------------------------------------
  // DIALOG: Single Page Live Preview
  // -------------------------------------------------------------
  singlePagePreview?.let { page ->
    val tempEvent = buildEvent()
    Dialog(
      onDismissRequest = { singlePagePreview = null },
      properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black.copy(alpha = 0.85f))
          .padding(16.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
          ) {
            IconButton(onClick = { singlePagePreview = null }) {
              Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
          }

          val dsg = allDesigns.find { it.id == page.designId }
          InvitationPageCard(
            page = page,
            design = dsg,
            event = tempEvent,
            modifier = Modifier.padding(bottom = 20.dp)
          )
        }
      }
    }
  }
}

// -------------------------------------------------------------
// COMPOSABLE: Page Editor Dialog with Visual Drag & Text Positioning
// -------------------------------------------------------------
@Composable
private fun PageEditorDialog(
  page: EventPage,
  event: EventEntity,
  allDesigns: List<DesignTemplateEntity>,
  onDismiss: () -> Unit,
  onSave: (EventPage) -> Unit
) {
  var selectedTab by remember { mutableIntStateOf(0) } // 0: Content & Details, 1: Visual Card & Layout

  var pageName by remember { mutableStateOf(page.pageName) }
  var selectedDesignId by remember { mutableStateOf(page.designId) }
  var greeting by remember { mutableStateOf(page.greeting) }
  var date by remember { mutableStateOf(page.date) }
  var time by remember { mutableStateOf(page.time) }
  var venue by remember { mutableStateOf(page.venue) }
  var additionalDetails by remember { mutableStateOf(page.additionalDetails) }

  // Bismillah controls
  var showBismillah by remember { mutableStateOf(page.showBismillah) }
  var bismillahArabic by remember { mutableStateOf(page.bismillahArabic) }
  var bismillahEnglish by remember { mutableStateOf(page.bismillahEnglish) }

  // Text layers state for visual positioning editor
  var textLayers by remember {
    mutableStateOf(TextLayerConfig.buildResolvedLayers(event, page))
  }
  var selectedLayerId by remember {
    mutableStateOf(textLayers.firstOrNull { it.isVisible }?.id ?: "bismillah_arabic")
  }

  var isChoosingDesign by remember { mutableStateOf(false) }

  val currentDesign = allDesigns.find { it.id == selectedDesignId }
  val isWedding = event.eventType.equals("Wedding", ignoreCase = true)

  fun constructPage(): EventPage {
    val tempPage = page.copy(
      pageName = pageName.trim(),
      designId = selectedDesignId,
      greeting = greeting.trim(),
      date = date.trim(),
      time = time.trim(),
      venue = venue.trim(),
      additionalDetails = additionalDetails.trim(),
      showBismillah = showBismillah,
      bismillahArabic = bismillahArabic.trim(),
      bismillahEnglish = bismillahEnglish.trim()
    )
    val syncedLayers = TextLayerConfig.buildResolvedLayers(event, tempPage)
    val currentLayoutMap = textLayers.associateBy { it.id }
    val mergedLayers = syncedLayers.map { synced ->
      val current = currentLayoutMap[synced.id]
      if (current != null) {
        synced.copy(
          xPercent = current.xPercent,
          yPercent = current.yPercent,
          fontSizeSp = current.fontSizeSp,
          fontWeight = current.fontWeight,
          fontStyle = current.fontStyle,
          fontFamily = current.fontFamily,
          colorHex = current.colorHex,
          alignment = current.alignment,
          isVisible = current.isVisible
        )
      } else {
        synced
      }
    }
    return tempPage.copy(
      textLayoutJson = TextLayerConfig.listToJson(mergedLayers)
    )
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(IvoryBg)
        .padding(16.dp)
        .testTag("page_editor_dialog")
    ) {
      Column(
        modifier = Modifier.fillMaxSize()
      ) {
        // Dialog Top Header Bar
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Edit Page: $pageName",
            fontFamily = FontFamily.Serif,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = CharcoalPrimary
          )

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onDismiss) {
              Text("Cancel", color = CharcoalMuted)
            }
            ElevatedButton(
              onClick = { onSave(constructPage()) },
              colors = ButtonDefaults.elevatedButtonColors(containerColor = GoldPrimary, contentColor = Color.White),
              shape = RoundedCornerShape(6.dp),
              modifier = Modifier.testTag("save_page_edits_button")
            ) {
              Text("Apply", fontSize = 12.sp)
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tabs: Content & Details vs Visual Layout & Text Placement
        TabRow(
          selectedTabIndex = selectedTab,
          containerColor = IvorySurface,
          contentColor = GoldDark,
          indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
              Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
              color = GoldPrimary
            )
          }
        ) {
          Tab(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            text = { Text("Content & Details", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
            icon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp)) }
          )
          Tab(
            selected = selectedTab == 1,
            onClick = {
              // Sync updated text into layer states before viewing
              textLayers = TextLayerConfig.buildResolvedLayers(event, constructPage())
              selectedTab = 1
            },
            text = { Text("Visual Layout & Text Placement", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
            icon = { Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp)) }
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // TAB 0: Content & Details Form
        if (selectedTab == 0) {
          Column(
            modifier = Modifier
              .weight(1f)
              .verticalScroll(rememberScrollState())
          ) {
            // Page Name
            Text("Page / Ceremony Name", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = CharcoalPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
              value = pageName,
              onValueChange = { pageName = it },
              placeholder = { Text("e.g. Mehndi, Baraat, Walima, Birthday Dinner") },
              modifier = Modifier.fillMaxWidth().testTag("page_name_input"),
              singleLine = true,
              shape = RoundedCornerShape(8.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GoldPrimary,
                unfocusedBorderColor = IvoryBorderStrong,
                focusedContainerColor = IvorySurface,
                unfocusedContainerColor = IvorySurface
              )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Assigned Invitation Artwork
            Text("Assigned Invitation Artwork", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = CharcoalPrimary)
            Spacer(modifier = Modifier.height(6.dp))
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(0.5.dp, GoldPrimary, RoundedCornerShape(8.dp)),
              colors = CardDefaults.cardColors(containerColor = IvorySurface)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  val context = LocalContext.current
                  val hasCurrentImg = currentDesign != null && (currentDesign.imagePath.isNotBlank() || currentDesign.referenceDrawable.isNotBlank())
                  Box(
                    modifier = Modifier
                      .size(48.dp)
                      .clip(RoundedCornerShape(6.dp))
                      .background(IvorySurfaceLight),
                    contentAlignment = Alignment.Center
                  ) {
                    if (hasCurrentImg) {
                      val f = File(currentDesign!!.imagePath)
                      val drawableResId = remember(currentDesign.referenceDrawable) {
                        if (currentDesign.referenceDrawable.isNotBlank()) {
                          context.resources.getIdentifier(currentDesign.referenceDrawable, "drawable", context.packageName)
                        } else 0
                      }
                      val model: Any = when {
                        f.exists() -> f
                        currentDesign.imagePath.isNotBlank() -> currentDesign.imagePath
                        drawableResId != 0 -> drawableResId
                        else -> currentDesign.imagePath
                      }
                      AsyncImage(
                        model = model,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                      )
                    } else {
                      Icon(Icons.Default.Palette, contentDescription = null, tint = GoldDark)
                    }
                  }
                  Spacer(modifier = Modifier.width(12.dp))
                  Column {
                    Text(currentDesign?.name ?: "Select a Design", fontFamily = FontFamily.Serif, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = CharcoalPrimary)
                    Text(if (currentDesign?.imagePath?.isNotBlank() == true) "Custom uploaded design" else "Theme palette", fontSize = 11.sp, color = CharcoalMuted)
                  }
                }

                OutlinedButton(
                  onClick = { isChoosingDesign = true },
                  shape = RoundedCornerShape(6.dp),
                  modifier = Modifier.testTag("choose_design_button")
                ) {
                  Text("Choose Design", fontSize = 11.sp, color = GoldDark)
                }
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // BISMILLAH SECTION (Arabic & English Translation)
            Card(
              modifier = Modifier.fillMaxWidth(),
              colors = CardDefaults.cardColors(containerColor = IvorySurface),
              shape = RoundedCornerShape(8.dp),
              border = androidx.compose.foundation.BorderStroke(0.8.dp, IvoryBorderStrong)
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column {
                    Text("BISMILLAH HEADER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldDark)
                    Text("Arabic text with English translation underneath", fontSize = 10.sp, color = CharcoalMuted)
                  }
                  Switch(
                    checked = showBismillah,
                    onCheckedChange = { showBismillah = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = GoldPrimary)
                  )
                }

                if (showBismillah) {
                  Spacer(modifier = Modifier.height(10.dp))
                  Text("Arabic Text", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = CharcoalPrimary)
                  Spacer(modifier = Modifier.height(4.dp))
                  OutlinedTextField(
                    value = bismillahArabic,
                    onValueChange = { bismillahArabic = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("bismillah_arabic_input"),
                    shape = RoundedCornerShape(8.dp)
                  )

                  Spacer(modifier = Modifier.height(8.dp))
                  Text("English Translation (Directly Underneath)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = CharcoalPrimary)
                  Spacer(modifier = Modifier.height(4.dp))
                  OutlinedTextField(
                    value = bismillahEnglish,
                    onValueChange = { bismillahEnglish = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("bismillah_english_input"),
                    shape = RoundedCornerShape(8.dp)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Greeting / Invitation Wording (Editable, Replaceable, Removable)
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("Greeting / Invitation Wording", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = CharcoalPrimary)
              if (greeting.isNotBlank()) {
                Text(
                  text = "Clear",
                  fontSize = 11.sp,
                  color = Color(0xFFB71C1C),
                  modifier = Modifier.clickable { greeting = "" }
                )
              }
            }
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
              value = greeting,
              onValueChange = { greeting = it },
              placeholder = { Text("Enter greeting message (or leave empty)") },
              modifier = Modifier.fillMaxWidth().testTag("page_greeting_input"),
              minLines = 2,
              shape = RoundedCornerShape(8.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GoldPrimary,
                unfocusedBorderColor = IvoryBorderStrong,
                focusedContainerColor = IvorySurface,
                unfocusedContainerColor = IvorySurface
              )
            )

            // Greeting Quick Presets (Event-Specific: Mehndi, Baraat, Walima, etc.)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              val presets = if (isWedding) {
                listOf(
                  "Mehndi Greeting" to getDefaultGreetingForPage("Wedding", "Mehndi"),
                  "Baraat Greeting" to getDefaultGreetingForPage("Wedding", "Baraat"),
                  "Walima Greeting" to getDefaultGreetingForPage("Wedding", "Walima"),
                  "Ceremony" to "We request the honor of your presence to celebrate the wedding ceremony of"
                )
              } else {
                listOf(
                  "Birthday" to "Join us to celebrate another wonderful year of joy and memories",
                  "Anniversary" to "Cordially invite you to celebrate the milestone anniversary of",
                  "Dinner" to "Delighted to invite you to an evening of celebration and fine dining"
                )
              }
              presets.forEach { (label, text) ->
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(IvorySurface)
                    .border(0.5.dp, IvoryBorder, RoundedCornerShape(10.dp))
                    .clickable { greeting = text }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                  Text(label, fontSize = 9.sp, color = CharcoalSecondary)
                }
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Date & Time
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              Column(modifier = Modifier.weight(1f)) {
                Text("Date", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = CharcoalPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                  value = date,
                  onValueChange = { date = it },
                  placeholder = { Text("e.g. Dec 18, 2026") },
                  modifier = Modifier.fillMaxWidth(),
                  singleLine = true,
                  shape = RoundedCornerShape(8.dp),
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = IvoryBorderStrong,
                    focusedContainerColor = IvorySurface,
                    unfocusedContainerColor = IvorySurface
                  )
                )
              }

              Column(modifier = Modifier.weight(1f)) {
                Text("Time", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = CharcoalPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                  value = time,
                  onValueChange = { time = it },
                  placeholder = { Text("e.g. 7:00 PM") },
                  modifier = Modifier.fillMaxWidth(),
                  singleLine = true,
                  shape = RoundedCornerShape(8.dp),
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = IvoryBorderStrong,
                    focusedContainerColor = IvorySurface,
                    unfocusedContainerColor = IvorySurface
                  )
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Venue
            Text("Venue", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = CharcoalPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
              value = venue,
              onValueChange = { venue = it },
              placeholder = { Text("e.g. The Rosewood Ballroom, 124 Grand Ave") },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(8.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GoldPrimary,
                unfocusedBorderColor = IvoryBorderStrong,
                focusedContainerColor = IvorySurface,
                unfocusedContainerColor = IvorySurface
              )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Additional Details
            Text("Additional Details (Optional)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = CharcoalPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
              value = additionalDetails,
              onValueChange = { additionalDetails = it },
              placeholder = { Text("e.g. Dress code: Formal Black Tie. Valet parking at East Gate.") },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(8.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GoldPrimary,
                unfocusedBorderColor = IvoryBorderStrong,
                focusedContainerColor = IvorySurface,
                unfocusedContainerColor = IvorySurface
              )
            )

            Spacer(modifier = Modifier.height(30.dp))
          }
        }

        // TAB 1: Visual Layout & Text Placement
        if (selectedTab == 1) {
          Column(
            modifier = Modifier
              .weight(1f)
              .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "Drag any element to move it on top of the uploaded artwork. The artwork is 100% visible.",
              fontSize = 11.sp,
              color = CharcoalMuted,
              modifier = Modifier.padding(bottom = 8.dp)
            )

            // The Interactive Editable Invitation Card
            val activePagePreview = constructPage()
            InvitationPageCard(
              page = activePagePreview,
              design = currentDesign,
              event = event,
              isEditable = true,
              selectedLayerId = selectedLayerId,
              onSelectLayer = { selectedLayerId = it },
              onUpdateLayer = { updatedLayer ->
                textLayers = textLayers.map { if (it.id == updatedLayer.id) updatedLayer else it }
              },
              modifier = Modifier.fillMaxWidth(0.92f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Active Layer Toolbar: Resize font, Align, Color, Position Nudge, Hide/Show
            val currentActiveLayer = textLayers.find { it.id == selectedLayerId } ?: textLayers.firstOrNull()
            if (currentActiveLayer != null) {
              TextLayerEditorToolbar(
                selectedLayer = currentActiveLayer,
                allLayers = textLayers,
                onSelectLayer = { selectedLayerId = it },
                onUpdateLayer = { updated ->
                  textLayers = textLayers.map { if (it.id == updated.id) updated else it }
                }
              )
            }

            Spacer(modifier = Modifier.height(20.dp))
          }
        }
      }
    }
  }

  // Design Picker Modal
  if (isChoosingDesign) {
    Dialog(onDismissRequest = { isChoosingDesign = false }) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = IvorySurface),
        shape = RoundedCornerShape(12.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Select Design from Library", fontFamily = FontFamily.Serif, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = CharcoalPrimary)
            IconButton(onClick = { isChoosingDesign = false }) {
              Icon(Icons.Default.Close, contentDescription = null, tint = CharcoalMuted)
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            items(allDesigns) { design ->
              val isSel = (design.id == selectedDesignId)
              Card(
                modifier = Modifier
                  .width(130.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .border(if (isSel) 1.5.dp else 0.5.dp, if (isSel) GoldPrimary else IvoryBorder, RoundedCornerShape(8.dp))
                  .clickable {
                    selectedDesignId = design.id
                    isChoosingDesign = false
                  },
                colors = CardDefaults.cardColors(containerColor = IvorySurfaceLight)
              ) {
                Column(
                  modifier = Modifier.padding(8.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  val ctx = LocalContext.current
                  val hasDesignImg = design.imagePath.isNotBlank() || design.referenceDrawable.isNotBlank()
                  Box(
                    modifier = Modifier
                      .size(90.dp)
                      .clip(RoundedCornerShape(6.dp))
                      .background(IvorySurface),
                    contentAlignment = Alignment.Center
                  ) {
                    if (hasDesignImg) {
                      val f = File(design.imagePath)
                      val drawableResId = remember(design.referenceDrawable) {
                        if (design.referenceDrawable.isNotBlank()) {
                          ctx.resources.getIdentifier(design.referenceDrawable, "drawable", ctx.packageName)
                        } else 0
                      }
                      val model: Any = when {
                        f.exists() -> f
                        design.imagePath.isNotBlank() -> design.imagePath
                        drawableResId != 0 -> drawableResId
                        else -> design.imagePath
                      }
                      AsyncImage(
                        model = model,
                        contentDescription = design.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                      )
                    } else {
                      Icon(Icons.Default.Palette, contentDescription = null, tint = GoldDark)
                    }
                  }
                  Spacer(modifier = Modifier.height(6.dp))
                  Text(design.name, fontFamily = FontFamily.Serif, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = CharcoalPrimary, maxLines = 1)
                }
              }
            }
          }
        }
      }
    }
  }
}
