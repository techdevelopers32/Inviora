package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.EditedDesignResult
import com.example.ai.GeminiService
import com.example.ai.GeneratedAnimationResult
import com.example.ai.GeneratedDesignResult
import com.example.data.AppDatabase
import com.example.data.model.AnimationExperienceEntity
import com.example.data.model.DesignTemplateEntity
import com.example.data.model.EventEntity
import com.example.data.model.EventPage
import com.example.data.model.GuestEntity
import com.example.data.model.PageGuestStyle
import com.example.data.repository.InvioraRepository
import com.example.data.repository.PublishEventResult
import com.example.data.repository.PublishResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InvioraViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: InvioraRepository

  val allDesigns: StateFlow<List<DesignTemplateEntity>>
  val allAnimations: StateFlow<List<AnimationExperienceEntity>>
  val allEvents: StateFlow<List<EventEntity>>
  val currentEventId: StateFlow<String?>
  val totalGuestCount: StateFlow<Int>

  init {
    val database = AppDatabase.getDatabase(application, viewModelScope)
    repository = InvioraRepository(
      database.designDao(),
      database.animationDao(),
      database.eventDao(),
      database.guestDao(),
      database.preferenceDao()
    )

    allDesigns = repository.allDesigns.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      emptyList()
    )

    allAnimations = repository.allAnimations.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      emptyList()
    )

    allEvents = repository.allEvents.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      emptyList()
    )

    currentEventId = repository.currentEventIdFlow.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      null
    )

    totalGuestCount = repository.totalGuestCount.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      0
    )

    // Ensure Firebase Anonymous Authentication is ready in background.
    // If already authenticated, the existing anonymous UID is retained and reused.
    // If not authenticated, signs in anonymously.
    // Any error is caught safely without affecting local Room database operations or app UI.
    viewModelScope.launch {
      try {
        repository.ensureAuthenticated()
          .onSuccess { uid ->
            Log.d("InvioraViewModel", "Firebase anonymous authentication initialized: $uid")
          }
          .onFailure { error ->
            Log.w("InvioraViewModel", "Firebase anonymous authentication deferred/failed safely: ${error.message}")
          }
      } catch (e: Throwable) {
        Log.w("InvioraViewModel", "Safe startup auth catch: ${e.message}")
      }
    }
  }

  // Current active event flow
  val currentEvent: StateFlow<EventEntity?> = combine(allEvents, currentEventId) { events, currentId ->
    if (currentId.isNullOrBlank()) {
      events.firstOrNull()
    } else {
      events.firstOrNull { it.id == currentId } ?: events.firstOrNull()
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  // Current design
  val currentDesign: StateFlow<DesignTemplateEntity?> = combine(currentEvent, allDesigns) { event, designs ->
    event?.let { e -> designs.firstOrNull { it.id == e.designId } } ?: designs.firstOrNull()
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  // Current animation
  val currentAnimation: StateFlow<AnimationExperienceEntity?> = combine(currentEvent, allAnimations) { event, anims ->
    event?.let { e -> anims.firstOrNull { it.id == e.animationId } } ?: anims.firstOrNull()
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  // Guests for current event
  val currentEventGuests: StateFlow<List<GuestEntity>> = currentEvent.flatMapLatest { event ->
    if (event != null) {
      repository.getGuestsForEvent(event.id)
    } else {
      flowOf(emptyList())
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Guest count for current event
  val currentEventGuestCount: StateFlow<Int> = currentEvent.flatMapLatest { event ->
    if (event != null) {
      repository.getGuestCountForEvent(event.id)
    } else {
      flowOf(0)
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

  // Operations
  fun selectCurrentEvent(eventId: String) {
    viewModelScope.launch {
      repository.setCurrentEventId(eventId)
    }
  }

  fun saveEvent(event: EventEntity, onComplete: () -> Unit = {}) {
    viewModelScope.launch {
      repository.insertEvent(event)
      // Synchronize guests of this event: prune deleted page IDs
      val validPageIds = event.getPages().map { it.id }.toSet()
      val eventGuests = repository.getGuestsListForEvent(event.id)
      val guestsToUpdate = mutableListOf<GuestEntity>()
      for (guest in eventGuests) {
        val originalIds = guest.getSelectedPageIds()
        val filteredIds = originalIds.filter { validPageIds.contains(it) }
        if (filteredIds.size != originalIds.size) {
          val overrides = PageGuestStyle.parseMap(guest.pageOverridesJson).toMutableMap()
          overrides.keys.retainAll(validPageIds)
          guestsToUpdate.add(
            guest.copy(
              selectedPageIdsJson = GuestEntity.idsToJson(filteredIds),
              pageOverridesJson = PageGuestStyle.mapToJson(overrides)
            )
          )
        }
      }
      if (guestsToUpdate.isNotEmpty()) {
        repository.insertGuests(guestsToUpdate)
      }
      onComplete()
    }
  }

  fun deletePageFromEvent(eventId: String, pageId: String, onComplete: () -> Unit = {}) {
    viewModelScope.launch {
      val event = repository.getEventById(eventId) ?: return@launch
      val remainingPages = event.getPages().filter { it.id != pageId }.mapIndexed { idx, p -> p.copy(pageOrder = idx) }
      if (remainingPages.isEmpty()) return@launch // Never leave an event with zero pages

      val updatedEvent = event.copy(pagesJson = EventPage.listToJson(remainingPages))
      repository.insertEvent(updatedEvent)

      // Prune pageId from all guests of this event without deleting the guests
      val guests = repository.getGuestsListForEvent(eventId)
      val updatedGuests = mutableListOf<GuestEntity>()
      for (g in guests) {
        val pIds = g.getSelectedPageIds()
        if (pIds.contains(pageId)) {
          val filtered = pIds.filter { it != pageId }
          val overrides = PageGuestStyle.parseMap(g.pageOverridesJson).toMutableMap()
          overrides.remove(pageId)
          updatedGuests.add(
            g.copy(
              selectedPageIdsJson = GuestEntity.idsToJson(filtered),
              pageOverridesJson = PageGuestStyle.mapToJson(overrides)
            )
          )
        }
      }
      if (updatedGuests.isNotEmpty()) {
        repository.insertGuests(updatedGuests)
      }
      onComplete()
    }
  }

  fun deleteEvent(eventId: String) {
    viewModelScope.launch {
      repository.deleteEvent(eventId)
    }
  }

  fun saveDesign(design: DesignTemplateEntity, onComplete: () -> Unit = {}) {
    viewModelScope.launch {
      repository.insertDesign(design)
      onComplete()
    }
  }

  fun renameDesign(designId: String, newName: String) {
    viewModelScope.launch {
      repository.renameDesign(designId, newName)
    }
  }

  fun toggleDesignFavorite(designId: String, isFavorite: Boolean) {
    viewModelScope.launch {
      repository.toggleFavoriteDesign(designId, isFavorite)
    }
  }

  suspend fun checkEventsUsingDesign(designId: String): List<EventEntity> {
    return repository.getEventsUsingDesign(designId)
  }

  fun deleteDesign(designId: String, cascade: Boolean = false) {
    viewModelScope.launch {
      if (cascade) {
        repository.deleteDesignWithCascade(designId)
      } else {
        repository.deleteDesign(designId)
      }
    }
  }

  fun saveAnimation(animation: AnimationExperienceEntity, onComplete: () -> Unit = {}) {
    viewModelScope.launch {
      repository.insertAnimation(animation)
      onComplete()
    }
  }

  suspend fun checkEventsUsingAnimation(animationId: String): List<EventEntity> {
    return repository.getEventsUsingAnimation(animationId)
  }

  fun deleteAnimation(animationId: String, cascade: Boolean = false) {
    viewModelScope.launch {
      if (cascade) {
        repository.deleteAnimationWithCascade(animationId)
      } else {
        repository.deleteAnimation(animationId)
      }
    }
  }

  fun saveGuest(guest: GuestEntity, onComplete: () -> Unit = {}) {
    viewModelScope.launch {
      repository.insertGuest(guest)
      onComplete()
    }
  }

  fun saveBulkGuests(guests: List<GuestEntity>, onComplete: () -> Unit = {}) {
    viewModelScope.launch {
      repository.insertGuests(guests)
      onComplete()
    }
  }

  fun regenerateGuestToken(guestId: String) {
    viewModelScope.launch {
      val allowed = "23456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
      val newToken = (1..6).map { allowed.random() }.joinToString("")
      repository.regenerateToken(guestId, newToken)
    }
  }

  fun deleteGuest(guestId: String) {
    viewModelScope.launch {
      repository.deleteGuest(guestId)
    }
  }

  suspend fun getGuestByToken(token: String): GuestEntity? {
    return repository.getGuestByToken(token)
  }

  // AI Generation helpers
  fun editDesignImage(
    base64Image: String,
    prompt: String,
    onResult: (EditedDesignResult) -> Unit,
    onError: (String) -> Unit
  ) {
    viewModelScope.launch {
      val result = GeminiService.editDesignImage(base64Image, prompt)
      result.fold(
        onSuccess = { onResult(it) },
        onFailure = { onError(it.message ?: "Gemini failed to edit image.") }
      )
    }
  }

  fun generateDesign(
    base64Image: String,
    prompt: String,
    onResult: (GeneratedDesignResult) -> Unit,
    onError: (String) -> Unit
  ) {
    viewModelScope.launch {
      val result = GeminiService.generateDesignTemplate(base64Image, prompt)
      result.fold(
        onSuccess = { onResult(it) },
        onFailure = { onError(it.message ?: "Unable to generate this design. Please try again.") }
      )
    }
  }

  fun generateAnimation(
    base64Image: String,
    prompt: String,
    refinementPrompt: String? = null,
    onResult: (GeneratedAnimationResult) -> Unit,
    onError: (String) -> Unit
  ) {
    viewModelScope.launch {
      val result = GeminiService.generateAnimationExperience(base64Image, prompt, refinementPrompt)
      result.fold(
        onSuccess = { onResult(it) },
        onFailure = { onError(it.message ?: "Unable to generate this animation. Please try again.") }
      )
    }
  }

  // --- Firestore Invitation Publishing ---
  suspend fun publishInvitation(eventId: String, guestId: String): Result<PublishResult> =
    repository.publishInvitation(eventId, guestId)

  suspend fun publishEventInvitations(eventId: String): Result<PublishEventResult> =
    repository.publishEventInvitations(eventId)

  suspend fun updatePublishedInvitation(eventId: String, guestId: String): Result<PublishResult> =
    repository.updatePublishedInvitation(eventId, guestId)

  suspend fun updatePublishedEventInvitations(eventId: String): Result<PublishEventResult> =
    repository.updatePublishedEventInvitations(eventId)

  suspend fun deactivatePublishedInvitation(uniqueToken: String): Result<Unit> =
    repository.deactivatePublishedInvitation(uniqueToken)

  suspend fun getPublishedInvitation(uniqueToken: String): Result<Map<String, Any?>?> =
    repository.getPublishedInvitation(uniqueToken)

  /**
   * Connects the guest share flow to Firestore publishing:
   * 1. Ensures Firebase anonymous authentication is available.
   * 2. Publishes the guest's invitation to Firestore (publishedInvitations/{uniqueToken}).
   * 3. Generates and returns the guest-specific web invitation URL reusing the existing uniqueToken.
   * 4. Returns failure if publishing or authentication fails so no invalid URL is shared.
   */
  suspend fun publishAndGetShareUrl(eventId: String, guestId: String): Result<String> =
    repository.publishAndGetShareUrl(eventId, guestId)
}
