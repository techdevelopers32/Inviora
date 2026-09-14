package com.example.ai

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

data class EditedDesignResult(
  val id: String,
  val name: String,
  val editedBase64Image: String,
  val mimeType: String = "image/png",
  val prompt: String
)

data class GeneratedDesignResult(
  val id: String,
  val name: String,
  val themeStyle: String,
  val accentColorHex: String,
  val fontStyle: String,
  val ornamentStyle: String,
  val description: String
)

data class GeneratedAnimationResult(
  val id: String,
  val name: String,
  val animationType: String,
  val motionTimingMs: Long,
  val lightingMood: String,
  val description: String
)

object GeminiService {

  /**
   * Directly sends the user's uploaded reference image and prompt to Gemini image generation/editing model.
   * Gemini edits the image (e.g. removing all text while preserving the exact design) and returns the edited image.
   * NO hardcoded fallback or sample data is used; if Gemini fails or returns no image, an error is returned.
   */
  suspend fun editDesignImage(
    base64Image: String,
    prompt: String
  ): Result<EditedDesignResult> = withContext(Dispatchers.IO) {
    val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (_: Exception) { "" }

    if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
      return@withContext Result.failure(
        IllegalStateException("Gemini API key is required. Please set GEMINI_API_KEY in the AI Studio Secrets panel.")
      )
    }

    if (base64Image.isBlank()) {
      return@withContext Result.failure(
        IllegalArgumentException("Please select an invitation photo first.")
      )
    }

    val models = listOf(
      "gemini-2.5-flash-image",
      "gemini-3.1-flash-image",
      "gemini-3.1-flash-image-preview",
      "gemini-3.1-flash-lite-image",
      "gemini-3-pro-image-preview"
    )
    var lastException: Exception? = null

    for (model in models) {
      val modalityOptions = listOf(listOf("IMAGE"), listOf("TEXT", "IMAGE"))
      for (modalities in modalityOptions) {
        try {
          val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
          val url = URL(endpoint)
          val conn = url.openConnection() as HttpURLConnection
          conn.requestMethod = "POST"
          conn.setRequestProperty("Content-Type", "application/json")
          conn.doOutput = true
          conn.connectTimeout = 60000
          conn.readTimeout = 60000

          val root = JSONObject()
          val contents = JSONArray()
          val contentObj = JSONObject()
          val parts = JSONArray()

          // 1. Text Instruction Part
          val textPart = JSONObject()
          textPart.put("text", prompt)
          parts.put(textPart)

          // 2. Multimodal Inline Image Data Part
          val imagePart = JSONObject()
          val inlineData = JSONObject()
          inlineData.put("mimeType", "image/jpeg")
          inlineData.put("data", base64Image.trim())
          imagePart.put("inlineData", inlineData)
          parts.put(imagePart)

          contentObj.put("parts", parts)
          contents.put(contentObj)
          root.put("contents", contents)

          // Request image output
          val genConfig = JSONObject()
          val respModalities = JSONArray()
          modalities.forEach { respModalities.put(it) }
          genConfig.put("responseModalities", respModalities)
          root.put("generationConfig", genConfig)

          OutputStreamWriter(conn.outputStream).use { it.write(root.toString()) }

          val responseCode = conn.responseCode
          if (responseCode == 200) {
            val responseText = conn.inputStream.bufferedReader().readText()
            val json = JSONObject(responseText)
            val candidates = json.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
              var foundBase64: String? = null
              var foundMimeType = "image/png"
              var finishReasonText: String? = null

              for (i in 0 until candidates.length()) {
                val cand = candidates.getJSONObject(i)
                val finishReason = cand.optString("finishReason", "")
                if (finishReason.isNotBlank() && finishReason != "STOP") {
                  finishReasonText = finishReason
                }
                val cContent = cand.optJSONObject("content")
                val cParts = cContent?.optJSONArray("parts")
                if (cParts != null) {
                  for (j in 0 until cParts.length()) {
                    val p = cParts.getJSONObject(j)
                    val inline = p.optJSONObject("inlineData") ?: p.optJSONObject("inline_data")
                    if (inline != null) {
                      val data = inline.optString("data", "")
                      if (data.isNotBlank()) {
                        foundBase64 = data
                        foundMimeType = inline.optString("mimeType", inline.optString("mime_type", "image/png"))
                        break
                      }
                    }
                  }
                }
                if (foundBase64 != null) break
              }

              if (foundBase64 != null) {
                return@withContext Result.success(
                  EditedDesignResult(
                    id = "design_" + UUID.randomUUID().toString().take(8),
                    name = "Edited Invitation Design",
                    editedBase64Image = foundBase64,
                    mimeType = foundMimeType,
                    prompt = prompt
                  )
                )
              } else {
                val reasonInfo = if (!finishReasonText.isNullOrBlank()) " (finishReason: $finishReasonText)" else ""
                lastException = IllegalStateException("Gemini did not return an edited image$reasonInfo.")
              }
            } else {
              val promptFeedback = json.optJSONObject("promptFeedback")
              val blockReason = promptFeedback?.optString("blockReason")
              lastException = IllegalStateException(
                if (!blockReason.isNullOrBlank()) "Gemini blocked the request: $blockReason"
                else "Gemini returned an empty candidate list."
              )
            }
          } else {
            val errStream = conn.errorStream?.bufferedReader()?.readText() ?: "HTTP $responseCode"
            val formattedMsg = try {
              val errJson = JSONObject(errStream)
              val errObj = errJson.optJSONObject("error")
              val msg = errObj?.optString("message")
              val status = errObj?.optString("status")
              if (!msg.isNullOrBlank()) {
                if (!status.isNullOrBlank()) "$status ($responseCode): $msg" else "$msg (HTTP $responseCode)"
              } else {
                "HTTP $responseCode: $errStream"
              }
            } catch (_: Exception) {
              "HTTP $responseCode: $errStream"
            }
            lastException = IllegalStateException("Gemini API Error: $formattedMsg")
            // On quota limit (429) or auth failures (401/403), return the real API error immediately
            if (responseCode == 429 || responseCode == 403 || responseCode == 401) {
              return@withContext Result.failure(lastException)
            }
          }
        } catch (e: Exception) {
          lastException = e
        }
      }
    }

    Result.failure(lastException ?: IllegalStateException("Gemini did not return an edited image. Please verify your network and GEMINI_API_KEY."))
  }

  /**
   * Generates a reusable invitation design template by analyzing a user-provided visual reference image.
   * Sends BOTH the actual base64 image and aesthetic instructions to Gemini multimodal API.
   * Extracts visual design while removing event-specific names, dates, and venues.
   */
  suspend fun generateDesignTemplate(
    base64Image: String,
    prompt: String
  ): Result<GeneratedDesignResult> = withContext(Dispatchers.IO) {
    val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (_: Exception) { "" }

    if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
      return@withContext Result.failure(
        IllegalStateException("Gemini API key is required to analyze reference photos. Please configure your GEMINI_API_KEY in the AI Studio Secrets panel.")
      )
    }

    if (base64Image.isBlank()) {
      return@withContext Result.failure(
        IllegalArgumentException("Please select a reference photo first.")
      )
    }

    val systemInstruction = """
      You are Inviora's Master Invitation Art Director.
      Analyze the provided visual reference image carefully.
      Your task: Recreate and extract its visual design as an editable, reusable luxury invitation template.
      
      CRITICAL MANDATES:
      1. SEPARATE VISUAL DESIGN FROM EVENT CONTENT: The reference may contain specific names (e.g. 'Ali & Ayesha'), specific dates (e.g. '20 December'), venues, or personal wording. You MUST remove all event-specific text and reconstruct the background and ornamental layout so it becomes a clean, reusable template suitable for any celebration.
      2. PRESERVE LUXURY AESTHETIC: Carefully analyze the composition, border styles, floral/filigree motifs, color relationships, background texture, and typography hierarchy from the image.
      3. USER INSTRUCTION: Integrate this user request: "$prompt".
      
      You must return ONLY a strict JSON object with these exact keys:
      - "name": (A 2-4 word evocative luxury title for this template, e.g. "Gilded Florentine Arch", "Atelier Botanical Crest", "Champagne Filigree Frame")
      - "themeStyle": (One of: "ROYAL_GOLD", "BOTANICAL_CREAM", "NOIR_VELVET", "CELESTIAL_ROSE")
      - "accentColorHex": (A valid 6-character hex color starting with # extracted from the image, e.g. "#9E7B3B", "#768368", "#C6A75E", "#8B1D2C")
      - "fontStyle": (Typography pairing description, e.g. "Regal Serif & Script Calligraphy", "Editorial Modern Roman Serif")
      - "ornamentStyle": (Detailed description of the borders and motifs extracted, e.g. "Dual-line gold filigree frame with floral crest")
      - "description": (Summary of the reusable visual structure and reconstructed background)
    """.trimIndent()

    try {
      val responseJson = callMultimodalGemini(apiKey, systemInstruction, base64Image)
      val parsed = parseDesignJson(responseJson, prompt)
      Result.success(parsed)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Generates a reusable cinematic opening experience by analyzing the physical motion and object
   * in the user's reference image and instruction prompt.
   * Supports iterative refinement.
   */
  suspend fun generateAnimationExperience(
    base64Image: String,
    movementPrompt: String,
    refinementPrompt: String? = null
  ): Result<GeneratedAnimationResult> = withContext(Dispatchers.IO) {
    val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (_: Exception) { "" }

    if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
      return@withContext Result.failure(
        IllegalStateException("Gemini API key is required for animation synthesis. Please configure your GEMINI_API_KEY in the AI Studio Secrets panel.")
      )
    }

    if (base64Image.isBlank()) {
      return@withContext Result.failure(
        IllegalArgumentException("Please select an animation reference photo first.")
      )
    }

    val refinementInstruction = if (!refinementPrompt.isNullOrBlank()) {
      "\nUSER ITERATIVE REFINEMENT: The user requests these changes to the animation: \"$refinementPrompt\""
    } else ""

    val systemInstruction = """
      You are Inviora's Master Cinematic Opening Director.
      Analyze the provided reference image (which depicts a physical reveal element such as a curtain, grand palace door, gate, envelope, wax seal, or keepsake box).
      Analyze the user's movement instructions: "$movementPrompt"$refinementInstruction
      
      Your task: Determine the physical animation parameters, depth perspective, lighting reveal, and motion timing to cinematic perfection.
      
      You must return ONLY a strict JSON object with these exact keys:
      - "name": (A 2-4 word evocative luxury title, e.g. "Imperial Velvet Unveiling", "Grand Palais Portals", "Artisan Wax Seal Reveal")
      - "animationType": (Strictly one of: "CURTAIN", "PALACE_DOORS", "WAX_SEAL")
      - "motionTimingMs": (Integer duration in milliseconds between 2000 and 4500)
      - "lightingMood": (Atmosphere and lighting description, e.g. "Warm Champagne Luminescence", "Soft Studio Spotlight")
      - "description": (Detailed explanation of how the physical object in the reference moves and reveals the invitation behind it)
    """.trimIndent()

    try {
      val responseJson = callMultimodalGemini(apiKey, systemInstruction, base64Image)
      val parsed = parseAnimationJson(responseJson, movementPrompt)
      Result.success(parsed)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  private fun callMultimodalGemini(apiKey: String, instruction: String, base64Image: String): JSONObject {
    val models = listOf("gemini-3.5-flash", "gemini-2.5-flash")
    var lastException: Exception? = null

    for (model in models) {
      try {
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
        val url = URL(endpoint)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true
        conn.connectTimeout = 60000
        conn.readTimeout = 60000

        val root = JSONObject()
        val contents = JSONArray()
        val contentObj = JSONObject()
        val parts = JSONArray()

        // 1. Text Prompt Part
        val textPart = JSONObject()
        textPart.put("text", instruction)
        parts.put(textPart)

        // 2. Multimodal Inline Image Data Part
        val imagePart = JSONObject()
        val inlineData = JSONObject()
        inlineData.put("mimeType", "image/jpeg")
        inlineData.put("data", base64Image.trim())
        imagePart.put("inlineData", inlineData)
        parts.put(imagePart)

        contentObj.put("parts", parts)
        contents.put(contentObj)
        root.put("contents", contents)

        // Generation config requesting structured JSON
        val genConfig = JSONObject()
        genConfig.put("responseMimeType", "application/json")
        genConfig.put("temperature", 0.4)
        root.put("generationConfig", genConfig)

        OutputStreamWriter(conn.outputStream).use { it.write(root.toString()) }

        val responseCode = conn.responseCode
        if (responseCode == 200) {
          val responseText = conn.inputStream.bufferedReader().readText()
          val json = JSONObject(responseText)
          val candidates = json.optJSONArray("candidates")
          if (candidates != null && candidates.length() > 0) {
            val first = candidates.getJSONObject(0)
            val partsArr = first.optJSONObject("content")?.optJSONArray("parts")
            if (partsArr != null && partsArr.length() > 0) {
              val rawOutput = partsArr.getJSONObject(0).optString("text", "")
              // Clean markdown json fences if any
              val cleanJson = rawOutput.replace("```json", "").replace("```", "").trim()
              return JSONObject(cleanJson)
            }
          }
        } else {
          val errStream = conn.errorStream?.bufferedReader()?.readText() ?: "HTTP $responseCode"
          lastException = IllegalStateException("Gemini API Error ($responseCode): $errStream")
        }
      } catch (e: Exception) {
        lastException = e
      }
    }

    throw lastException ?: IllegalStateException("Unable to generate result. Please check your network and API key.")
  }

  private fun parseDesignJson(json: JSONObject, originalPrompt: String): GeneratedDesignResult {
    val name = json.optString("name", "Bespoke Invitation Template")
    val rawTheme = json.optString("themeStyle", "ROYAL_GOLD").uppercase()
    val theme = when {
      rawTheme.contains("BOTANICAL") -> "BOTANICAL_CREAM"
      rawTheme.contains("NOIR") || rawTheme.contains("VELVET") -> "NOIR_VELVET"
      rawTheme.contains("ROSE") || rawTheme.contains("CELESTIAL") -> "CELESTIAL_ROSE"
      else -> "ROYAL_GOLD"
    }
    val accent = json.optString("accentColorHex", "#9E7B3B").let {
      if (it.startsWith("#") && (it.length == 7 || it.length == 9)) it else "#9E7B3B"
    }
    val fontStyle = json.optString("fontStyle", "Regal Serif & Script Calligraphy")
    val ornament = json.optString("ornamentStyle", "Filigree Ornamental Frame")
    val desc = json.optString("description", originalPrompt)

    return GeneratedDesignResult(
      id = "design_" + UUID.randomUUID().toString().take(8),
      name = name,
      themeStyle = theme,
      accentColorHex = accent,
      fontStyle = fontStyle,
      ornamentStyle = ornament,
      description = desc
    )
  }

  private fun parseAnimationJson(json: JSONObject, originalPrompt: String): GeneratedAnimationResult {
    val name = json.optString("name", "Cinematic Reveal Experience")
    val rawType = json.optString("animationType", "CURTAIN").uppercase()
    val type = when {
      rawType.contains("DOOR") || rawType.contains("GATE") || rawType.contains("PALACE") -> "PALACE_DOORS"
      rawType.contains("WAX") || rawType.contains("SEAL") || rawType.contains("ENVELOPE") -> "WAX_SEAL"
      else -> "CURTAIN"
    }
    val timing = json.optLong("motionTimingMs", 2600L).coerceIn(1800L, 5000L)
    val lighting = json.optString("lightingMood", "Warm Luminescent Ambiance")
    val desc = json.optString("description", originalPrompt)

    return GeneratedAnimationResult(
      id = "anim_" + UUID.randomUUID().toString().take(8),
      name = name,
      animationType = type,
      motionTimingMs = timing,
      lightingMood = lighting,
      description = desc
    )
  }
}
