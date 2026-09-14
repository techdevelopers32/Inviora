package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ai.GeneratedAnimationResult
import com.example.data.model.AnimationExperienceEntity
import com.example.data.model.DesignTemplateEntity
import com.example.data.model.EventEntity
import com.example.ui.components.InvitationCardRenderer
import com.example.ui.components.LuxuryCurtainScene
import com.example.ui.components.RoyalDoorsScene
import com.example.ui.components.WaxSealEnvelopeScene
import com.example.ui.theme.CharcoalMuted
import com.example.ui.theme.CharcoalPrimary
import com.example.ui.theme.CharcoalSecondary
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.IvoryBg
import com.example.ui.theme.IvoryBorder
import com.example.ui.theme.IvoryBorderStrong
import com.example.ui.theme.IvorySurface
import com.example.ui.theme.IvorySurfaceLight
import com.example.util.ImageUtils
import kotlinx.coroutines.launch

enum class GenerationState {
  IDLE,
  GENERATING,
  PREVIEW_READY,
  SAVED,
  ERROR
}

@Composable
fun CreateAnimationScreen(
  onSaveAnimation: (AnimationExperienceEntity) -> Unit,
  onGenerateAI: (String, String, String?, (GeneratedAnimationResult) -> Unit, (String) -> Unit) -> Unit,
  onCancel: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
  var prompt by remember {
    mutableStateOf("Create a realistic cinematic animation where the curtain opens slowly from the center and reveals the invitation behind it.")
  }
  var refinementPrompt by remember { mutableStateOf("") }

  var generationState by remember { mutableStateOf(GenerationState.IDLE) }
  var generatedResult by remember { mutableStateOf<GeneratedAnimationResult?>(null) }
  var editedAnimationName by remember { mutableStateOf("") }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  // Playback control key to force re-running the animation
  var playbackSessionId by remember { mutableStateOf(0) }
  var isPlaying by remember { mutableStateOf(false) }

  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    if (uri != null) {
      selectedImageUri = uri
      errorMessage = null
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(IvoryBg)
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 20.dp, vertical = 16.dp)
  ) {
    // Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = onCancel,
        modifier = Modifier.testTag("create_animation_back_button")
      ) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = CharcoalPrimary)
      }
      Spacer(modifier = Modifier.width(6.dp))
      Column {
        Text(
          text = "Create Animation",
          fontFamily = FontFamily.Serif,
          fontSize = 20.sp,
          color = CharcoalPrimary
        )
        Text(
          text = "Synthesize cinematic opening from your reference visual",
          fontSize = 11.sp,
          color = CharcoalMuted
        )
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // STEP 1: Animation Reference (User-provided image only, no hardcoded choices)
    Text(
      text = "1. ANIMATION REFERENCE",
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 1.6.sp,
      color = GoldDark
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = "Select a photo of curtains, palace doors, wax seal envelopes, or keepsake boxes.",
      fontSize = 12.sp,
      color = CharcoalMuted
    )
    Spacer(modifier = Modifier.height(10.dp))

    if (selectedImageUri == null) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .border(1.dp, IvoryBorderStrong, RoundedCornerShape(12.dp))
          .clickable {
            photoPickerLauncher.launch(
              PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
          }
          .testTag("select_animation_reference_card"),
        colors = CardDefaults.cardColors(containerColor = IvorySurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(28.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Box(
            modifier = Modifier
              .size(54.dp)
              .clip(CircleShape)
              .background(IvorySurfaceLight),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.AddPhotoAlternate,
              contentDescription = null,
              tint = GoldPrimary,
              modifier = Modifier.size(28.dp)
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = "Select Reference Image",
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = CharcoalPrimary
          )

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = "Tap to choose an opening physical reference from gallery",
            fontSize = 12.sp,
            color = CharcoalMuted
          )
        }
      }
    } else {
      // Visual confirmation
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, IvoryBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = IvorySurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            AsyncImage(
              model = selectedImageUri,
              contentDescription = "Animation Reference Photo",
              modifier = Modifier
                .size(90.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(0.8.dp, IvoryBorderStrong, RoundedCornerShape(8.dp)),
              contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = null,
                  tint = Color(0xFF2E6F40),
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Reference photo selected ✓",
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 13.sp,
                  color = Color(0xFF2E6F40)
                )
              }

              Spacer(modifier = Modifier.height(4.dp))

              Text(
                text = "Ready to analyze physical structure, material & cinematic reveal.",
                fontSize = 11.sp,
                color = CharcoalMuted
              )

              Spacer(modifier = Modifier.height(10.dp))

              OutlinedButton(
                onClick = {
                  photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                  )
                },
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CharcoalPrimary),
                modifier = Modifier
                  .height(34.dp)
                  .testTag("change_animation_photo_button")
              ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Change Photo", fontSize = 11.sp)
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // STEP 2: Movement Instruction Prompt
    Text(
      text = "2. MOVEMENT INSTRUCTION PROMPT",
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 1.6.sp,
      color = GoldDark
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = "Describe how the element physically opens, its timing, and illumination.",
      fontSize = 12.sp,
      color = CharcoalMuted
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
      value = prompt,
      onValueChange = { prompt = it },
      label = { Text("Movement Instructions") },
      shape = RoundedCornerShape(8.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = GoldPrimary,
        unfocusedBorderColor = IvoryBorderStrong,
        focusedTextColor = CharcoalPrimary,
        unfocusedTextColor = CharcoalSecondary,
        focusedContainerColor = IvorySurface,
        unfocusedContainerColor = IvorySurface
      ),
      modifier = Modifier
        .fillMaxWidth()
        .height(100.dp)
        .testTag("animation_prompt_input")
    )

    Spacer(modifier = Modifier.height(20.dp))

    // GENERATE BUTTON
    ElevatedButton(
      onClick = {
        val uri = selectedImageUri
        if (uri == null) {
          errorMessage = "Please select an animation reference photo first."
          return@ElevatedButton
        }

        generationState = GenerationState.GENERATING
        errorMessage = null

        coroutineScope.launch {
          val base64 = ImageUtils.uriToBase64(context, uri)
          if (base64.isNullOrBlank()) {
            errorMessage = "Could not process selected image. Please choose another image."
            generationState = GenerationState.IDLE
            return@launch
          }

          onGenerateAI(
            base64,
            prompt,
            null,
            { result ->
              generatedResult = result
              editedAnimationName = result.name
              playbackSessionId++
              isPlaying = true
              generationState = GenerationState.PREVIEW_READY
            },
            { error ->
              errorMessage = error
              generationState = GenerationState.IDLE
            }
          )
        }
      },
      enabled = generationState != GenerationState.GENERATING,
      colors = ButtonDefaults.elevatedButtonColors(
        containerColor = GoldPrimary,
        contentColor = Color.White
      ),
      shape = RoundedCornerShape(8.dp),
      modifier = Modifier
        .fillMaxWidth()
        .height(50.dp)
        .shadow(4.dp, RoundedCornerShape(8.dp))
        .testTag("generate_animation_button")
    ) {
      if (generationState == GenerationState.GENERATING) {
        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        Spacer(modifier = Modifier.width(10.dp))
        Text("Synthesizing Motion Physics & Reveal...", fontFamily = FontFamily.Serif, fontSize = 13.sp)
      } else {
        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (generatedResult != null) "Regenerate Animation" else "Generate Animation with AI",
          fontFamily = FontFamily.Serif,
          fontWeight = FontWeight.SemiBold,
          fontSize = 14.sp
        )
      }
    }

    errorMessage?.let { err ->
      Spacer(modifier = Modifier.height(12.dp))
      Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFDECEA)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = err,
          color = Color(0xFFB71C1C),
          fontSize = 12.sp,
          modifier = Modifier.padding(12.dp)
        )
      }
    }

    // STEP 3: LIVE PLAYABLE ANIMATION PREVIEW & ITERATIVE REFINEMENT
    generatedResult?.let { res ->
      Spacer(modifier = Modifier.height(28.dp))

      Text(
        text = "3. LIVE ANIMATION PREVIEW",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.6.sp,
        color = GoldDark
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Editable Name
      OutlinedTextField(
        value = editedAnimationName,
        onValueChange = { editedAnimationName = it },
        label = { Text("Animation Experience Name") },
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = GoldPrimary,
          unfocusedBorderColor = IvoryBorderStrong,
          focusedTextColor = CharcoalPrimary,
          unfocusedTextColor = CharcoalSecondary,
          focusedContainerColor = IvorySurface,
          unfocusedContainerColor = IvorySurface
        ),
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Characteristics card
      Card(
        colors = CardDefaults.cardColors(containerColor = IvorySurface),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
          .fillMaxWidth()
          .border(0.5.dp, IvoryBorder, RoundedCornerShape(8.dp))
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text(
            text = "Type: ${res.animationType} • Duration: ${res.motionTimingMs}ms",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = CharcoalPrimary
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "Lighting: ${res.lightingMood}",
            fontSize = 11.sp,
            color = CharcoalMuted
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Playback Controls Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Button(
          onClick = {
            playbackSessionId++
            isPlaying = true
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = GoldPrimary,
            contentColor = Color.White
          ),
          shape = RoundedCornerShape(6.dp),
          modifier = Modifier
            .weight(1f)
            .height(44.dp)
            .testTag("play_animation_preview_button")
        ) {
          Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Play Animation", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        }

        OutlinedButton(
          onClick = {
            playbackSessionId++
            isPlaying = false
          },
          shape = RoundedCornerShape(6.dp),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = CharcoalPrimary),
          modifier = Modifier
            .weight(0.8f)
            .height(44.dp)
        ) {
          Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Replay", fontSize = 12.sp)
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Interactive Container with Simulated Invitation Inside
      val mockEvent = EventEntity(
        id = "mock_evt",
        title = "Ahmed & Ayesha Wedding",
        eventType = "Wedding",
        hostNames = "Mr. & Mrs. Mansoor Khan",
        primaryVenue = "The Rosewood Palace",
        primaryDate = "December 18, 2026",
        notes = "Honored to celebrate with you.",
        designId = "design_royal_gold",
        animationId = res.id
      )

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(440.dp)
          .clip(RoundedCornerShape(12.dp))
          .border(1.dp, IvoryBorderStrong, RoundedCornerShape(12.dp))
          .background(IvorySurface)
      ) {
        // Remount scene when playbackSessionId changes
        androidx.compose.runtime.key(playbackSessionId) {
          when (res.animationType) {
            "PALACE_DOORS" -> RoyalDoorsScene(isOpenInitially = false) {
              InvitationCardRenderer(event = mockEvent, design = null)
            }
            "WAX_SEAL" -> WaxSealEnvelopeScene(isOpenInitially = false) {
              InvitationCardRenderer(event = mockEvent, design = null)
            }
            else -> LuxuryCurtainScene(isOpenInitially = false) {
              InvitationCardRenderer(event = mockEvent, design = null)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // ITERATIVE REFINEMENT SECTION
      Text(
        text = "MODIFY & REFINE ANIMATION",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.4.sp,
        color = GoldDark
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "Not fully satisfied? Provide a modification instruction (e.g., 'Make the curtain open slower and add warmer illumination').",
        fontSize = 12.sp,
        color = CharcoalMuted
      )
      Spacer(modifier = Modifier.height(8.dp))

      OutlinedTextField(
        value = refinementPrompt,
        onValueChange = { refinementPrompt = it },
        placeholder = { Text("E.g. Make reveal movement slower and more cinematic...") },
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = GoldPrimary,
          unfocusedBorderColor = IvoryBorderStrong,
          focusedTextColor = CharcoalPrimary,
          unfocusedTextColor = CharcoalSecondary,
          focusedContainerColor = IvorySurface,
          unfocusedContainerColor = IvorySurface
        ),
        modifier = Modifier
          .fillMaxWidth()
          .height(85.dp)
          .testTag("animation_refinement_input")
      )

      Spacer(modifier = Modifier.height(8.dp))

      OutlinedButton(
        onClick = {
          val uri = selectedImageUri ?: return@OutlinedButton
          if (refinementPrompt.isBlank()) return@OutlinedButton

          generationState = GenerationState.GENERATING
          errorMessage = null

          coroutineScope.launch {
            val base64 = ImageUtils.uriToBase64(context, uri) ?: return@launch
            onGenerateAI(
              base64,
              prompt,
              refinementPrompt,
              { updatedResult ->
                generatedResult = updatedResult
                editedAnimationName = updatedResult.name
                playbackSessionId++
                isPlaying = true
                refinementPrompt = ""
                generationState = GenerationState.PREVIEW_READY
              },
              { err ->
                errorMessage = err
                generationState = GenerationState.PREVIEW_READY
              }
            )
          }
        },
        enabled = refinementPrompt.isNotBlank() && generationState != GenerationState.GENERATING,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldDark),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(44.dp)
          .testTag("refine_animation_button")
      ) {
        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Apply Modification & Refine", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
      }

      Spacer(modifier = Modifier.height(18.dp))

      // SAVE TO ANIMATION LIBRARY
      val finalAnim = AnimationExperienceEntity(
        id = res.id,
        name = editedAnimationName.ifBlank { res.name },
        referenceDrawable = selectedImageUri?.toString() ?: "custom_anim",
        animationType = res.animationType,
        motionTimingMs = res.motionTimingMs,
        lightingMood = res.lightingMood,
        description = res.description
      )

      Button(
        onClick = {
          onSaveAnimation(finalAnim)
          generationState = GenerationState.SAVED
        },
        colors = ButtonDefaults.buttonColors(
          containerColor = GoldPrimary,
          contentColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("save_generated_animation_button")
      ) {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (generationState == GenerationState.SAVED) "Saved to Animation Library ✓" else "Save to Animation Library",
          fontFamily = FontFamily.Serif,
          fontWeight = FontWeight.SemiBold,
          fontSize = 14.sp
        )
      }
    }

    Spacer(modifier = Modifier.height(40.dp))
  }
}
