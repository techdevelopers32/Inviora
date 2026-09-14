package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.data.model.AnimationExperienceEntity
import com.example.data.model.DesignTemplateEntity
import com.example.data.model.EventEntity
import com.example.data.model.GuestEntity
import com.example.ui.InvioraViewModel
import com.example.ui.components.DrawerDestination
import com.example.ui.components.InvioraBottomNav
import com.example.ui.components.InvioraDrawerContent
import com.example.ui.components.InvioraHeader
import com.example.ui.components.NavTab
import com.example.ui.screens.AboutDialog
import com.example.ui.screens.AnimationLibraryScreen
import com.example.ui.screens.CreateAnimationScreen
import com.example.ui.screens.CreateDesignScreen
import com.example.ui.screens.CreateEditEventScreen
import com.example.ui.screens.DesignLibraryScreen
import com.example.ui.screens.EventsScreen
import com.example.ui.screens.GuestExperienceScreen
import com.example.ui.screens.GuestsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsDialog
import com.example.ui.theme.InvioraTheme
import com.example.ui.theme.IvoryBg
import kotlinx.coroutines.launch

sealed class AppScreen {
  object MainTabs : AppScreen()
  data class CreateEditEvent(val event: EventEntity? = null) : AppScreen()
  object DesignLibrary : AppScreen()
  object CreateDesign : AppScreen()
  object AnimationLibrary : AppScreen()
  object CreateAnimation : AppScreen()
  data class GuestExperience(
    val event: EventEntity,
    val guest: GuestEntity?,
    val design: DesignTemplateEntity?,
    val animation: AnimationExperienceEntity?
  ) : AppScreen()
}

class MainActivity : ComponentActivity() {

  private val viewModel: InvioraViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      InvioraTheme {
        InvioraApp(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun InvioraApp(viewModel: InvioraViewModel) {
  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val scope = rememberCoroutineScope()

  var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.MainTabs) }
  var currentTab by remember { mutableStateOf(NavTab.HOME) }

  var showSettingsDialog by remember { mutableStateOf(false) }
  var showAboutDialog by remember { mutableStateOf(false) }

  // State flows from ViewModel
  val allDesigns by viewModel.allDesigns.collectAsState()
  val allAnimations by viewModel.allAnimations.collectAsState()
  val allEvents by viewModel.allEvents.collectAsState()
  val currentEvent by viewModel.currentEvent.collectAsState()
  val currentEventId by viewModel.currentEventId.collectAsState()
  val currentDesign by viewModel.currentDesign.collectAsState()
  val currentAnimation by viewModel.currentAnimation.collectAsState()
  val currentGuests by viewModel.currentEventGuests.collectAsState()
  val currentGuestCount by viewModel.currentEventGuestCount.collectAsState()

  // Handle system back button
  BackHandler(enabled = drawerState.isOpen || currentScreen !is AppScreen.MainTabs) {
    if (drawerState.isOpen) {
      scope.launch { drawerState.close() }
    } else if (currentScreen !is AppScreen.MainTabs) {
      currentScreen = AppScreen.MainTabs
    }
  }

  ModalNavigationDrawer(
    drawerState = drawerState,
    gesturesEnabled = currentScreen is AppScreen.MainTabs,
    drawerContent = {
      InvioraDrawerContent(
        currentDestination = when (currentScreen) {
          is AppScreen.DesignLibrary -> DrawerDestination.DesignLibrary
          is AppScreen.CreateDesign -> DrawerDestination.CreateDesign
          is AppScreen.AnimationLibrary -> DrawerDestination.AnimationLibrary
          is AppScreen.CreateAnimation -> DrawerDestination.CreateAnimation
          else -> when (currentTab) {
            NavTab.HOME -> DrawerDestination.Home
            NavTab.EVENTS -> DrawerDestination.Events
            NavTab.GUESTS -> DrawerDestination.Guests
          }
        },
        onSelectDestination = { dest ->
          scope.launch { drawerState.close() }
          when (dest) {
            DrawerDestination.Home -> {
              currentTab = NavTab.HOME
              currentScreen = AppScreen.MainTabs
            }
            DrawerDestination.Events -> {
              currentTab = NavTab.EVENTS
              currentScreen = AppScreen.MainTabs
            }
            DrawerDestination.Guests -> {
              currentTab = NavTab.GUESTS
              currentScreen = AppScreen.MainTabs
            }
            DrawerDestination.CreateDesign -> {
              currentScreen = AppScreen.CreateDesign
            }
            DrawerDestination.DesignLibrary -> {
              currentScreen = AppScreen.DesignLibrary
            }
            DrawerDestination.CreateAnimation -> {
              currentScreen = AppScreen.CreateAnimation
            }
            DrawerDestination.AnimationLibrary -> {
              currentScreen = AppScreen.AnimationLibrary
            }
            DrawerDestination.Settings -> {
              showSettingsDialog = true
            }
            DrawerDestination.About -> {
              showAboutDialog = true
            }
          }
        },
        onClose = {
          scope.launch { drawerState.close() }
        }
      )
    }
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(IvoryBg)
    ) {
      when (val screen = currentScreen) {
        is AppScreen.MainTabs -> {
          Scaffold(
            topBar = {
              InvioraHeader(
                onMenuClick = { scope.launch { drawerState.open() } }
              )
            },
            bottomBar = {
              InvioraBottomNav(
                currentTab = currentTab,
                onTabSelected = { currentTab = it }
              )
            }
          ) { innerPadding ->
            Box(
              modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
            ) {
              AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tabs_crossfade"
              ) { tab ->
                when (tab) {
                  NavTab.HOME -> {
                    HomeScreen(
                      currentEvent = currentEvent,
                      currentDesign = currentDesign,
                      currentAnimation = currentAnimation,
                      guestCount = currentGuestCount,
                      onCreateEventClick = {
                        currentScreen = AppScreen.CreateEditEvent(null)
                      },
                      onContinueWorkingClick = { evt ->
                        currentScreen = AppScreen.CreateEditEvent(evt)
                      },
                      onSwitchEventClick = {
                        currentTab = NavTab.EVENTS
                      },
                      onPreviewMasterClick = { evt ->
                        val dsg = allDesigns.firstOrNull { it.id == evt.designId } ?: currentDesign
                        val anm = allAnimations.firstOrNull { it.id == evt.animationId } ?: currentAnimation
                        currentScreen = AppScreen.GuestExperience(evt, null, dsg, anm)
                      },
                      onManageGuestsClick = {
                        currentTab = NavTab.GUESTS
                      }
                    )
                  }
                  NavTab.EVENTS -> {
                    EventsScreen(
                      events = allEvents,
                      currentEventId = currentEventId,
                      onSelectEvent = { evt ->
                        viewModel.selectCurrentEvent(evt.id)
                      },
                      onCreateEventClick = {
                        currentScreen = AppScreen.CreateEditEvent(null)
                      },
                      onEditEventClick = { evt ->
                        viewModel.selectCurrentEvent(evt.id)
                        currentScreen = AppScreen.CreateEditEvent(evt)
                      },
                      onDeleteEventClick = { evt ->
                        viewModel.deleteEvent(evt.id)
                      },
                      onPreviewEventClick = { evt ->
                        val dsg = allDesigns.firstOrNull { it.id == evt.designId } ?: currentDesign
                        val anm = allAnimations.firstOrNull { it.id == evt.animationId } ?: currentAnimation
                        currentScreen = AppScreen.GuestExperience(evt, null, dsg, anm)
                      }
                    )
                  }
                  NavTab.GUESTS -> {
                    GuestsScreen(
                      currentEvent = currentEvent,
                      allEvents = allEvents,
                      guests = currentGuests,
                      currentDesign = currentDesign,
                      allDesigns = allDesigns,
                      onSelectEvent = { evt ->
                        viewModel.selectCurrentEvent(evt.id)
                      },
                      onAddGuest = { guest ->
                        viewModel.saveGuest(guest)
                      },
                      onUpdateEvent = { evt ->
                        viewModel.saveEvent(evt)
                      },
                      onAddBulkGuests = { guests ->
                        viewModel.saveBulkGuests(guests)
                      },
                      onRegenerateToken = { guestId ->
                        viewModel.regenerateGuestToken(guestId)
                      },
                      onDeleteGuest = { guest ->
                        viewModel.deleteGuest(guest.id)
                      },
                      onTestGuestExperience = { guest, evt ->
                        val dsg = allDesigns.firstOrNull { it.id == evt.designId } ?: currentDesign
                        val anm = allAnimations.firstOrNull { it.id == evt.animationId } ?: currentAnimation
                        currentScreen = AppScreen.GuestExperience(evt, guest, dsg, anm)
                      }
                    )
                  }
                }
              }
            }
          }
        }

        is AppScreen.CreateEditEvent -> {
          CreateEditEventScreen(
            existingEvent = screen.event,
            allDesigns = allDesigns,
            allAnimations = allAnimations,
            onSaveEvent = { event ->
              viewModel.saveEvent(event) {
                currentScreen = AppScreen.MainTabs
                currentTab = NavTab.HOME
              }
            },
            onDeletePage = { eventId, pageId ->
              viewModel.deletePageFromEvent(eventId, pageId)
            },
            onCancel = { currentScreen = AppScreen.MainTabs },
            onPreviewMaster = { evt, dsg, anm ->
              currentScreen = AppScreen.GuestExperience(evt, null, dsg, anm)
            }
          )
        }

        is AppScreen.DesignLibrary -> {
          Scaffold(
            topBar = {
              InvioraHeader(
                onMenuClick = { scope.launch { drawerState.open() } }
              )
            }
          ) { innerPadding ->
            DesignLibraryScreen(
              designs = allDesigns,
              onCreateDesignClick = { currentScreen = AppScreen.CreateDesign },
              onPreviewDesign = { design ->
                val evt = currentEvent ?: EventEntity(
                  id = "preview_evt",
                  title = "Celebration of Moments",
                  eventType = "Wedding",
                  hostNames = "",
                  primaryVenue = "Grand Palace",
                  primaryDate = "December 18, 2026",
                  notes = "",
                  designId = design.id,
                  animationId = "anim_1"
                )
                currentScreen = AppScreen.GuestExperience(evt, null, design, currentAnimation)
              },
              onRenameDesign = { dsgId, newName ->
                viewModel.renameDesign(dsgId, newName)
              },
              onToggleFavorite = { dsgId, isFav ->
                viewModel.toggleDesignFavorite(dsgId, isFav)
              },
              checkEventsUsingDesign = { dsgId ->
                viewModel.checkEventsUsingDesign(dsgId)
              },
              onDeleteDesign = { dsgId, cascade ->
                viewModel.deleteDesign(dsgId, cascade)
              },
              modifier = Modifier.padding(innerPadding)
            )
          }
        }

        is AppScreen.CreateDesign -> {
          CreateDesignScreen(
            onSaveDesign = { design ->
              viewModel.saveDesign(design) {
                currentScreen = AppScreen.DesignLibrary
              }
            },
            onCancel = { currentScreen = AppScreen.DesignLibrary }
          )
        }

        is AppScreen.AnimationLibrary -> {
          Scaffold(
            topBar = {
              InvioraHeader(
                onMenuClick = { scope.launch { drawerState.open() } }
              )
            }
          ) { innerPadding ->
            AnimationLibraryScreen(
              animations = allAnimations,
              allEvents = allEvents,
              currentEvent = currentEvent,
              allDesigns = allDesigns,
              currentDesign = currentDesign,
              onCreateAnimationClick = { currentScreen = AppScreen.CreateAnimation },
              onPreviewAnimation = { anim ->
                val evt = currentEvent ?: EventEntity(
                  id = "preview_evt",
                  title = "Celebration of Moments",
                  eventType = "Wedding",
                  hostNames = "",
                  primaryVenue = "Grand Palace",
                  primaryDate = "December 18, 2026",
                  notes = "",
                  designId = currentDesign?.id ?: "design_1",
                  animationId = anim.id
                )
                currentScreen = AppScreen.GuestExperience(evt, null, currentDesign, anim)
              },
              onApplyAnimationToEvent = { anim, evt ->
                viewModel.saveEvent(evt.copy(animationId = anim.id))
              },
              checkEventsUsingAnimation = { animId ->
                viewModel.checkEventsUsingAnimation(animId)
              },
              onDeleteAnimation = { animId, cascade ->
                viewModel.deleteAnimation(animId, cascade)
              },
              modifier = Modifier.padding(innerPadding)
            )
          }
        }

        is AppScreen.CreateAnimation -> {
          CreateAnimationScreen(
            onSaveAnimation = { anim ->
              viewModel.saveAnimation(anim) {
                currentScreen = AppScreen.AnimationLibrary
              }
            },
            onGenerateAI = { base64, prompt, refinement, onResult, onError ->
              viewModel.generateAnimation(base64, prompt, refinement, onResult, onError)
            },
            onCancel = { currentScreen = AppScreen.AnimationLibrary }
          )
        }

        is AppScreen.GuestExperience -> {
          GuestExperienceScreen(
            event = screen.event,
            guest = screen.guest,
            allDesigns = allDesigns,
            animation = screen.animation,
            onClose = { currentScreen = AppScreen.MainTabs }
          )
        }
      }
    }
  }

  if (showSettingsDialog) {
    SettingsDialog(onDismiss = { showSettingsDialog = false })
  }

  if (showAboutDialog) {
    AboutDialog(onDismiss = { showAboutDialog = false })
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  androidx.compose.material3.Text(text = "Hello $name!", modifier = modifier)
}

