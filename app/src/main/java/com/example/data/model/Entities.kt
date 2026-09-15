package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

data class PageGuestStyle(
  val xPercent: Float = 0.5f,
  val yPercent: Float = 0.38f,
  val fontSizeSp: Int = 18,
  val colorHex: String = "#8C6D23",
  val fontFamily: String = "Serif",
  val fontWeight: String = "Bold",
  val fontStyle: String = "Italic",
  val alignment: String = "Center"
) {
  fun toJson(): JSONObject {
    val obj = JSONObject()
    obj.put("xPercent", xPercent.toDouble())
    obj.put("yPercent", yPercent.toDouble())
    obj.put("fontSizeSp", fontSizeSp)
    obj.put("colorHex", colorHex)
    obj.put("fontFamily", fontFamily)
    obj.put("fontWeight", fontWeight)
    obj.put("fontStyle", fontStyle)
    obj.put("alignment", alignment)
    return obj
  }

  companion object {
    fun fromJson(obj: JSONObject): PageGuestStyle {
      return PageGuestStyle(
        xPercent = obj.optDouble("xPercent", 0.5).toFloat(),
        yPercent = obj.optDouble("yPercent", 0.38).toFloat(),
        fontSizeSp = obj.optInt("fontSizeSp", 18),
        colorHex = obj.optString("colorHex", "#8C6D23"),
        fontFamily = obj.optString("fontFamily", "Serif"),
        fontWeight = obj.optString("fontWeight", "Bold"),
        fontStyle = obj.optString("fontStyle", "Italic"),
        alignment = obj.optString("alignment", "Center")
      )
    }

    fun parseMap(json: String): Map<String, PageGuestStyle> {
      if (json.isBlank() || json == "{}" || json == "[]") return emptyMap()
      return try {
        val obj = JSONObject(json)
        val map = mutableMapOf<String, PageGuestStyle>()
        val keys = obj.keys()
        while (keys.hasNext()) {
          val key = keys.next()
          map[key] = fromJson(obj.getJSONObject(key))
        }
        map
      } catch (_: Exception) {
        emptyMap()
      }
    }

    fun mapToJson(map: Map<String, PageGuestStyle>): String {
      val obj = JSONObject()
      map.forEach { (k, v) -> obj.put(k, v.toJson()) }
      return obj.toString()
    }
  }
}

data class EventPage(
  val id: String,
  val eventId: String,
  val pageName: String,
  val designId: String,
  val greeting: String = "",
  val eventTitle: String = "",
  val date: String = "",
  val time: String = "",
  val venue: String = "",
  val additionalDetails: String = "",
  val pageOrder: Int = 0,
  val bismillahArabic: String = "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ",
  val bismillahEnglish: String = "In the name of Allah, the Most Gracious, the Most Merciful.",
  val showBismillah: Boolean = true,
  val textLayoutJson: String = "",
  val hasCustomGuestDefault: Boolean = false,
  val defaultGuestXPercent: Float = 0.5f,
  val defaultGuestYPercent: Float = 0.38f,
  val defaultGuestFontSize: Int = 18,
  val defaultGuestColorHex: String = "#8C6D23",
  val defaultGuestFontFamily: String = "Serif",
  val defaultGuestFontWeight: String = "Bold",
  val defaultGuestFontStyle: String = "Italic",
  val defaultGuestAlignment: String = "Center"
) {
  fun getPageDefaultGuestStyle(event: EventEntity?): PageGuestStyle {
    if (hasCustomGuestDefault) {
      return PageGuestStyle(
        xPercent = defaultGuestXPercent,
        yPercent = defaultGuestYPercent,
        fontSizeSp = defaultGuestFontSize,
        colorHex = defaultGuestColorHex,
        fontFamily = defaultGuestFontFamily,
        fontWeight = defaultGuestFontWeight,
        fontStyle = defaultGuestFontStyle,
        alignment = defaultGuestAlignment
      )
    }
    return PageGuestStyle(
      xPercent = event?.defaultGuestNameXPercent ?: 0.5f,
      yPercent = event?.defaultGuestNameYPercent ?: (if (event?.eventType.equals("Wedding", ignoreCase = true) || event == null) 0.38f else 0.29f),
      fontSizeSp = event?.defaultGuestNameFontSize ?: 18,
      colorHex = event?.defaultGuestNameColorHex ?: "#8C6D23",
      fontFamily = event?.defaultGuestNameFontFamily ?: "Serif",
      fontWeight = event?.defaultGuestNameFontWeight ?: "Bold",
      fontStyle = event?.defaultGuestNameFontStyle ?: "Italic",
      alignment = event?.defaultGuestNameAlignment ?: "Center"
    )
  }

  fun toJson(): JSONObject {
    val obj = JSONObject()
    obj.put("id", id)
    obj.put("eventId", eventId)
    obj.put("pageName", pageName)
    obj.put("designId", designId)
    obj.put("greeting", greeting)
    obj.put("eventTitle", eventTitle)
    obj.put("date", date)
    obj.put("time", time)
    obj.put("venue", venue)
    obj.put("additionalDetails", additionalDetails)
    obj.put("pageOrder", pageOrder)
    obj.put("bismillahArabic", bismillahArabic)
    obj.put("bismillahEnglish", bismillahEnglish)
    obj.put("showBismillah", showBismillah)
    obj.put("textLayoutJson", textLayoutJson)
    obj.put("hasCustomGuestDefault", hasCustomGuestDefault)
    obj.put("defaultGuestXPercent", defaultGuestXPercent.toDouble())
    obj.put("defaultGuestYPercent", defaultGuestYPercent.toDouble())
    obj.put("defaultGuestFontSize", defaultGuestFontSize)
    obj.put("defaultGuestColorHex", defaultGuestColorHex)
    obj.put("defaultGuestFontFamily", defaultGuestFontFamily)
    obj.put("defaultGuestFontWeight", defaultGuestFontWeight)
    obj.put("defaultGuestFontStyle", defaultGuestFontStyle)
    obj.put("defaultGuestAlignment", defaultGuestAlignment)
    return obj
  }

  companion object {
    fun fromJson(obj: JSONObject): EventPage {
      return EventPage(
        id = obj.optString("id", ""),
        eventId = obj.optString("eventId", ""),
        pageName = obj.optString("pageName", ""),
        designId = obj.optString("designId", ""),
        greeting = obj.optString("greeting", ""),
        eventTitle = obj.optString("eventTitle", ""),
        date = obj.optString("date", ""),
        time = obj.optString("time", ""),
        venue = obj.optString("venue", ""),
        additionalDetails = obj.optString("additionalDetails", ""),
        pageOrder = obj.optInt("pageOrder", 0),
        bismillahArabic = obj.optString("bismillahArabic", "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ"),
        bismillahEnglish = obj.optString("bismillahEnglish", "In the name of Allah, the Most Gracious, the Most Merciful."),
        showBismillah = obj.optBoolean("showBismillah", true),
        textLayoutJson = obj.optString("textLayoutJson", ""),
        hasCustomGuestDefault = obj.optBoolean("hasCustomGuestDefault", false),
        defaultGuestXPercent = obj.optDouble("defaultGuestXPercent", 0.5).toFloat(),
        defaultGuestYPercent = obj.optDouble("defaultGuestYPercent", 0.38).toFloat(),
        defaultGuestFontSize = obj.optInt("defaultGuestFontSize", 18),
        defaultGuestColorHex = obj.optString("defaultGuestColorHex", "#8C6D23"),
        defaultGuestFontFamily = obj.optString("defaultGuestFontFamily", "Serif"),
        defaultGuestFontWeight = obj.optString("defaultGuestFontWeight", "Bold"),
        defaultGuestFontStyle = obj.optString("defaultGuestFontStyle", "Italic"),
        defaultGuestAlignment = obj.optString("defaultGuestAlignment", "Center")
      )
    }

    fun parseList(jsonString: String): List<EventPage> {
      if (jsonString.isBlank()) return emptyList()
      return try {
        val array = JSONArray(jsonString)
        val list = mutableListOf<EventPage>()
        for (i in 0 until array.length()) {
          list.add(fromJson(array.getJSONObject(i)))
        }
        list.sortedBy { it.pageOrder }
      } catch (_: Exception) {
        emptyList()
      }
    }

    fun listToJson(list: List<EventPage>): String {
      val array = JSONArray()
      list.forEach { array.put(it.toJson()) }
      return array.toString()
    }
  }
}

data class SubEvent(
  val id: String,
  val name: String,
  val date: String,
  val time: String,
  val venue: String
) {
  fun toJson(): JSONObject {
    val obj = JSONObject()
    obj.put("id", id)
    obj.put("name", name)
    obj.put("date", date)
    obj.put("time", time)
    obj.put("venue", venue)
    return obj
  }

  companion object {
    fun fromJson(obj: JSONObject): SubEvent {
      return SubEvent(
        id = obj.optString("id", ""),
        name = obj.optString("name", ""),
        date = obj.optString("date", ""),
        time = obj.optString("time", ""),
        venue = obj.optString("venue", "")
      )
    }

    fun parseList(jsonString: String): List<SubEvent> {
      if (jsonString.isBlank()) return emptyList()
      return try {
        val array = JSONArray(jsonString)
        val list = mutableListOf<SubEvent>()
        for (i in 0 until array.length()) {
          list.add(fromJson(array.getJSONObject(i)))
        }
        list
      } catch (_: Exception) {
        emptyList()
      }
    }

    fun listToJson(list: List<SubEvent>): String {
      val array = JSONArray()
      list.forEach { array.put(it.toJson()) }
      return array.toString()
    }
  }
}

@Entity(tableName = "design_templates")
data class DesignTemplateEntity(
  @PrimaryKey val id: String,
  val name: String,
  val referenceDrawable: String = "",
  val imagePath: String = "", // File path or base64 or URI
  val themeStyle: String = "CUSTOM_UPLOAD",
  val accentColorHex: String = "#D4AF37",
  val fontStyle: String = "Serif Calligraphic",
  val ornamentStyle: String = "Clean Upload",
  val prompt: String = "",
  val isFavorite: Boolean = false,
  val createdAt: Long = System.currentTimeMillis()
)

val BUILT_IN_DESIGN_IDS = setOf("design_royal_gold", "design_botanical_cream", "design_noir_luxe")

val DesignTemplateEntity.isCustom: Boolean
  get() = id !in BUILT_IN_DESIGN_IDS || (referenceDrawable.isBlank() && imagePath.isNotBlank())

@Entity(tableName = "animation_experiences")
data class AnimationExperienceEntity(
  @PrimaryKey val id: String,
  val name: String,
  val referenceDrawable: String = "",
  val animationType: String = "CURTAIN",
  val motionTimingMs: Long = 2500L,
  val lightingMood: String = "Warm Cinematic",
  val description: String = "",
  val isFavorite: Boolean = false,
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "events")
data class EventEntity(
  @PrimaryKey val id: String,
  val title: String,
  val eventType: String, // "Wedding", "Birthday", "Anniversary", "Engagement", "Dinner", "Party", "Graduation", "Corporate Event", "Religious Event", "Custom Event"
  val groomName: String = "",
  val brideName: String = "",
  val hostNames: String = "",
  val primaryVenue: String = "",
  val primaryDate: String = "",
  val notes: String = "",
  val designId: String = "",
  val animationId: String = "anim_velvet_curtain",
  val pagesJson: String = "[]",
  val subEventsJson: String = "[]",
  val includeTraditionalGreeting: Boolean = false,
  val traditionalBismillahText: String = "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ",
  val traditionalGreetingText: String = "",
  val invitationWording: String = "",
  val defaultGuestNameXPercent: Float = 0.5f,
  val defaultGuestNameYPercent: Float = 0.38f,
  val defaultGuestNameFontSize: Int = 18,
  val defaultGuestNameColorHex: String = "#8C6D23",
  val defaultGuestNameFontFamily: String = "Serif",
  val defaultGuestNameFontWeight: String = "Bold",
  val defaultGuestNameFontStyle: String = "Italic",
  val defaultGuestNameAlignment: String = "Center",
  val createdAt: Long = System.currentTimeMillis()
) {
  fun getPages(): List<EventPage> {
    val pages = EventPage.parseList(pagesJson)
    if (pages.isNotEmpty()) return pages
    val sub = getSubEvents()
    if (sub.isNotEmpty()) {
      return sub.mapIndexed { idx, s ->
        EventPage(
          id = s.id,
          eventId = id,
          pageName = s.name,
          designId = designId,
          greeting = getDefaultGreetingForPage(eventType, s.name),
          eventTitle = if (eventType == "Wedding" && groomName.isNotBlank() && brideName.isNotBlank()) "$groomName & $brideName" else title,
          date = s.date,
          time = s.time,
          venue = s.venue,
          additionalDetails = notes,
          pageOrder = idx,
          showBismillah = eventType == "Wedding"
        )
      }
    }
    return listOf(
      EventPage(
        id = "page_${id}_main",
        eventId = id,
        pageName = if (eventType == "Wedding") "Wedding Ceremony" else (title.ifBlank { "Main Celebration" }),
        designId = designId,
        greeting = getDefaultGreetingForPage(eventType, "Main"),
        eventTitle = if (eventType == "Wedding" && groomName.isNotBlank() && brideName.isNotBlank()) "$groomName & $brideName" else title,
        date = primaryDate,
        venue = primaryVenue,
        additionalDetails = notes,
        pageOrder = 0,
        showBismillah = eventType == "Wedding"
      )
    )
  }

  fun getSubEvents(): List<SubEvent> = SubEvent.parseList(subEventsJson)
}

fun getDefaultGreetingForPage(eventType: String, pageName: String): String {
  if (eventType.equals("Wedding", ignoreCase = true)) {
    val lower = pageName.lowercase()
    return when {
      lower.contains("mehndi") || lower.contains("mayun") || lower.contains("sangeet") ->
        "Together with our families, we invite you to celebrate the joyous evening of music, henna, and colors as we celebrate the upcoming wedding of"
      lower.contains("baraat") || lower.contains("barat") || lower.contains("shaadi") || lower.contains("nikah") || lower.contains("ceremony") ->
        "We request the honor of your presence and blessings to celebrate the auspicious wedding ceremony of"
      lower.contains("walima") || lower.contains("reception") ->
        "Cordially request the pleasure of your company to celebrate the wedding reception (Walima) of"
      else ->
        "With joy in our hearts, we invite you to celebrate the wedding ceremony of"
    }
  }
  val lower = eventType.lowercase()
  return when {
    lower.contains("birthday") -> "Join us to celebrate another wonderful year of joy and memories"
    lower.contains("anniversary") -> "Cordially invite you to celebrate the milestone anniversary of"
    lower.contains("dinner") || lower.contains("party") -> "Delighted to invite you to an evening of celebration and fine dining"
    else -> "We cordially invite you to celebrate this special moment with us."
  }
}

@Entity(tableName = "guests")
data class GuestEntity(
  @PrimaryKey val id: String,
  val eventId: String,
  val name: String,
  val phone: String = "",
  val guestNote: String? = null,
  val selectedPageIdsJson: String = "[]",
  val selectedSubEventIdsJson: String = "[]",
  val uniqueToken: String,
  val active: Boolean = true,
  val hasCustomStyleOverride: Boolean = false,
  val guestNameXPercent: Float = 0.5f,
  val guestNameYPercent: Float = 0.38f,
  val guestNameFontSize: Int = 18,
  val guestNameColorHex: String = "#8C6D23",
  val guestNameFontFamily: String = "Serif",
  val guestNameFontWeight: String = "Bold",
  val guestNameFontStyle: String = "Italic",
  val guestNameAlignment: String = "Center",
  val pageOverridesJson: String = "{}",
  val createdAt: Long = System.currentTimeMillis()
) {
  fun getEffectiveStyleForPage(page: EventPage?, event: EventEntity?): PageGuestStyle {
    val overrides = PageGuestStyle.parseMap(pageOverridesJson)
    if (page != null && overrides.containsKey(page.id)) {
      return overrides[page.id]!!
    }
    val hasDirectGuestOverride = hasCustomStyleOverride ||
      guestNameFontSize != 18 ||
      guestNameYPercent != 0.38f ||
      guestNameXPercent != 0.5f ||
      guestNameColorHex != "#8C6D23" ||
      guestNameFontFamily != "Serif" ||
      guestNameFontWeight != "Bold" ||
      guestNameFontStyle != "Italic" ||
      guestNameAlignment != "Center"

    if (hasDirectGuestOverride) {
      return PageGuestStyle(
        xPercent = guestNameXPercent,
        yPercent = guestNameYPercent,
        fontSizeSp = guestNameFontSize,
        colorHex = guestNameColorHex,
        fontFamily = guestNameFontFamily,
        fontWeight = guestNameFontWeight,
        fontStyle = guestNameFontStyle,
        alignment = guestNameAlignment
      )
    }
    if (page != null) {
      return page.getPageDefaultGuestStyle(event)
    }
    return PageGuestStyle(
      xPercent = event?.defaultGuestNameXPercent ?: 0.5f,
      yPercent = event?.defaultGuestNameYPercent ?: 0.38f,
      fontSizeSp = event?.defaultGuestNameFontSize ?: 18,
      colorHex = event?.defaultGuestNameColorHex ?: "#8C6D23",
      fontFamily = event?.defaultGuestNameFontFamily ?: "Serif",
      fontWeight = event?.defaultGuestNameFontWeight ?: "Bold",
      fontStyle = event?.defaultGuestNameFontStyle ?: "Italic",
      alignment = event?.defaultGuestNameAlignment ?: "Center"
    )
  }

  fun getEffectiveXPercent(event: EventEntity?, page: EventPage? = null): Float =
    getEffectiveStyleForPage(page, event).xPercent

  fun getEffectiveYPercent(event: EventEntity?, page: EventPage? = null): Float =
    getEffectiveStyleForPage(page, event).yPercent

  fun getEffectiveFontSize(event: EventEntity?, page: EventPage? = null): Int =
    getEffectiveStyleForPage(page, event).fontSizeSp

  fun getEffectiveColorHex(event: EventEntity?, page: EventPage? = null): String =
    getEffectiveStyleForPage(page, event).colorHex

  fun getEffectiveFontFamily(event: EventEntity?, page: EventPage? = null): String =
    getEffectiveStyleForPage(page, event).fontFamily

  fun getEffectiveFontWeight(event: EventEntity?, page: EventPage? = null): String =
    getEffectiveStyleForPage(page, event).fontWeight

  fun getEffectiveFontStyle(event: EventEntity?, page: EventPage? = null): String =
    getEffectiveStyleForPage(page, event).fontStyle

  fun getEffectiveAlignment(event: EventEntity?, page: EventPage? = null): String =
    getEffectiveStyleForPage(page, event).alignment

  fun getSelectedPageIds(): List<String> {
    val json = if (selectedPageIdsJson.isNotBlank() && selectedPageIdsJson != "[]") selectedPageIdsJson else selectedSubEventIdsJson
    if (json.isBlank()) return emptyList()
    return try {
      val array = JSONArray(json)
      val list = mutableListOf<String>()
      for (i in 0 until array.length()) {
        list.add(array.getString(i))
      }
      list
    } catch (_: Exception) {
      emptyList()
    }
  }

  fun getInvitedPages(allPages: List<EventPage>): List<EventPage> {
    val selectedIds = getSelectedPageIds()
    if (selectedIds.isEmpty()) return allPages
    return allPages.filter { selectedIds.contains(it.id) }
  }

  fun getSelectedSubEventIds(): List<String> = getSelectedPageIds()

  fun getInvitedSubEvents(allSubEvents: List<SubEvent>): List<SubEvent> {
    val selectedIds = getSelectedPageIds()
    if (selectedIds.isEmpty()) return allSubEvents
    return allSubEvents.filter { selectedIds.contains(it.id) }
  }

  companion object {
    fun idsToJson(ids: List<String>): String {
      val array = JSONArray()
      ids.forEach { array.put(it) }
      return array.toString()
    }
  }
}

@Entity(tableName = "app_preferences")
data class AppPreferenceEntity(
  @PrimaryKey val key: String,
  val value: String
)
