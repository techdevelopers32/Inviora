package com.example.data.repository

import com.example.data.dao.DesignDao
import com.example.data.dao.EventDao
import com.example.data.dao.GuestDao
import com.example.data.model.DesignTemplateEntity
import com.example.data.model.EventEntity
import com.example.data.model.EventPage
import com.example.data.model.GuestEntity
import com.example.data.model.PageGuestStyle
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * Data classes representing the Firestore document schema for published invitations.
 * Target collection: publishedInvitations/{uniqueToken}
 */
data class PublishedTraditionalGreetingDto(
  val includeTraditionalGreeting: Boolean,
  val traditionalBismillahText: String,
  val traditionalGreetingText: String
) {
  fun toMap(): Map<String, Any?> = mapOf(
    "includeTraditionalGreeting" to includeTraditionalGreeting,
    "traditionalBismillahText" to traditionalBismillahText,
    "traditionalGreetingText" to traditionalGreetingText
  )
}

data class PublishedGuestNameSettingsDto(
  val xPercent: Float,
  val yPercent: Float,
  val fontSize: Int,
  val colorHex: String,
  val fontFamily: String,
  val fontWeight: String,
  val fontStyle: String,
  val alignment: String
) {
  fun toMap(): Map<String, Any?> = mapOf(
    "xPercent" to xPercent,
    "yPercent" to yPercent,
    "fontSize" to fontSize,
    "colorHex" to colorHex,
    "fontFamily" to fontFamily,
    "fontWeight" to fontWeight,
    "fontStyle" to fontStyle,
    "alignment" to alignment
  )
}

data class PublishedDesignMetadataDto(
  val designId: String,
  val designName: String,
  val accentColorHex: String,
  val fontStyle: String,
  val themeStyle: String,
  val ornamentStyle: String
) {
  fun toMap(): Map<String, Any?> = mapOf(
    "designId" to designId,
    "designName" to designName,
    "accentColorHex" to accentColorHex,
    "fontStyle" to fontStyle,
    "themeStyle" to themeStyle,
    "ornamentStyle" to ornamentStyle
  )
}

data class PublishedGuestOverrideDto(
  val hasOverride: Boolean,
  val xPercent: Float,
  val yPercent: Float,
  val fontSize: Int,
  val colorHex: String,
  val fontFamily: String,
  val fontWeight: String,
  val fontStyle: String,
  val alignment: String
) {
  fun toMap(): Map<String, Any?> = mapOf(
    "hasOverride" to hasOverride,
    "xPercent" to xPercent,
    "yPercent" to yPercent,
    "fontSize" to fontSize,
    "colorHex" to colorHex,
    "fontFamily" to fontFamily,
    "fontWeight" to fontWeight,
    "fontStyle" to fontStyle,
    "alignment" to alignment
  )
}

data class PublishedPageGuestSettingsDto(
  val hasCustomGuestDefault: Boolean,
  val defaultGuestXPercent: Float,
  val defaultGuestYPercent: Float,
  val defaultGuestFontSize: Int,
  val defaultGuestColorHex: String,
  val defaultGuestFontFamily: String,
  val defaultGuestFontWeight: String,
  val defaultGuestFontStyle: String,
  val defaultGuestAlignment: String
) {
  fun toMap(): Map<String, Any?> = mapOf(
    "hasCustomGuestDefault" to hasCustomGuestDefault,
    "defaultGuestXPercent" to defaultGuestXPercent,
    "defaultGuestYPercent" to defaultGuestYPercent,
    "defaultGuestFontSize" to defaultGuestFontSize,
    "defaultGuestColorHex" to defaultGuestColorHex,
    "defaultGuestFontFamily" to defaultGuestFontFamily,
    "defaultGuestFontWeight" to defaultGuestFontWeight,
    "defaultGuestFontStyle" to defaultGuestFontStyle,
    "defaultGuestAlignment" to defaultGuestAlignment
  )
}

data class PublishedPageDto(
  val pageId: String,
  val pageName: String,
  val pageOrder: Int,
  val designId: String,
  val date: String,
  val time: String,
  val venue: String,
  val greeting: String,
  val eventTitle: String,
  val additionalDetails: String,
  val showBismillah: Boolean,
  val bismillahArabic: String,
  val bismillahEnglish: String,
  val textLayoutJson: String,
  val pageGuestSettings: PublishedPageGuestSettingsDto,
  val guestOverride: PublishedGuestOverrideDto,
  val designMetadata: PublishedDesignMetadataDto
) {
  fun toMap(): Map<String, Any?> = mapOf(
    "pageId" to pageId,
    "pageName" to pageName,
    "pageOrder" to pageOrder,
    "designId" to designId,
    "date" to date,
    "time" to time,
    "venue" to venue,
    "greeting" to greeting,
    "eventTitle" to eventTitle,
    "additionalDetails" to additionalDetails,
    "showBismillah" to showBismillah,
    "bismillahArabic" to bismillahArabic,
    "bismillahEnglish" to bismillahEnglish,
    "textLayoutJson" to textLayoutJson,
    "pageGuestSettings" to pageGuestSettings.toMap(),
    "guestOverride" to guestOverride.toMap(),
    "designMetadata" to designMetadata.toMap()
  )
}

data class PublishedInvitationDto(
  val token: String,
  val active: Boolean,
  val eventId: String,
  val guestId: String,
  val guestName: String,
  val phone: String,
  val eventTitle: String,
  val eventType: String,
  val groomName: String,
  val brideName: String,
  val hostNames: String,
  val primaryVenue: String,
  val primaryDate: String,
  val notes: String,
  val animationId: String,
  val invitationWording: String,
  val traditionalGreeting: PublishedTraditionalGreetingDto,
  val defaultGuestNameSettings: PublishedGuestNameSettingsDto,
  val selectedPageIds: List<String>,
  val pages: List<PublishedPageDto>,
  val publishedAt: Long,
  val updatedAt: Long
) {
  fun toMap(): Map<String, Any?> = mapOf(
    "token" to token,
    "active" to active,
    "eventId" to eventId,
    "guestId" to guestId,
    "guestName" to guestName,
    "phone" to phone,
    "eventTitle" to eventTitle,
    "eventType" to eventType,
    "groomName" to groomName,
    "brideName" to brideName,
    "hostNames" to hostNames,
    "primaryVenue" to primaryVenue,
    "primaryDate" to primaryDate,
    "notes" to notes,
    "animationId" to animationId,
    "invitationWording" to invitationWording,
    "traditionalGreeting" to traditionalGreeting.toMap(),
    "defaultGuestNameSettings" to defaultGuestNameSettings.toMap(),
    "selectedPageIds" to selectedPageIds,
    "pages" to pages.map { it.toMap() },
    "publishedAt" to publishedAt,
    "updatedAt" to updatedAt
  )
}

data class PublishResult(
  val token: String,
  val guestId: String,
  val guestName: String,
  val isPublished: Boolean,
  val message: String
)

data class PublishEventResult(
  val eventId: String,
  val eventTitle: String,
  val totalGuests: Int,
  val publishedCount: Int,
  val deactivatedCount: Int,
  val results: List<PublishResult>
)

/**
 * Repository responsible for synchronizing/publishing invitations from Room to Cloud Firestore.
 * Room remains the local single source of truth for all editing.
 */
class FirestorePublishingRepository(
  private val eventDao: EventDao,
  private val guestDao: GuestDao,
  private val designDao: DesignDao,
  private val firestoreProvider: () -> FirebaseFirestore = { FirebaseFirestore.getInstance() }
) {
  companion object {
    const val COLLECTION_PUBLISHED_INVITATIONS = "publishedInvitations"
  }

  /**
   * Builds the published invitation document DTO by resolving event, guest,
   * filtered pages, page-specific designs, and guest layout overrides.
   */
  fun buildPublishedInvitationDto(
    event: EventEntity,
    guest: GuestEntity,
    allDesignsMap: Map<String, DesignTemplateEntity>,
    existingPublishedAt: Long? = null
  ): PublishedInvitationDto {
    val allPages = event.getPages()
    val invitedPages = guest.getInvitedPages(allPages)
    val guestOverridesMap = PageGuestStyle.parseMap(guest.pageOverridesJson)

    val publishedPages = invitedPages.map { page ->
      val effectiveDesignId = page.designId.ifBlank { event.designId }
      val design = allDesignsMap[effectiveDesignId]

      val designMetadata = PublishedDesignMetadataDto(
        designId = design?.id ?: effectiveDesignId.ifBlank { "default_design" },
        designName = design?.name ?: "Royal Heritage",
        accentColorHex = design?.accentColorHex ?: "#D4AF37",
        fontStyle = design?.fontStyle ?: "Serif Calligraphic",
        themeStyle = design?.themeStyle ?: "ROYAL_GOLD",
        ornamentStyle = design?.ornamentStyle ?: "FLORAL_CORNER"
      )

      val hasPageOverride = guestOverridesMap.containsKey(page.id)
      val hasOverride = hasPageOverride || guest.hasCustomStyleOverride
      val effectiveStyle = guest.getEffectiveStyleForPage(page, event)

      val guestOverride = PublishedGuestOverrideDto(
        hasOverride = hasOverride,
        xPercent = effectiveStyle.xPercent,
        yPercent = effectiveStyle.yPercent,
        fontSize = effectiveStyle.fontSizeSp,
        colorHex = effectiveStyle.colorHex,
        fontFamily = effectiveStyle.fontFamily,
        fontWeight = effectiveStyle.fontWeight,
        fontStyle = effectiveStyle.fontStyle,
        alignment = effectiveStyle.alignment
      )

      val pageGuestSettings = PublishedPageGuestSettingsDto(
        hasCustomGuestDefault = page.hasCustomGuestDefault,
        defaultGuestXPercent = page.defaultGuestXPercent,
        defaultGuestYPercent = page.defaultGuestYPercent,
        defaultGuestFontSize = page.defaultGuestFontSize,
        defaultGuestColorHex = page.defaultGuestColorHex,
        defaultGuestFontFamily = page.defaultGuestFontFamily,
        defaultGuestFontWeight = page.defaultGuestFontWeight,
        defaultGuestFontStyle = page.defaultGuestFontStyle,
        defaultGuestAlignment = page.defaultGuestAlignment
      )

      PublishedPageDto(
        pageId = page.id,
        pageName = page.pageName,
        pageOrder = page.pageOrder,
        designId = effectiveDesignId,
        date = page.date,
        time = page.time,
        venue = page.venue,
        greeting = page.greeting,
        eventTitle = page.eventTitle,
        additionalDetails = page.additionalDetails,
        showBismillah = page.showBismillah,
        bismillahArabic = page.bismillahArabic,
        bismillahEnglish = page.bismillahEnglish,
        textLayoutJson = page.textLayoutJson,
        pageGuestSettings = pageGuestSettings,
        guestOverride = guestOverride,
        designMetadata = designMetadata
      )
    }

    val defaultGuestSettings = PublishedGuestNameSettingsDto(
      xPercent = event.defaultGuestNameXPercent,
      yPercent = event.defaultGuestNameYPercent,
      fontSize = event.defaultGuestNameFontSize,
      colorHex = event.defaultGuestNameColorHex,
      fontFamily = event.defaultGuestNameFontFamily,
      fontWeight = event.defaultGuestNameFontWeight,
      fontStyle = event.defaultGuestNameFontStyle,
      alignment = event.defaultGuestNameAlignment
    )

    val traditionalGreeting = PublishedTraditionalGreetingDto(
      includeTraditionalGreeting = event.includeTraditionalGreeting,
      traditionalBismillahText = event.traditionalBismillahText,
      traditionalGreetingText = event.traditionalGreetingText
    )

    val now = System.currentTimeMillis()
    return PublishedInvitationDto(
      token = guest.uniqueToken,
      active = guest.active,
      eventId = event.id,
      guestId = guest.id,
      guestName = guest.name,
      phone = guest.phone,
      eventTitle = event.title,
      eventType = event.eventType,
      groomName = event.groomName,
      brideName = event.brideName,
      hostNames = event.hostNames,
      primaryVenue = event.primaryVenue,
      primaryDate = event.primaryDate,
      notes = event.notes,
      animationId = event.animationId,
      invitationWording = event.invitationWording,
      traditionalGreeting = traditionalGreeting,
      defaultGuestNameSettings = defaultGuestSettings,
      selectedPageIds = guest.getSelectedPageIds(),
      pages = publishedPages,
      publishedAt = existingPublishedAt ?: now,
      updatedAt = now
    )
  }

  /**
   * Publishes or updates a single guest invitation document in Firestore under publishedInvitations/{uniqueToken}.
   */
  suspend fun publishInvitation(eventId: String, guestId: String): Result<PublishResult> {
    return try {
      val event = eventDao.getEventById(eventId)
        ?: return Result.failure(IllegalArgumentException("Event with ID '$eventId' not found in local database."))
      val guests = guestDao.getGuestsListForEvent(eventId)
      val guest = guests.firstOrNull { it.id == guestId }
        ?: return Result.failure(IllegalArgumentException("Guest with ID '$guestId' not found for event '$eventId'."))

      val firestore = firestoreProvider()
      val docRef = firestore.collection(COLLECTION_PUBLISHED_INVITATIONS).document(guest.uniqueToken)

      if (!guest.active) {
        val snapshot = docRef.get().await()
        if (snapshot.exists()) {
          docRef.update(
            mapOf(
              "active" to false,
              "updatedAt" to System.currentTimeMillis()
            )
          ).await()
          return Result.success(
            PublishResult(
              token = guest.uniqueToken,
              guestId = guest.id,
              guestName = guest.name,
              isPublished = false,
              message = "Guest is inactive; marked published invitation as inactive."
            )
          )
        } else {
          return Result.success(
            PublishResult(
              token = guest.uniqueToken,
              guestId = guest.id,
              guestName = guest.name,
              isPublished = false,
              message = "Guest is inactive and was not previously published. Skipped."
            )
          )
        }
      }

      val allDesigns = designDao.getAllDesignsList()
      val designsMap = allDesigns.associateBy { it.id }

      val snapshot = docRef.get().await()
      val existingPublishedAt = if (snapshot.exists()) snapshot.getLong("publishedAt") else null

      val dto = buildPublishedInvitationDto(event, guest, designsMap, existingPublishedAt)
      docRef.set(dto.toMap(), SetOptions.merge()).await()

      Result.success(
        PublishResult(
          token = guest.uniqueToken,
          guestId = guest.id,
          guestName = guest.name,
          isPublished = true,
          message = "Invitation published successfully."
        )
      )
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Publishes or updates all guest invitations for a given event in Firestore.
   */
  suspend fun publishEventInvitations(eventId: String): Result<PublishEventResult> {
    return try {
      val event = eventDao.getEventById(eventId)
        ?: return Result.failure(IllegalArgumentException("Event with ID '$eventId' not found in local database."))
      val guests = guestDao.getGuestsListForEvent(eventId)

      if (guests.isEmpty()) {
        return Result.success(
          PublishEventResult(
            eventId = event.id,
            eventTitle = event.title,
            totalGuests = 0,
            publishedCount = 0,
            deactivatedCount = 0,
            results = emptyList()
          )
        )
      }

      val firestore = firestoreProvider()
      val allDesigns = designDao.getAllDesignsList()
      val designsMap = allDesigns.associateBy { it.id }

      val results = mutableListOf<PublishResult>()
      var publishedCount = 0
      var deactivatedCount = 0

      for (guest in guests) {
        val docRef = firestore.collection(COLLECTION_PUBLISHED_INVITATIONS).document(guest.uniqueToken)
        if (!guest.active) {
          val snapshot = docRef.get().await()
          if (snapshot.exists()) {
            docRef.update(
              mapOf(
                "active" to false,
                "updatedAt" to System.currentTimeMillis()
              )
            ).await()
            deactivatedCount++
            results.add(
              PublishResult(
                token = guest.uniqueToken,
                guestId = guest.id,
                guestName = guest.name,
                isPublished = false,
                message = "Guest is inactive; marked published invitation as inactive."
              )
            )
          } else {
            results.add(
              PublishResult(
                token = guest.uniqueToken,
                guestId = guest.id,
                guestName = guest.name,
                isPublished = false,
                message = "Guest is inactive and was not previously published. Skipped."
              )
            )
          }
        } else {
          val snapshot = docRef.get().await()
          val existingPublishedAt = if (snapshot.exists()) snapshot.getLong("publishedAt") else null

          val dto = buildPublishedInvitationDto(event, guest, designsMap, existingPublishedAt)
          docRef.set(dto.toMap(), SetOptions.merge()).await()
          publishedCount++
          results.add(
            PublishResult(
              token = guest.uniqueToken,
              guestId = guest.id,
              guestName = guest.name,
              isPublished = true,
              message = "Invitation published successfully."
            )
          )
        }
      }

      Result.success(
        PublishEventResult(
          eventId = event.id,
          eventTitle = event.title,
          totalGuests = guests.size,
          publishedCount = publishedCount,
          deactivatedCount = deactivatedCount,
          results = results
        )
      )
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Update published invitation (identical sync behavior using the guest's permanent uniqueToken).
   */
  suspend fun updatePublishedInvitation(eventId: String, guestId: String): Result<PublishResult> =
    publishInvitation(eventId, guestId)

  /**
   * Update all published invitations for an event.
   */
  suspend fun updatePublishedEventInvitations(eventId: String): Result<PublishEventResult> =
    publishEventInvitations(eventId)

  /**
   * Sets active = false on a published invitation in Firestore when revoked or removed.
   */
  suspend fun deactivatePublishedInvitation(uniqueToken: String): Result<Unit> {
    return try {
      val firestore = firestoreProvider()
      val docRef = firestore.collection(COLLECTION_PUBLISHED_INVITATIONS).document(uniqueToken)
      val snapshot = docRef.get().await()
      if (snapshot.exists()) {
        docRef.update(
          mapOf(
            "active" to false,
            "updatedAt" to System.currentTimeMillis()
          )
        ).await()
      }
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Reads the current published invitation document directly from Firestore.
   * Useful for verification and inspection.
   */
  suspend fun getPublishedInvitation(uniqueToken: String): Result<Map<String, Any?>?> {
    return try {
      val firestore = firestoreProvider()
      val snapshot = firestore.collection(COLLECTION_PUBLISHED_INVITATIONS).document(uniqueToken).get().await()
      if (snapshot.exists()) {
        Result.success(snapshot.data)
      } else {
        Result.success(null)
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
