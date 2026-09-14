package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject

/**
 * TextLayerConfig:
 * Represents an individual editable text element on an invitation page.
 * Uses normalized proportional coordinates (0.0 to 1.0) so text remains in the exact
 * proportional position relative to the uploaded background artwork across all screen sizes.
 */
data class TextLayerConfig(
  val id: String,
  val label: String,
  var text: String = "",
  var xPercent: Float = 0.5f, // 0.05f to 0.95f, 0.5f is centered horizontally
  var yPercent: Float = 0.5f, // 0.02f to 0.98f from top to bottom
  var fontSizeSp: Int = 14,
  var fontWeight: String = "Normal", // "Normal", "Medium", "SemiBold", "Bold"
  var fontStyle: String = "Normal", // "Normal", "Italic"
  var alignment: String = "Center", // "Center", "Start", "End"
  var colorHex: String = "#1E1A16", // "#1E1A16" (Charcoal), "#8C6D23" (Gold), "#FFFFFF" (White)
  var fontFamily: String = "Serif", // "Serif", "SansSerif", "Cursive"
  var isVisible: Boolean = true
) {
  fun toJson(): JSONObject {
    val obj = JSONObject()
    obj.put("id", id)
    obj.put("label", label)
    obj.put("text", text)
    obj.put("xPercent", xPercent.toDouble())
    obj.put("yPercent", yPercent.toDouble())
    obj.put("fontSizeSp", fontSizeSp)
    obj.put("fontWeight", fontWeight)
    obj.put("fontStyle", fontStyle)
    obj.put("alignment", alignment)
    obj.put("colorHex", colorHex)
    obj.put("fontFamily", fontFamily)
    obj.put("isVisible", isVisible)
    return obj
  }

  companion object {
    fun fromJson(obj: JSONObject): TextLayerConfig {
      return TextLayerConfig(
        id = obj.optString("id", ""),
        label = obj.optString("label", ""),
        text = obj.optString("text", ""),
        xPercent = obj.optDouble("xPercent", 0.5).toFloat(),
        yPercent = obj.optDouble("yPercent", 0.5).toFloat(),
        fontSizeSp = obj.optInt("fontSizeSp", 14),
        fontWeight = obj.optString("fontWeight", "Normal"),
        fontStyle = obj.optString("fontStyle", "Normal"),
        alignment = obj.optString("alignment", "Center"),
        colorHex = obj.optString("colorHex", "#1E1A16"),
        fontFamily = obj.optString("fontFamily", "Serif"),
        isVisible = obj.optBoolean("isVisible", true)
      )
    }

    fun parseList(jsonString: String): List<TextLayerConfig> {
      if (jsonString.isBlank() || jsonString == "[]") return emptyList()
      return try {
        val array = JSONArray(jsonString)
        val list = mutableListOf<TextLayerConfig>()
        for (i in 0 until array.length()) {
          list.add(fromJson(array.getJSONObject(i)))
        }
        list
      } catch (_: Exception) {
        emptyList()
      }
    }

    fun listToJson(list: List<TextLayerConfig>): String {
      val array = JSONArray()
      list.forEach { array.put(it.toJson()) }
      return array.toString()
    }

    /**
     * Builds default layers based on Event and Page attributes,
     * merging any customized positions or styling from page.textLayoutJson.
     */
    fun buildResolvedLayers(
      event: EventEntity?,
      page: EventPage,
      guest: GuestEntity? = null,
      guestName: String? = null
    ): List<TextLayerConfig> {
      val isWedding = event?.eventType.equals("Wedding", ignoreCase = true) || (event == null)

      // Arabic Bismillah text
      val bismillahAr = page.bismillahArabic.ifBlank { "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ" }
      // English Translation text directly underneath
      val bismillahEn = page.bismillahEnglish.ifBlank { "In the name of Allah, the Most Gracious, the Most Merciful." }

      // Couple / Title text
      val coupleTitle = if (isWedding) {
        if (!event?.groomName.isNullOrBlank() && !event?.brideName.isNullOrBlank()) {
          "${event!!.groomName} & ${event.brideName}"
        } else if (!event?.title.isNullOrBlank()) {
          event!!.title
        } else {
          page.eventTitle.ifBlank { "Wedding Celebration" }
        }
      } else {
        page.eventTitle.ifBlank { event?.title ?: "Celebration" }
      }

      val actualGuestName = guestName ?: guest?.name
      val guestText = actualGuestName ?: ""
      val guestStyle = guest?.getEffectiveStyleForPage(page, event) ?: page.getPageDefaultGuestStyle(event)

      // Default baseline layers
      val defaults = listOf(
        TextLayerConfig(
          id = "bismillah_arabic",
          label = "Bismillah (Arabic)",
          text = bismillahAr,
          xPercent = 0.5f,
          yPercent = 0.08f,
          fontSizeSp = 20,
          fontWeight = "SemiBold",
          alignment = "Center",
          colorHex = "#8C6D23",
          isVisible = isWedding && page.showBismillah
        ),
        TextLayerConfig(
          id = "bismillah_english",
          label = "Bismillah (English)",
          text = bismillahEn,
          xPercent = 0.5f,
          yPercent = 0.13f,
          fontSizeSp = 10,
          fontStyle = "Italic",
          alignment = "Center",
          colorHex = "#5C544C",
          isVisible = isWedding && page.showBismillah
        ),
        TextLayerConfig(
          id = "couple_names",
          label = if (isWedding) "Groom & Bride Names" else "Event Title",
          text = coupleTitle,
          xPercent = 0.5f,
          yPercent = if (isWedding) 0.22f else 0.14f,
          fontSizeSp = 24,
          fontWeight = "Bold",
          alignment = "Center",
          colorHex = "#1E1A16",
          isVisible = true
        ),
        TextLayerConfig(
          id = "page_name",
          label = "Ceremony / Occasion Name",
          text = if (page.pageName.isNotBlank()) "— ${page.pageName.uppercase()} —" else "",
          xPercent = 0.5f,
          yPercent = if (isWedding) 0.31f else 0.22f,
          fontSizeSp = 13,
          fontWeight = "SemiBold",
          alignment = "Center",
          colorHex = "#8C6D23",
          isVisible = page.pageName.isNotBlank()
        ),
        TextLayerConfig(
          id = "guest_name",
          label = "Guest Name",
          text = guestText,
          xPercent = guestStyle.xPercent,
          yPercent = guestStyle.yPercent,
          fontSizeSp = guestStyle.fontSizeSp,
          fontStyle = guestStyle.fontStyle,
          fontWeight = guestStyle.fontWeight,
          alignment = guestStyle.alignment,
          colorHex = guestStyle.colorHex,
          fontFamily = guestStyle.fontFamily,
          isVisible = guestText.isNotBlank()
        ),
        TextLayerConfig(
          id = "greeting",
          label = "Invitation Greeting",
          text = page.greeting,
          xPercent = 0.5f,
          yPercent = if (isWedding) 0.46f else 0.37f,
          fontSizeSp = 12,
          fontStyle = "Italic",
          alignment = "Center",
          colorHex = "#4A443D",
          isVisible = page.greeting.isNotBlank()
        ),
        TextLayerConfig(
          id = "date",
          label = "Date",
          text = page.date,
          xPercent = 0.5f,
          yPercent = 0.58f,
          fontSizeSp = 14,
          fontWeight = "Medium",
          alignment = "Center",
          colorHex = "#1E1A16",
          isVisible = page.date.isNotBlank()
        ),
        TextLayerConfig(
          id = "time",
          label = "Time",
          text = page.time,
          xPercent = 0.5f,
          yPercent = 0.64f,
          fontSizeSp = 13,
          alignment = "Center",
          colorHex = "#4A443D",
          isVisible = page.time.isNotBlank()
        ),
        TextLayerConfig(
          id = "venue",
          label = "Venue",
          text = page.venue,
          xPercent = 0.5f,
          yPercent = 0.72f,
          fontSizeSp = 13,
          fontWeight = "Medium",
          alignment = "Center",
          colorHex = "#2C2621",
          isVisible = page.venue.isNotBlank()
        ),
        TextLayerConfig(
          id = "details",
          label = "Additional Details",
          text = page.additionalDetails,
          xPercent = 0.5f,
          yPercent = 0.82f,
          fontSizeSp = 11,
          alignment = "Center",
          colorHex = "#70675E",
          isVisible = page.additionalDetails.isNotBlank()
        )
      )

      // Merge saved user modifications if any
      val saved = parseList(page.textLayoutJson).associateBy { it.id }
      if (saved.isEmpty()) {
        return defaults
      }

      return defaults.map { def ->
        val userLayer = saved[def.id]
        if (userLayer != null) {
          // Keep current dynamic text unless user explicitly altered it in textLayout
          val finalTxt = if (userLayer.text.isNotBlank() && def.id != "couple_names" && def.id != "guest_name") {
            userLayer.text
          } else {
            def.text
          }

          // For guest_name, prioritize guest custom styling or page/event defaults
          val finalX = if (def.id == "guest_name") guestStyle.xPercent else userLayer.xPercent
          val finalY = if (def.id == "guest_name") guestStyle.yPercent else userLayer.yPercent
          val finalSize = if (def.id == "guest_name") guestStyle.fontSizeSp else userLayer.fontSizeSp
          val finalFontWeight = if (def.id == "guest_name") guestStyle.fontWeight else userLayer.fontWeight
          val finalFontStyle = if (def.id == "guest_name") guestStyle.fontStyle else userLayer.fontStyle
          val finalFontFamily = if (def.id == "guest_name") guestStyle.fontFamily else userLayer.fontFamily
          val finalColorHex = if (def.id == "guest_name") guestStyle.colorHex else userLayer.colorHex
          val finalAlignment = if (def.id == "guest_name") guestStyle.alignment else userLayer.alignment

          val finalVisibility = if (def.id == "guest_name") {
            def.text.isNotBlank()
          } else {
            userLayer.isVisible && def.text.isNotBlank()
          }

          def.copy(
            text = finalTxt,
            xPercent = finalX,
            yPercent = finalY,
            fontSizeSp = finalSize,
            fontWeight = finalFontWeight,
            fontStyle = finalFontStyle,
            fontFamily = finalFontFamily,
            alignment = finalAlignment,
            colorHex = finalColorHex,
            isVisible = finalVisibility
          )
        } else {
          def
        }
      }
    }
  }
}
