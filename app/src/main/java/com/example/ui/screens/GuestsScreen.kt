package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import com.example.data.model.getDefaultGreetingForPage
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.DesignTemplateEntity
import com.example.data.model.EventEntity
import com.example.data.model.EventPage
import com.example.data.model.GuestEntity
import com.example.data.model.PageGuestStyle
import com.example.ui.components.InvitationPageCard
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

@Composable
fun GuestsScreen(
  currentEvent: EventEntity?,
  allEvents: List<EventEntity>,
  guests: List<GuestEntity>,
  currentDesign: DesignTemplateEntity? = null,
  allDesigns: List<DesignTemplateEntity> = emptyList(),
  onSelectEvent: (EventEntity) -> Unit,
  onAddGuest: (GuestEntity) -> Unit,
  onUpdateEvent: ((EventEntity) -> Unit)? = null,
  onAddBulkGuests: suspend (List<GuestEntity>) -> Unit,
  onRegenerateToken: (String) -> Unit,
  onDeleteGuest: (GuestEntity) -> Unit,
  onTestGuestExperience: (GuestEntity, EventEntity) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  var isAddingGuest by remember { mutableStateOf(false) }
  var editingGuest by remember { mutableStateOf<GuestEntity?>(null) }
  var isBatchGenerating by remember { mutableStateOf(false) }
  var isBulkWorking by remember { mutableStateOf(false) }
  var searchQuery by remember { mutableStateOf("") }

  val eventPages = currentEvent?.getPages() ?: emptyList()

  // Filter guests for current event and search term
  val currentEventGuests = remember(guests, currentEvent, searchQuery) {
    if (currentEvent == null) emptyList()
    else {
      guests.filter { it.eventId == currentEvent.id }
        .filter {
          searchQuery.isBlank() ||
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.uniqueToken.contains(searchQuery, ignoreCase = true)
        }
    }
  }

  fun generateSecureToken(): String {
    val allowed = "23456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
    return (1..6).map { allowed.random() }.joinToString("")
  }

  fun copyToClipboard(link: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Inviora Invitation Link", link)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Link copied to clipboard", Toast.LENGTH_SHORT).show()
  }

  fun shareViaWhatsApp(guest: GuestEntity, event: EventEntity) {
    val link = "https://yourusername.github.io/inviora/invite/${guest.uniqueToken}"
    val message = "Dear ${guest.name},\n\nWe are delighted to invite you to celebrate ${event.title}.\n\nPlease open your invitation here:\n$link"
    val intent = Intent(Intent.ACTION_SEND).apply {
      type = "text/plain"
      putExtra(Intent.EXTRA_TEXT, message)
    }
    context.startActivity(Intent.createChooser(intent, "Share Invitation via"))
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(IvoryBg)
      .padding(horizontal = 20.dp, vertical = 16.dp)
      .testTag("guests_screen")
  ) {
    // Header Row with Clear "+ Add Guest" Button
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "GUEST DIRECTORY",
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          letterSpacing = 2.sp,
          color = GoldDark
        )
        Text(
          text = "Manage Guest Links",
          fontFamily = FontFamily.Serif,
          fontSize = 20.sp,
          color = CharcoalPrimary
        )
      }

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        // [ + Add Guest ] Button
        ElevatedButton(
          onClick = {
            editingGuest = null
            isAddingGuest = true
          },
          enabled = currentEvent != null,
          colors = ButtonDefaults.elevatedButtonColors(
            containerColor = GoldPrimary,
            contentColor = Color.White
          ),
          shape = RoundedCornerShape(8.dp),
          contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
          modifier = Modifier
            .height(38.dp)
            .testTag("add_guest_button")
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Add Guest",
            fontFamily = FontFamily.Serif,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
          )
        }

        // [ Scale Test ] Button
        OutlinedButton(
          onClick = { isBatchGenerating = true },
          enabled = currentEvent != null && !isBulkWorking,
          colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldDark),
          border = androidx.compose.foundation.BorderStroke(0.8.dp, GoldPrimary),
          shape = RoundedCornerShape(8.dp),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
          modifier = Modifier
            .height(38.dp)
            .testTag("demo_scale_test_button")
        ) {
          Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Scale Test", fontSize = 11.sp)
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Event Selector Chips
    if (allEvents.isNotEmpty()) {
      Text("Select Event:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = CharcoalMuted)
      Spacer(modifier = Modifier.height(6.dp))
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        items(allEvents) { evt ->
          val isSelected = (evt.id == currentEvent?.id)
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(16.dp))
              .background(if (isSelected) GoldPrimary else IvorySurface)
              .border(0.5.dp, if (isSelected) GoldLight else IvoryBorder, RoundedCornerShape(16.dp))
              .clickable { onSelectEvent(evt) }
              .padding(horizontal = 14.dp, vertical = 6.dp)
          ) {
            Text(
              text = evt.title,
              fontSize = 11.sp,
              color = if (isSelected) Color.White else CharcoalPrimary,
              fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Search and Summary Bar
    if (currentEvent != null) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text("Search by name or token...", fontSize = 12.sp, color = CharcoalMuted) },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CharcoalMuted, modifier = Modifier.size(16.dp)) },
          singleLine = true,
          modifier = Modifier
            .weight(1f)
            .height(46.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GoldPrimary,
            unfocusedBorderColor = IvoryBorder,
            focusedContainerColor = IvorySurface,
            unfocusedContainerColor = IvorySurface
          ),
          shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(IvorySurfaceLight)
            .border(0.5.dp, IvoryBorderStrong, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
          Text(
            text = "${currentEventGuests.size} Guests",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = GoldDark
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Guests List
    if (currentEvent == null) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Please create or select an event first.", fontFamily = FontFamily.Serif, color = CharcoalMuted)
      }
    } else if (currentEventGuests.isEmpty()) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(Icons.Default.Group, contentDescription = null, tint = GoldDark, modifier = Modifier.size(40.dp))
          Spacer(modifier = Modifier.height(12.dp))
          Text("No guests added yet", fontFamily = FontFamily.Serif, fontSize = 16.sp, color = CharcoalPrimary)
          Spacer(modifier = Modifier.height(6.dp))
          Text("Use '+ Add Guest' above or run a Scale Test", fontSize = 12.sp, color = CharcoalMuted)
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(currentEventGuests, key = { it.id }) { guest ->
          val guestLink = "https://yourusername.github.io/inviora/invite/${guest.uniqueToken}"
          val invitedPages = guest.getInvitedPages(eventPages)

          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .border(0.5.dp, IvoryBorder, RoundedCornerShape(8.dp))
              .testTag("guest_card_${guest.uniqueToken}"),
            colors = CardDefaults.cardColors(containerColor = IvorySurface)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = guest.name,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = CharcoalPrimary
                  )
                  if (guest.phone.isNotBlank()) {
                    Text(text = guest.phone, fontSize = 11.sp, color = CharcoalMuted)
                  }
                  Text(
                    text = "Style: ${guest.guestNameFontSize}sp • Color: ${guest.guestNameColorHex} • Pos: ${((guest.guestNameYPercent) * 100).toInt()}%",
                    fontSize = 10.sp,
                    color = GoldDark
                  )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                  // Edit & Style Guest Button
                  IconButton(
                    onClick = {
                      editingGuest = guest
                      isAddingGuest = true
                    },
                    modifier = Modifier.size(28.dp).testTag("edit_guest_button")
                  ) {
                    Icon(Icons.Default.Tune, contentDescription = "Edit & Style Guest", tint = GoldDark, modifier = Modifier.size(16.dp))
                  }

                  IconButton(
                    onClick = { onDeleteGuest(guest) },
                    modifier = Modifier.size(28.dp).testTag("delete_guest_button")
                  ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Guest", tint = CharcoalMuted, modifier = Modifier.size(16.dp))
                  }
                }
              }

              Spacer(modifier = Modifier.height(8.dp))

              // Invited Pages Badges
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Text("Invited to: ", fontSize = 10.sp, color = CharcoalMuted)
                if (invitedPages.isEmpty()) {
                  Text("All Pages (Default)", fontSize = 10.sp, color = GoldDark)
                } else {
                  invitedPages.forEach { p ->
                    Box(
                      modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(GoldPrimary.copy(alpha = 0.12f))
                        .border(0.5.dp, GoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                      Text(p.pageName, fontSize = 9.sp, color = GoldDark, fontWeight = FontWeight.Medium)
                    }
                  }
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              // Link Bar
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(6.dp))
                  .background(IvorySurfaceLight)
                  .border(0.5.dp, IvoryBorderStrong, RoundedCornerShape(6.dp))
                  .padding(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween,
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Link, contentDescription = null, tint = GoldDark, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(guestLink, fontSize = 11.sp, color = GoldDark, maxLines = 1)
                  }

                  Row {
                    IconButton(
                      onClick = { copyToClipboard(guestLink) },
                      modifier = Modifier.size(26.dp).testTag("copy_link_button")
                    ) {
                      Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = CharcoalMuted, modifier = Modifier.size(13.dp))
                    }

                    IconButton(
                      onClick = { onRegenerateToken(guest.id) },
                      modifier = Modifier.size(26.dp).testTag("regenerate_link_button")
                    ) {
                      Icon(Icons.Default.Refresh, contentDescription = "Regenerate Token", tint = CharcoalMuted, modifier = Modifier.size(13.dp))
                    }
                  }
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              // Bottom Action Buttons
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
              ) {
                // WhatsApp Share
                OutlinedButton(
                  onClick = { shareViaWhatsApp(guest, currentEvent) },
                  colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2E7D32)),
                  border = androidx.compose.foundation.BorderStroke(0.6.dp, Color(0xFF2E7D32)),
                  shape = RoundedCornerShape(6.dp),
                  modifier = Modifier.height(30.dp).testTag("whatsapp_share_button")
                ) {
                  Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(12.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Share", fontSize = 10.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Preview Guest View
                ElevatedButton(
                  onClick = { onTestGuestExperience(guest, currentEvent) },
                  colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = IvorySurfaceLight,
                    contentColor = CharcoalPrimary
                  ),
                  shape = RoundedCornerShape(6.dp),
                  modifier = Modifier.height(30.dp).testTag("preview_guest_experience_button")
                ) {
                  Icon(Icons.Default.Visibility, contentDescription = null, tint = GoldDark, modifier = Modifier.size(12.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Preview Guest Experience", fontSize = 10.sp)
                }
              }
            }
          }
        }
      }
    }
  }

  // -------------------------------------------------------------
  // DIALOG: Add / Edit Guest with Visual Draggable Placement & Personalization
  // -------------------------------------------------------------
  if (isAddingGuest) {
    val isEditing = (editingGuest != null)

    val initialPageIds = remember(editingGuest, eventPages) {
      if (editingGuest != null) {
        editingGuest!!.getSelectedPageIds()
      } else {
        eventPages.map { it.id }
      }
    }
    val selectedPageIds = remember { mutableStateListOf<String>().apply { addAll(initialPageIds) } }

    val fallbackPage = remember(currentEvent) {
      EventPage(
        id = "page_${currentEvent?.id ?: "main"}",
        eventId = currentEvent?.id ?: "",
        pageName = if (currentEvent?.eventType == "Wedding") "Wedding Ceremony" else (currentEvent?.title ?: "Main Ceremony"),
        designId = currentEvent?.designId ?: "",
        greeting = getDefaultGreetingForPage(currentEvent?.eventType ?: "Wedding", "Main"),
        eventTitle = if (currentEvent?.eventType == "Wedding" && !currentEvent.groomName.isNullOrBlank() && !currentEvent.brideName.isNullOrBlank()) "${currentEvent.groomName} & ${currentEvent.brideName}" else (currentEvent?.title ?: "Wedding Celebration"),
        date = currentEvent?.primaryDate ?: "",
        venue = currentEvent?.primaryVenue ?: "",
        additionalDetails = currentEvent?.notes ?: "",
        showBismillah = currentEvent?.eventType.equals("Wedding", ignoreCase = true) || currentEvent == null
      )
    }

    val invitedPages = remember(selectedPageIds.toList(), eventPages) {
      val filtered = eventPages.filter { selectedPageIds.contains(it.id) }
      if (filtered.isNotEmpty()) filtered else (if (eventPages.isNotEmpty()) eventPages else listOf(fallbackPage))
    }

    var currentInvitedPageIndex by remember { mutableIntStateOf(0) }
    val safePageIndex = currentInvitedPageIndex.coerceIn(0, (invitedPages.size - 1).coerceAtLeast(0))
    val activePage = invitedPages.getOrElse(safePageIndex) { eventPages.firstOrNull() ?: fallbackPage }

    val activeDesign = allDesigns.firstOrNull { it.id == activePage.designId }
      ?: allDesigns.firstOrNull { it.id == currentEvent?.designId }
      ?: currentDesign
      ?: allDesigns.firstOrNull()

    val previewPage = if (activePage.designId.isBlank() && activeDesign != null) activePage.copy(designId = activeDesign.id) else activePage

    var guestNameInput by remember { mutableStateOf(editingGuest?.name ?: "") }
    var guestPhoneInput by remember { mutableStateOf(editingGuest?.phone ?: "") }

    var hasCustomOverride by remember { mutableStateOf(editingGuest?.hasCustomStyleOverride ?: false) }
    var saveAsEventDefault by remember { mutableStateOf(!isEditing) }

    // Map of per-page guest styles for multi-page editing
    val pageStyles = remember(editingGuest) { mutableStateMapOf<String, PageGuestStyle>() }
    // Initialize styles for all event pages
    eventPages.forEach { p ->
      if (!pageStyles.containsKey(p.id)) {
        pageStyles[p.id] = if (editingGuest != null) {
          editingGuest!!.getEffectiveStyleForPage(p, currentEvent)
        } else {
          p.getPageDefaultGuestStyle(currentEvent)
        }
      }
    }
    if (!pageStyles.containsKey(fallbackPage.id)) {
      pageStyles[fallbackPage.id] = if (editingGuest != null) {
        editingGuest!!.getEffectiveStyleForPage(fallbackPage, currentEvent)
      } else {
        fallbackPage.getPageDefaultGuestStyle(currentEvent)
      }
    }

    val currentStyle = pageStyles[activePage.id] ?: activePage.getPageDefaultGuestStyle(currentEvent)

    AlertDialog(
      onDismissRequest = { isAddingGuest = false },
      properties = DialogProperties(usePlatformDefaultWidth = false),
      modifier = Modifier
        .fillMaxWidth(0.96f)
        .fillMaxHeight(0.92f),
      title = {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = if (isEditing) "Edit Guest & Visual Positioning" else "Add Guest & Visual Positioning",
              fontFamily = FontFamily.Serif,
              fontSize = 17.sp,
              color = CharcoalPrimary
            )
            Text(
              text = "Position guest name directly on the invitation artwork",
              fontSize = 11.sp,
              color = CharcoalMuted
            )
          }
        }
      },
      text = {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
        ) {
          // Guest Full Name Field
          Text("Guest Full Name *", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = CharcoalPrimary)
          Spacer(modifier = Modifier.height(4.dp))
          OutlinedTextField(
            value = guestNameInput,
            onValueChange = { guestNameInput = it },
            placeholder = { Text("e.g. Mr. & Mrs. Tariq Aziz, Dr. Zainab Malik") },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("guest_name_input"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = GoldPrimary,
              unfocusedBorderColor = IvoryBorder
            )
          )

          Spacer(modifier = Modifier.height(8.dp))

          // Phone Field
          Text("Phone (Optional)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = CharcoalPrimary)
          Spacer(modifier = Modifier.height(4.dp))
          OutlinedTextField(
            value = guestPhoneInput,
            onValueChange = { guestPhoneInput = it },
            placeholder = { Text("e.g. +1 555-0199 (for WhatsApp)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = GoldPrimary,
              unfocusedBorderColor = IvoryBorder
            )
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Checkboxes for Invitation Pages
          Text(
            text = "INVITED PAGES (MULTI-PAGE WEDDING)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = GoldDark
          )
          Spacer(modifier = Modifier.height(4.dp))

          if (eventPages.isEmpty()) {
            Text("No separate ceremony pages created yet.", fontSize = 11.sp, color = CharcoalMuted)
          } else {
            eventPages.forEach { page ->
              val isChecked = selectedPageIds.contains(page.id)
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable {
                    if (isChecked) {
                      selectedPageIds.remove(page.id)
                    } else {
                      selectedPageIds.add(page.id)
                      // Navigate to this page in preview
                      val newIdx = eventPages.filter { selectedPageIds.contains(it.id) }.indexOfFirst { it.id == page.id }
                      if (newIdx >= 0) currentInvitedPageIndex = newIdx
                    }
                  }
                  .padding(vertical = 2.dp)
              ) {
                Checkbox(
                  checked = isChecked,
                  onCheckedChange = { chk ->
                    if (chk) {
                      selectedPageIds.add(page.id)
                      val newIdx = eventPages.filter { selectedPageIds.contains(it.id) }.indexOfFirst { it.id == page.id }
                      if (newIdx >= 0) currentInvitedPageIndex = newIdx
                    } else {
                      selectedPageIds.remove(page.id)
                    }
                  },
                  colors = CheckboxDefaults.colors(checkedColor = GoldPrimary)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(page.pageName, fontSize = 12.sp, color = CharcoalPrimary, fontWeight = FontWeight.Medium)
                  if (page.date.isNotBlank()) {
                    Text(page.date, fontSize = 10.sp, color = CharcoalMuted)
                  }
                }

                // Quick preview jump button
                if (isChecked && page.id != activePage.id) {
                  TextButton(
                    onClick = {
                      val newIdx = invitedPages.indexOfFirst { it.id == page.id }
                      if (newIdx >= 0) currentInvitedPageIndex = newIdx
                    },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                  ) {
                    Text("Preview", fontSize = 10.sp, color = GoldDark)
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))
          HorizontalDivider(color = IvoryBorder)
          Spacer(modifier = Modifier.height(12.dp))

          // -----------------------------------------------------------------
          // MULTI-PAGE PREVIEW NAVIGATION BAR
          // -----------------------------------------------------------------
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(IvorySurfaceLight)
              .border(0.8.dp, IvoryBorderStrong, RoundedCornerShape(8.dp))
              .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedButton(
              onClick = {
                if (safePageIndex > 0) {
                  currentInvitedPageIndex = safePageIndex - 1
                }
              },
              enabled = safePageIndex > 0,
              shape = RoundedCornerShape(6.dp),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
              modifier = Modifier
                .height(32.dp)
                .testTag("prev_preview_page_button")
            ) {
              Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Page", modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Prev", fontSize = 11.sp)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = activePage.pageName,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = CharcoalPrimary,
                modifier = Modifier.testTag("current_preview_page_name")
              )
              Text(
                text = "Page ${safePageIndex + 1} of ${invitedPages.size}",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = GoldDark,
                modifier = Modifier.testTag("current_preview_page_counter")
              )
            }

            OutlinedButton(
              onClick = {
                if (safePageIndex < invitedPages.size - 1) {
                  currentInvitedPageIndex = safePageIndex + 1
                }
              },
              enabled = safePageIndex < invitedPages.size - 1,
              shape = RoundedCornerShape(6.dp),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
              modifier = Modifier
                .height(32.dp)
                .testTag("next_preview_page_button")
            ) {
              Text("Next", fontSize = 11.sp)
              Spacer(modifier = Modifier.width(4.dp))
              Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Page", modifier = Modifier.size(14.dp))
            }
          }

          // Page selection chips if multiple pages
          if (invitedPages.size > 1) {
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              itemsIndexed(invitedPages) { idx, page ->
                val isSelected = (idx == safePageIndex)
                FilterChip(
                  selected = isSelected,
                  onClick = { currentInvitedPageIndex = idx },
                  label = { Text(page.pageName, fontSize = 11.sp) },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GoldPrimary.copy(alpha = 0.2f),
                    selectedLabelColor = GoldDark
                  ),
                  modifier = Modifier.testTag("preview_page_chip_$idx")
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // -----------------------------------------------------------------
          // VISUAL DRAGGABLE INVITATION CARD PREVIEW (MASTER LOCKED)
          // -----------------------------------------------------------------
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = GoldDark, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "VISUAL CANVAS (PAGE: ${activePage.pageName.uppercase()})",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 1.sp,
                  color = GoldDark
                )
              }
              Text(
                text = "Drag the guest name to position anywhere on this page",
                fontSize = 10.sp,
                color = CharcoalMuted
              )
            }

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(IvorySurfaceLight)
                .border(0.5.dp, IvoryBorderStrong, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = "X: ${(currentStyle.xPercent * 100).toInt()}% • Y: ${(currentStyle.yPercent * 100).toInt()}%",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = GoldDark
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Visual Card Container
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(350.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(Color(0xFF141210))
              .border(1.dp, GoldPrimary.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
              .padding(8.dp),
            contentAlignment = Alignment.Center
          ) {
            val previewGuest = remember(
              guestNameInput, currentStyle
            ) {
              GuestEntity(
                id = editingGuest?.id ?: "preview_guest_temp",
                eventId = currentEvent?.id ?: "",
                name = guestNameInput.ifBlank { "Honored Guest Name" },
                phone = guestPhoneInput,
                uniqueToken = "PREVIEW",
                hasCustomStyleOverride = true,
                guestNameXPercent = currentStyle.xPercent,
                guestNameYPercent = currentStyle.yPercent,
                guestNameFontSize = currentStyle.fontSizeSp,
                guestNameColorHex = currentStyle.colorHex,
                guestNameAlignment = currentStyle.alignment,
                guestNameFontFamily = currentStyle.fontFamily,
                guestNameFontStyle = currentStyle.fontStyle,
                guestNameFontWeight = currentStyle.fontWeight
              )
            }

            InvitationPageCard(
              page = previewPage,
              design = activeDesign,
              event = currentEvent,
              guest = previewGuest,
              guestName = guestNameInput.ifBlank { "Honored Guest Name" },
              isEditable = true,
              onlyGuestNameEditable = true,
              selectedLayerId = "guest_name",
              onUpdateLayer = { updated ->
                if (updated.id == "guest_name") {
                  pageStyles[activePage.id] = currentStyle.copy(
                    xPercent = updated.xPercent,
                    yPercent = updated.yPercent
                  )
                  hasCustomOverride = true
                }
              },
              modifier = Modifier.fillMaxHeight()
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Fine Nudge Buttons & Center Reset for Active Page
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Fine Nudge:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = CharcoalPrimary)

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
              IconButton(
                onClick = {
                  val newY = (currentStyle.yPercent - 0.015f).coerceIn(0.04f, 0.96f)
                  pageStyles[activePage.id] = currentStyle.copy(yPercent = newY)
                  hasCustomOverride = true
                },
                modifier = Modifier.size(30.dp)
              ) {
                Icon(Icons.Default.ArrowUpward, contentDescription = "Nudge Up", tint = CharcoalPrimary, modifier = Modifier.size(16.dp))
              }
              IconButton(
                onClick = {
                  val newY = (currentStyle.yPercent + 0.015f).coerceIn(0.04f, 0.96f)
                  pageStyles[activePage.id] = currentStyle.copy(yPercent = newY)
                  hasCustomOverride = true
                },
                modifier = Modifier.size(30.dp)
              ) {
                Icon(Icons.Default.ArrowDownward, contentDescription = "Nudge Down", tint = CharcoalPrimary, modifier = Modifier.size(16.dp))
              }
              IconButton(
                onClick = {
                  val newX = (currentStyle.xPercent - 0.015f).coerceIn(0.08f, 0.92f)
                  pageStyles[activePage.id] = currentStyle.copy(xPercent = newX)
                  hasCustomOverride = true
                },
                modifier = Modifier.size(30.dp)
              ) {
                Icon(Icons.Default.ArrowLeft, contentDescription = "Nudge Left", tint = CharcoalPrimary, modifier = Modifier.size(16.dp))
              }
              IconButton(
                onClick = {
                  val newX = (currentStyle.xPercent + 0.015f).coerceIn(0.08f, 0.92f)
                  pageStyles[activePage.id] = currentStyle.copy(xPercent = newX)
                  hasCustomOverride = true
                },
                modifier = Modifier.size(30.dp)
              ) {
                Icon(Icons.Default.ArrowRight, contentDescription = "Nudge Right", tint = CharcoalPrimary, modifier = Modifier.size(16.dp))
              }

              OutlinedButton(
                onClick = {
                  pageStyles[activePage.id] = currentStyle.copy(xPercent = 0.5f)
                  hasCustomOverride = true
                },
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 8.dp),
                modifier = Modifier.height(28.dp)
              ) {
                Text("Center X", fontSize = 10.sp, color = GoldDark)
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))
          HorizontalDivider(color = IvoryBorder)
          Spacer(modifier = Modifier.height(10.dp))

          // -----------------------------------------------------------------
          // TYPOGRAPHY & STYLING CONTROLS FOR ACTIVE PAGE
          // -----------------------------------------------------------------
          Text(
            text = "GUEST NAME TYPOGRAPHY & COLOR (${activePage.pageName.uppercase()})",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = GoldDark
          )
          Spacer(modifier = Modifier.height(8.dp))

          // Font Size & Colors Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Font Size
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text("Size:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = CharcoalPrimary)
              Spacer(modifier = Modifier.width(6.dp))
              OutlinedButton(
                onClick = {
                  if (currentStyle.fontSizeSp > 10) {
                    pageStyles[activePage.id] = currentStyle.copy(fontSizeSp = currentStyle.fontSizeSp - 1)
                    hasCustomOverride = true
                  }
                },
                contentPadding = PaddingValues(horizontal = 6.dp),
                modifier = Modifier
                  .size(width = 30.dp, height = 28.dp)
                  .testTag("decrease_font_size_button")
              ) {
                Text("–", fontSize = 13.sp)
              }
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                "${currentStyle.fontSizeSp}sp",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = GoldDark,
                modifier = Modifier.testTag("font_size_display")
              )
              Spacer(modifier = Modifier.width(6.dp))
              OutlinedButton(
                onClick = {
                  if (currentStyle.fontSizeSp < 48) {
                    pageStyles[activePage.id] = currentStyle.copy(fontSizeSp = currentStyle.fontSizeSp + 1)
                    hasCustomOverride = true
                  }
                },
                contentPadding = PaddingValues(horizontal = 6.dp),
                modifier = Modifier
                  .size(width = 30.dp, height = 28.dp)
                  .testTag("increase_font_size_button")
              ) {
                Text("+", fontSize = 13.sp)
              }
            }

            // Colors
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              listOf(
                "#8C6D23" to "Gold",
                "#D4AF37" to "Warm Gold",
                "#1E1A16" to "Obsidian",
                "#0F3D29" to "Emerald",
                "#FFFFFF" to "White",
                "#C7828E" to "Rose Gold"
              ).forEach { (hex, _) ->
                val isSel = currentStyle.colorHex.equals(hex, ignoreCase = true)
                Box(
                  modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                      when (hex) {
                        "#FFFFFF" -> Color.White
                        "#8C6D23" -> GoldPrimary
                        "#D4AF37" -> Color(0xFFD4AF37)
                        "#0F3D29" -> Color(0xFF0F3D29)
                        "#C7828E" -> Color(0xFFC7828E)
                        else -> Color(0xFF1E1A16)
                      }
                    )
                    .border(
                      width = if (isSel) 2.dp else 1.dp,
                      color = if (isSel) GoldDark else Color.Gray.copy(alpha = 0.4f),
                      shape = CircleShape
                    )
                    .clickable {
                      pageStyles[activePage.id] = currentStyle.copy(colorHex = hex)
                      hasCustomOverride = true
                    },
                  contentAlignment = Alignment.Center
                ) {
                  if (isSel) {
                    Icon(
                      Icons.Default.Check,
                      contentDescription = null,
                      tint = if (hex == "#FFFFFF") Color.Black else Color.White,
                      modifier = Modifier.size(12.dp)
                    )
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Font Family Chips
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Font:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = CharcoalPrimary)

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              listOf("Serif", "SansSerif", "Cursive", "Monospace").forEach { fam ->
                val isSel = currentStyle.fontFamily.equals(fam, ignoreCase = true)
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isSel) GoldPrimary else IvorySurfaceLight)
                    .border(0.5.dp, IvoryBorderStrong, RoundedCornerShape(4.dp))
                    .clickable {
                      pageStyles[activePage.id] = currentStyle.copy(fontFamily = fam)
                      hasCustomOverride = true
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                  Text(
                    text = fam,
                    fontSize = 10.sp,
                    color = if (isSel) Color.White else CharcoalPrimary,
                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Font Style & Alignment Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Style / Weight Chips
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
              Text("Style:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = CharcoalPrimary)
              listOf(
                Triple("Italic", "Normal", "Italic"),
                Triple("Bold", "Bold", "Normal"),
                Triple("Bold Italic", "Bold", "Italic"),
                Triple("Regular", "Normal", "Normal")
              ).forEach { (label, wt, st) ->
                val isSel = (currentStyle.fontWeight.equals(wt, ignoreCase = true) && currentStyle.fontStyle.equals(st, ignoreCase = true))
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isSel) GoldPrimary else IvorySurfaceLight)
                    .border(0.5.dp, IvoryBorderStrong, RoundedCornerShape(4.dp))
                    .clickable {
                      pageStyles[activePage.id] = currentStyle.copy(fontWeight = wt, fontStyle = st)
                      hasCustomOverride = true
                    }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                  Text(
                    text = label,
                    fontSize = 10.sp,
                    color = if (isSel) Color.White else CharcoalPrimary
                  )
                }
              }
            }

            // Alignment Icons
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
              IconButton(
                onClick = {
                  pageStyles[activePage.id] = currentStyle.copy(alignment = "Left")
                  hasCustomOverride = true
                },
                modifier = Modifier.size(28.dp)
              ) {
                Icon(
                  Icons.Default.FormatAlignLeft,
                  contentDescription = "Left",
                  tint = if (currentStyle.alignment.equals("Left", true)) GoldDark else CharcoalMuted,
                  modifier = Modifier.size(16.dp)
                )
              }
              IconButton(
                onClick = {
                  pageStyles[activePage.id] = currentStyle.copy(alignment = "Center")
                  hasCustomOverride = true
                },
                modifier = Modifier.size(28.dp)
              ) {
                Icon(
                  Icons.Default.FormatAlignCenter,
                  contentDescription = "Center",
                  tint = if (currentStyle.alignment.equals("Center", true)) GoldDark else CharcoalMuted,
                  modifier = Modifier.size(16.dp)
                )
              }
              IconButton(
                onClick = {
                  pageStyles[activePage.id] = currentStyle.copy(alignment = "Right")
                  hasCustomOverride = true
                },
                modifier = Modifier.size(28.dp)
              ) {
                Icon(
                  Icons.Default.FormatAlignRight,
                  contentDescription = "Right",
                  tint = if (currentStyle.alignment.equals("Right", true)) GoldDark else CharcoalMuted,
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))
          HorizontalDivider(color = IvoryBorder)
          Spacer(modifier = Modifier.height(10.dp))

          // -----------------------------------------------------------------
          // EVENT DEFAULT MANAGEMENT ("USE AS DEFAULT FOR THIS EVENT")
          // -----------------------------------------------------------------
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(IvorySurfaceLight)
              .border(0.5.dp, IvoryBorderStrong, RoundedCornerShape(8.dp))
              .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Use as Default for This Event",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CharcoalPrimary
              )
              Text(
                text = "Save custom position & styles as defaults for each page across all guests",
                fontSize = 10.sp,
                color = CharcoalMuted
              )
            }

            Switch(
              checked = saveAsEventDefault,
              onCheckedChange = { saveAsEventDefault = it },
              colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = GoldPrimary
              ),
              modifier = Modifier.testTag("use_as_event_default_switch")
            )
          }

          // Reset Active Page to Default button if modified
          Spacer(modifier = Modifier.height(8.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            OutlinedButton(
              onClick = {
                pageStyles[activePage.id] = activePage.getPageDefaultGuestStyle(currentEvent)
              },
              shape = RoundedCornerShape(6.dp),
              modifier = Modifier.height(28.dp)
            ) {
              Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(12.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Reset This Page to Default", fontSize = 10.sp, color = CharcoalMuted)
            }
          }
        }
      },
      confirmButton = {
        ElevatedButton(
          onClick = {
            if (guestNameInput.isNotBlank() && currentEvent != null) {
              // 1. If saveAsEventDefault is active, update the event and page defaults
              if (saveAsEventDefault) {
                val updatedPages = currentEvent.getPages().map { pg ->
                  val st = pageStyles[pg.id]
                  if (st != null) {
                    pg.copy(
                      hasCustomGuestDefault = true,
                      defaultGuestXPercent = st.xPercent,
                      defaultGuestYPercent = st.yPercent,
                      defaultGuestFontSize = st.fontSizeSp,
                      defaultGuestColorHex = st.colorHex,
                      defaultGuestFontFamily = st.fontFamily,
                      defaultGuestFontWeight = st.fontWeight,
                      defaultGuestFontStyle = st.fontStyle,
                      defaultGuestAlignment = st.alignment
                    )
                  } else pg
                }
                val actStyle = pageStyles[activePage.id] ?: currentStyle
                val updatedEvent = currentEvent.copy(
                  pagesJson = EventPage.listToJson(updatedPages),
                  defaultGuestNameXPercent = actStyle.xPercent,
                  defaultGuestNameYPercent = actStyle.yPercent,
                  defaultGuestNameFontSize = actStyle.fontSizeSp,
                  defaultGuestNameColorHex = actStyle.colorHex,
                  defaultGuestNameFontFamily = actStyle.fontFamily,
                  defaultGuestNameFontStyle = actStyle.fontStyle,
                  defaultGuestNameFontWeight = actStyle.fontWeight,
                  defaultGuestNameAlignment = actStyle.alignment
                )
                onUpdateEvent?.invoke(updatedEvent)
              }

              // 2. Save guest entity
              val actStyle = pageStyles[activePage.id] ?: currentStyle
              val guestToSave = GuestEntity(
                id = editingGuest?.id ?: ("gst_" + UUID.randomUUID().toString().take(8)),
                eventId = currentEvent.id,
                name = guestNameInput.trim(),
                phone = guestPhoneInput.trim(),
                uniqueToken = editingGuest?.uniqueToken ?: generateSecureToken(),
                active = editingGuest?.active ?: true,
                selectedSubEventIdsJson = "[]",
                selectedPageIdsJson = GuestEntity.idsToJson(selectedPageIds.toList()),
                createdAt = editingGuest?.createdAt ?: System.currentTimeMillis(),
                hasCustomStyleOverride = hasCustomOverride && !saveAsEventDefault,
                guestNameXPercent = actStyle.xPercent,
                guestNameYPercent = actStyle.yPercent,
                guestNameFontSize = actStyle.fontSizeSp,
                guestNameColorHex = actStyle.colorHex,
                guestNameAlignment = actStyle.alignment,
                guestNameFontFamily = actStyle.fontFamily,
                guestNameFontStyle = actStyle.fontStyle,
                guestNameFontWeight = actStyle.fontWeight,
                pageOverridesJson = if (hasCustomOverride && !saveAsEventDefault) PageGuestStyle.mapToJson(pageStyles.toMap()) else "{}"
              )
              onAddGuest(guestToSave)
              isAddingGuest = false
              editingGuest = null
            }
          },
          colors = ButtonDefaults.elevatedButtonColors(containerColor = GoldPrimary, contentColor = Color.White),
          modifier = Modifier.testTag("confirm_add_guest_button")
        ) {
          Text(if (isEditing) "Save Changes" else "Create Guest Link", fontSize = 12.sp)
        }
      },
      dismissButton = {
        TextButton(onClick = {
          isAddingGuest = false
          editingGuest = null
        }) {
          Text("Cancel", color = CharcoalMuted)
        }
      },
      containerColor = IvorySurface,
      shape = RoundedCornerShape(12.dp)
    )
  }

  // -------------------------------------------------------------
  // DIALOG: Demo-Scale Testing (10, 50, 100, 500, 1000 guests)
  // -------------------------------------------------------------
  if (isBatchGenerating) {
    AlertDialog(
      onDismissRequest = { if (!isBulkWorking) isBatchGenerating = false },
      title = { Text("Demo-Scale Testing", fontFamily = FontFamily.Serif, color = CharcoalPrimary) },
      text = {
        Column {
          Text(
            text = "Generate large volume of test guests with unique links to verify performance stability.",
            fontSize = 12.sp,
            color = CharcoalSecondary
          )
          Spacer(modifier = Modifier.height(16.dp))

          if (isBulkWorking) {
            Column(
              modifier = Modifier.fillMaxWidth(),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              CircularProgressIndicator(color = GoldPrimary, modifier = Modifier.size(36.dp))
              Spacer(modifier = Modifier.height(12.dp))
              Text("Generating unique tokens...", fontSize = 12.sp, color = CharcoalMuted)
            }
          } else {
            val quantities = listOf(10, 50, 100, 500, 1000)
            quantities.forEach { count ->
              ElevatedButton(
                onClick = {
                  if (currentEvent != null) {
                    isBulkWorking = true
                    scope.launch(Dispatchers.IO) {
                      val generated = mutableListOf<GuestEntity>()
                      val baseFirstNames = listOf("Aarav", "Fatima", "Zainab", "Ali", "Hamza", "Ayesha", "Omar", "Sara", "Mustafa", "Noor", "Bilal", "Maryam", "Tariq", "Hina")
                      val baseLastNames = listOf("Khan", "Ahmed", "Syed", "Malik", "Chaudhry", "Mirza", "Qureshi", "Siddiqui", "Hashmi", "Bhatti")

                      for (i in 1..count) {
                        val fn = baseFirstNames[(i + (i % 7)) % baseFirstNames.size]
                        val ln = baseLastNames[(i * 3) % baseLastNames.size]
                        val guestName = "$fn $ln"

                        val pageSubset = if (eventPages.isEmpty()) emptyList()
                        else when (i % 3) {
                          0 -> eventPages.map { it.id } // All pages
                          1 -> listOf(eventPages.first().id) // First page only (e.g. Mehndi)
                          else -> listOf(eventPages.last().id) // Last page only (e.g. Walima)
                        }

                        generated.add(
                          GuestEntity(
                            id = "gst_bulk_${System.currentTimeMillis()}_$i",
                            eventId = currentEvent.id,
                            name = guestName,
                            phone = "+1 555-${(1000 + i % 9000)}",
                            uniqueToken = generateSecureToken(),
                            active = true,
                            selectedSubEventIdsJson = "[]",
                            selectedPageIdsJson = GuestEntity.idsToJson(pageSubset),
                            createdAt = System.currentTimeMillis()
                          )
                        )
                      }

                      onAddBulkGuests(generated)

                      withContext(Dispatchers.Main) {
                        isBulkWorking = false
                        isBatchGenerating = false
                        Toast.makeText(context, "Successfully generated $count guest links!", Toast.LENGTH_SHORT).show()
                      }
                    }
                  }
                },
                colors = ButtonDefaults.elevatedButtonColors(containerColor = IvorySurfaceLight, contentColor = CharcoalPrimary),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp)
              ) {
                Text("Generate $count Guests", fontFamily = FontFamily.Serif, fontSize = 12.sp)
              }
            }
          }
        }
      },
      confirmButton = {},
      dismissButton = {
        if (!isBulkWorking) {
          TextButton(onClick = { isBatchGenerating = false }) {
            Text("Close", color = CharcoalMuted)
          }
        }
      },
      containerColor = IvorySurface,
      shape = RoundedCornerShape(12.dp)
    )
  }
}
