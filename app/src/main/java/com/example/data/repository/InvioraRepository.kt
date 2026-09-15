package com.example.data.repository

import com.example.data.dao.AnimationDao
import com.example.data.dao.DesignDao
import com.example.data.dao.EventDao
import com.example.data.dao.GuestDao
import com.example.data.dao.PreferenceDao
import com.example.data.model.AnimationExperienceEntity
import com.example.data.model.AppPreferenceEntity
import com.example.data.model.DesignTemplateEntity
import com.example.data.model.EventEntity
import com.example.data.model.GuestEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class InvioraRepository(
  private val designDao: DesignDao,
  private val animationDao: AnimationDao,
  private val eventDao: EventDao,
  private val guestDao: GuestDao,
  private val preferenceDao: PreferenceDao,
  val authRepository: FirebaseAuthRepository = FirebaseAuthRepository(),
  val publishingRepository: FirestorePublishingRepository = FirestorePublishingRepository(
    eventDao,
    guestDao,
    designDao
  )
) {
  companion object {
    const val KEY_CURRENT_EVENT_ID = "current_event_id"
    const val BASE_INVITATION_URL = "https://techdevelopers32.github.io/Inviora/invite"

    fun buildShareUrl(uniqueToken: String): String =
      "$BASE_INVITATION_URL/$uniqueToken"

    fun buildShareMessage(guestName: String, eventTitle: String, uniqueToken: String): String =
      "Dear $guestName,\n\nWe are delighted to invite you to celebrate $eventTitle.\n\nPlease open your invitation here:\n${buildShareUrl(uniqueToken)}"
  }

  // --- Designs ---
  val allDesigns: Flow<List<DesignTemplateEntity>> = designDao.getAllDesigns()

  suspend fun getDesignById(id: String): DesignTemplateEntity? = designDao.getDesignById(id)
  fun getDesignFlow(id: String): Flow<DesignTemplateEntity?> = designDao.getDesignFlow(id)
  suspend fun insertDesign(design: DesignTemplateEntity) = designDao.insertDesign(design)
  suspend fun updateDesign(design: DesignTemplateEntity) = designDao.updateDesign(design)
  suspend fun getEventsUsingDesign(designId: String): List<EventEntity> = eventDao.getEventsUsingDesign(designId)

  suspend fun deleteDesign(designId: String) {
    designDao.deleteDesignById(designId)
  }

  suspend fun renameDesign(id: String, newName: String) {
    designDao.renameDesign(id, newName)
  }

  suspend fun toggleFavoriteDesign(id: String, isFav: Boolean) {
    designDao.toggleFavorite(id, isFav)
  }

  suspend fun deleteDesignWithCascade(designId: String) {
    val events = eventDao.getEventsUsingDesign(designId)
    for (event in events) {
      deleteEvent(event.id)
    }
    designDao.deleteDesignById(designId)
  }

  // --- Animations ---
  val allAnimations: Flow<List<AnimationExperienceEntity>> = animationDao.getAllAnimations()

  suspend fun getAnimationById(id: String): AnimationExperienceEntity? = animationDao.getAnimationById(id)
  fun getAnimationFlow(id: String): Flow<AnimationExperienceEntity?> = animationDao.getAnimationFlow(id)
  suspend fun insertAnimation(animation: AnimationExperienceEntity) = animationDao.insertAnimation(animation)
  suspend fun toggleFavoriteAnimation(id: String, isFav: Boolean) = animationDao.toggleFavorite(id, isFav)
  suspend fun getEventsUsingAnimation(animationId: String): List<EventEntity> = eventDao.getEventsUsingAnimation(animationId)

  suspend fun deleteAnimation(animationId: String) {
    animationDao.deleteAnimationById(animationId)
  }

  suspend fun deleteAnimationWithCascade(animationId: String) {
    val events = eventDao.getEventsUsingAnimation(animationId)
    for (event in events) {
      deleteEvent(event.id)
    }
    animationDao.deleteAnimationById(animationId)
  }

  // --- Events ---
  val allEvents: Flow<List<EventEntity>> = eventDao.getAllEvents()

  suspend fun getEventById(id: String): EventEntity? = eventDao.getEventById(id)
  fun getEventFlow(id: String): Flow<EventEntity?> = eventDao.getEventFlow(id)

  suspend fun insertEvent(event: EventEntity) {
    eventDao.insertEvent(event)
    setCurrentEventId(event.id)
  }

  suspend fun updateEvent(event: EventEntity) {
    eventDao.updateEvent(event)
  }

  suspend fun deleteEvent(eventId: String) {
    guestDao.deleteGuestsForEvent(eventId)
    eventDao.deleteEventById(eventId)
    val current = preferenceDao.getPreference(KEY_CURRENT_EVENT_ID)
    if (current == eventId) {
      val remaining = allEvents.firstOrNull()?.firstOrNull { it.id != eventId }
      preferenceDao.setPreference(
        AppPreferenceEntity(KEY_CURRENT_EVENT_ID, remaining?.id ?: "")
      )
    }
  }

  // --- Guests ---
  val allGuests: Flow<List<GuestEntity>> = guestDao.getAllGuests()
  val totalGuestCount: Flow<Int> = guestDao.getTotalGuestCount()

  fun getGuestsForEvent(eventId: String): Flow<List<GuestEntity>> = guestDao.getGuestsForEvent(eventId)
  suspend fun getGuestsListForEvent(eventId: String): List<GuestEntity> = guestDao.getGuestsListForEvent(eventId)
  fun getGuestCountForEvent(eventId: String): Flow<Int> = guestDao.getGuestCountForEvent(eventId)
  suspend fun getGuestByToken(token: String): GuestEntity? = guestDao.getGuestByToken(token)
  suspend fun insertGuest(guest: GuestEntity) = guestDao.insertGuest(guest)
  suspend fun insertGuests(guests: List<GuestEntity>) = guestDao.insertGuests(guests)
  suspend fun regenerateToken(id: String, newToken: String) = guestDao.regenerateToken(id, newToken)
  suspend fun setGuestActive(id: String, isActive: Boolean) = guestDao.setGuestActive(id, isActive)
  suspend fun deleteGuest(id: String) = guestDao.deleteGuestById(id)

  // --- Preferences (Current Event) ---
  val currentEventIdFlow: Flow<String?> = preferenceDao.getPreferenceFlow(KEY_CURRENT_EVENT_ID)

  suspend fun getCurrentEventId(): String? = preferenceDao.getPreference(KEY_CURRENT_EVENT_ID)

  suspend fun setCurrentEventId(eventId: String) {
    preferenceDao.setPreference(AppPreferenceEntity(KEY_CURRENT_EVENT_ID, eventId))
  }

  // --- Firestore Invitation Publishing ---
  suspend fun publishInvitation(eventId: String, guestId: String): Result<PublishResult> =
    publishingRepository.publishInvitation(eventId, guestId)

  suspend fun publishEventInvitations(eventId: String): Result<PublishEventResult> =
    publishingRepository.publishEventInvitations(eventId)

  suspend fun updatePublishedInvitation(eventId: String, guestId: String): Result<PublishResult> =
    publishingRepository.updatePublishedInvitation(eventId, guestId)

  suspend fun updatePublishedEventInvitations(eventId: String): Result<PublishEventResult> =
    publishingRepository.updatePublishedEventInvitations(eventId)

  suspend fun deactivatePublishedInvitation(uniqueToken: String): Result<Unit> =
    publishingRepository.deactivatePublishedInvitation(uniqueToken)

  suspend fun getPublishedInvitation(uniqueToken: String): Result<Map<String, Any?>?> =
    publishingRepository.getPublishedInvitation(uniqueToken)

  /**
   * Connects the guest share action to Firebase Anonymous Auth and Firestore publishing:
   * 1. Ensures Firebase anonymous authentication is available via ensureAuthenticated().
   * 2. Publishes the guest's invitation using FirestorePublishingRepository.publishInvitation.
   *    (Uses existing GuestEntity.uniqueToken for the Firestore document publishedInvitations/{uniqueToken}).
   * 3. On success, returns the guest-specific web invitation URL reusing the existing uniqueToken.
   * 4. If authentication or publishing fails, returns Result.failure without opening a broken share URL.
   */
  suspend fun publishAndGetShareUrl(eventId: String, guestId: String): Result<String> {
    val authResult = ensureAuthenticated()
    if (authResult.isFailure) {
      return Result.failure(
        authResult.exceptionOrNull()
          ?: IllegalStateException("Unable to publish invitation: authentication failed.")
      )
    }

    val publishResult = publishInvitation(eventId, guestId)
    return if (publishResult.isSuccess) {
      val result = publishResult.getOrThrow()
      Result.success(buildShareUrl(result.token))
    } else {
      Result.failure(
        publishResult.exceptionOrNull()
          ?: IllegalStateException("Unable to publish invitation: publishing failed.")
      )
    }
  }

  // --- Firebase Authentication ---
  fun isAuthenticated(): Boolean = authRepository.isAuthenticated()
  fun getCurrentUserId(): String? = authRepository.getCurrentUserId()
  suspend fun ensureAuthenticated(): Result<String> = authRepository.ensureAuthenticated()
}
