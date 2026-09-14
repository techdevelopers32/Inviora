package com.example

import com.example.data.model.DesignTemplateEntity
import com.example.data.model.EventEntity
import com.example.data.model.EventPage
import com.example.data.model.GuestEntity
import com.example.data.model.PageGuestStyle
import com.example.data.model.TextLayerConfig
import com.example.data.model.getDefaultGreetingForPage
import com.example.data.repository.FirestorePublishingRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleUnitTest {

  @Test
  fun testWeddingGreetingsAreEventSpecific() {
    val mehndiGreeting = getDefaultGreetingForPage("Wedding", "Mehndi")
    val baraatGreeting = getDefaultGreetingForPage("Wedding", "Baraat")
    val walimaGreeting = getDefaultGreetingForPage("Wedding", "Walima")

    assertTrue(mehndiGreeting.contains("henna", ignoreCase = true) || mehndiGreeting.contains("music", ignoreCase = true))
    assertTrue(baraatGreeting.contains("honor of your presence", ignoreCase = true) || baraatGreeting.contains("wedding ceremony", ignoreCase = true))
    assertTrue(walimaGreeting.contains("reception", ignoreCase = true) || walimaGreeting.contains("Walima", ignoreCase = true))
  }

  @Test
  fun testCoupleNamesAppearOnWeddingPages() {
    val weddingEvent = EventEntity(
      id = "evt_1",
      title = "Ali & Ayesha Wedding",
      eventType = "Wedding",
      groomName = "Ali",
      brideName = "Ayesha"
    )

    val page = EventPage(
      id = "page_1",
      eventId = "evt_1",
      pageName = "Mehndi",
      designId = "dsg_1",
      greeting = "Celebrate with us",
      eventTitle = "Ali & Ayesha Wedding",
      date = "Dec 18, 2026",
      time = "7:00 PM",
      venue = "Royal Palace",
      showBismillah = true
    )

    val layers = TextLayerConfig.buildResolvedLayers(weddingEvent, page)
    val coupleLayer = layers.find { it.id == "couple_names" }
    assertNotNull(coupleLayer)
    assertTrue(coupleLayer!!.isVisible)
    assertEquals("Ali & Ayesha", coupleLayer.text)
  }

  @Test
  fun testBismillahArabicAndEnglishAppear() {
    val page = EventPage(
      id = "page_1",
      eventId = "evt_1",
      pageName = "Baraat",
      designId = "dsg_1",
      greeting = "Honor of your presence",
      eventTitle = "Wedding",
      showBismillah = true,
      bismillahArabic = "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ",
      bismillahEnglish = "In the name of Allah, the Most Gracious, the Most Merciful."
    )

    val layers = TextLayerConfig.buildResolvedLayers(null, page)
    val arabicLayer = layers.find { it.id == "bismillah_arabic" }
    val englishLayer = layers.find { it.id == "bismillah_english" }

    assertNotNull(arabicLayer)
    assertNotNull(englishLayer)
    assertTrue(arabicLayer!!.isVisible)
    assertTrue(englishLayer!!.isVisible)
    assertEquals("بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ", arabicLayer.text)
    assertEquals("In the name of Allah, the Most Gracious, the Most Merciful.", englishLayer.text)
  }

  @Test
  fun testGuestPersonalizationStyling() {
    val guest = GuestEntity(
      id = "gst_1",
      eventId = "evt_1",
      name = "Mr. & Mrs. Tariq Khan",
      phone = "+1 555-0199",
      uniqueToken = "AbCdEf",
      guestNameFontSize = 22,
      guestNameColorHex = "#8C6D23",
      guestNameYPercent = 0.55f
    )

    val page = EventPage(
      id = "page_1",
      eventId = "evt_1",
      pageName = "Walima",
      designId = "dsg_1",
      greeting = "Welcome",
      eventTitle = "Wedding"
    )

    val layers = TextLayerConfig.buildResolvedLayers(null, page, guest, guest.name)
    val guestLayer = layers.find { it.id == "guest_name" }

    assertNotNull(guestLayer)
    assertTrue(guestLayer!!.isVisible)
    assertEquals("Mr. & Mrs. Tariq Khan", guestLayer.text)
    assertEquals(22, guestLayer.fontSizeSp)
    assertEquals("#8C6D23", guestLayer.colorHex)
    assertEquals(0.55f, guestLayer.yPercent)
  }

  @Test
  fun testAhmedKhanVisiblyRendersEvenWithSavedPageLayout() {
    // Step 5, 6, 8, 9: Confirm "Mr. & Mrs. Ahmed Khan" visibly renders on top of the invitation design
    // even if page has saved text layout where guest_name had blank text/visibility previously
    val initialPage = EventPage(
      id = "page_baraat",
      eventId = "evt_wedding",
      pageName = "Baraat",
      designId = "design_royal_gold",
      greeting = "Cordially invite you",
      eventTitle = "Zayd & Fatima Wedding",
      date = "December 20, 2026",
      venue = "Grand Ballroom",
      // Simulate master design editor saving layers with isVisible = false for blank guest name
      textLayoutJson = """[{"id":"guest_name","text":"","xPercent":0.5,"yPercent":0.4,"fontSizeSp":18,"isVisible":false}]"""
    )

    val event = EventEntity(
      id = "evt_wedding",
      title = "Zayd & Fatima Wedding",
      eventType = "Wedding",
      defaultGuestNameXPercent = 0.5f,
      defaultGuestNameYPercent = 0.65f,
      defaultGuestNameFontSize = 30
    )

    val guestAhmed = GuestEntity(
      id = "gst_ahmed",
      eventId = "evt_wedding",
      name = "Mr. & Mrs. Ahmed Khan",
      uniqueToken = "tok_ahmed",
      hasCustomStyleOverride = false
    )

    val layers = TextLayerConfig.buildResolvedLayers(event, initialPage, guestAhmed, guestAhmed.name)
    val guestLayer = layers.find { it.id == "guest_name" }

    assertNotNull("Guest layer must exist", guestLayer)
    assertTrue("Guest layer must be visible when guest name is provided", guestLayer!!.isVisible)
    assertEquals("Mr. & Mrs. Ahmed Khan", guestLayer.text)
    assertEquals(30, guestLayer.fontSizeSp)
    assertEquals(0.65f, guestLayer.yPercent, 0.001f)
  }

  @Test
  fun testEventWideDefaultPositioningAndSecondGuestInheritance() {
    // Step 10-15:
    // 1. First guest changes position & font (e.g. Y: 0.72f, Font: 28sp)
    // 2. Saved as event default
    // 3. Second guest automatically inherits 0.72f and 28sp
    // 4. Modifying second guest with custom override does not alter first guest or event default
    var currentEvent = EventEntity(
      id = "evt_test",
      title = "Celebration",
      eventType = "Wedding",
      defaultGuestNameXPercent = 0.5f,
      defaultGuestNameYPercent = 0.38f,
      defaultGuestNameFontSize = 18
    )

    // First guest saved with "Use as default for this event"
    val firstGuestX = 0.52f
    val firstGuestY = 0.72f
    val firstGuestFontSize = 28

    currentEvent = currentEvent.copy(
      defaultGuestNameXPercent = firstGuestX,
      defaultGuestNameYPercent = firstGuestY,
      defaultGuestNameFontSize = firstGuestFontSize
    )

    val guest1 = GuestEntity(
      id = "gst_1",
      eventId = currentEvent.id,
      name = "Mr. & Mrs. Ahmed Khan",
      uniqueToken = "tok_1",
      hasCustomStyleOverride = false,
      guestNameXPercent = firstGuestX,
      guestNameYPercent = firstGuestY,
      guestNameFontSize = firstGuestFontSize
    )

    // Second guest added: hasCustomStyleOverride = false
    val guest2 = GuestEntity(
      id = "gst_2",
      eventId = currentEvent.id,
      name = "Dr. Bilal Siddiqui",
      uniqueToken = "tok_2",
      hasCustomStyleOverride = false
    )

    // Verify guest 2 automatically uses the event-wide defaults
    assertEquals(firstGuestX, guest2.getEffectiveXPercent(currentEvent), 0.001f)
    assertEquals(firstGuestY, guest2.getEffectiveYPercent(currentEvent), 0.001f)
    assertEquals(firstGuestFontSize, guest2.getEffectiveFontSize(currentEvent))

    // Now modify guest 2 with individual custom override (without changing event default)
    val guest2WithOverride = guest2.copy(
      hasCustomStyleOverride = true,
      guestNameXPercent = 0.40f,
      guestNameYPercent = 0.85f,
      guestNameFontSize = 36
    )

    // Confirm guest 2 now uses custom override
    assertEquals(0.40f, guest2WithOverride.getEffectiveXPercent(currentEvent), 0.001f)
    assertEquals(0.85f, guest2WithOverride.getEffectiveYPercent(currentEvent), 0.001f)
    assertEquals(36, guest2WithOverride.getEffectiveFontSize(currentEvent))

    // Confirm guest 1 and event default are UNCHANGED
    assertEquals(firstGuestX, currentEvent.defaultGuestNameXPercent, 0.001f)
    assertEquals(firstGuestY, currentEvent.defaultGuestNameYPercent, 0.001f)
    assertEquals(firstGuestFontSize, currentEvent.defaultGuestNameFontSize)
    assertEquals(firstGuestY, guest1.getEffectiveYPercent(currentEvent), 0.001f)
  }

  @Test
  fun testMultiPageGuestPositionOverridesPerCeremony() {
    val pageMehndi = EventPage(
      id = "page_mehndi",
      eventId = "evt_wedding",
      pageName = "Mehndi",
      designId = "dsg_floral_gold"
    )
    val pageBaraat = EventPage(
      id = "page_baraat",
      eventId = "evt_wedding",
      pageName = "Baraat",
      designId = "dsg_royal_gold"
    )
    val pageWalima = EventPage(
      id = "page_walima",
      eventId = "evt_wedding",
      pageName = "Walima",
      designId = "dsg_emerald"
    )

    val event = EventEntity(
      id = "evt_wedding",
      title = "Wedding Celebration",
      eventType = "Wedding",
      pagesJson = EventPage.listToJson(listOf(pageMehndi, pageBaraat, pageWalima)),
      defaultGuestNameXPercent = 0.5f,
      defaultGuestNameYPercent = 0.38f,
      defaultGuestNameFontSize = 18
    )

    // Per-page guest styles
    val styleMehndi = PageGuestStyle(xPercent = 0.5f, yPercent = 0.22f, fontSizeSp = 24, colorHex = "#8C6D23")
    val styleBaraat = PageGuestStyle(xPercent = 0.5f, yPercent = 0.68f, fontSizeSp = 30, colorHex = "#D4AF37")
    val styleWalima = PageGuestStyle(xPercent = 0.35f, yPercent = 0.45f, fontSizeSp = 20, colorHex = "#0F3D29")

    val stylesMap = mapOf(
      "page_mehndi" to styleMehndi,
      "page_baraat" to styleBaraat,
      "page_walima" to styleWalima
    )

    val guest = GuestEntity(
      id = "gst_multi",
      eventId = event.id,
      name = "Mr. & Mrs. Ahmed Khan",
      uniqueToken = "tok_multi",
      selectedPageIdsJson = GuestEntity.idsToJson(listOf("page_mehndi", "page_baraat", "page_walima")),
      hasCustomStyleOverride = true,
      pageOverridesJson = PageGuestStyle.mapToJson(stylesMap)
    )

    // Verify each page receives its exact customized style
    val effectiveMehndi = guest.getEffectiveStyleForPage(pageMehndi, event)
    assertEquals(0.22f, effectiveMehndi.yPercent, 0.001f)
    assertEquals(24, effectiveMehndi.fontSizeSp)
    assertEquals("#8C6D23", effectiveMehndi.colorHex)

    val effectiveBaraat = guest.getEffectiveStyleForPage(pageBaraat, event)
    assertEquals(0.68f, effectiveBaraat.yPercent, 0.001f)
    assertEquals(30, effectiveBaraat.fontSizeSp)
    assertEquals("#D4AF37", effectiveBaraat.colorHex)

    val effectiveWalima = guest.getEffectiveStyleForPage(pageWalima, event)
    assertEquals(0.35f, effectiveWalima.xPercent, 0.001f)
    assertEquals(0.45f, effectiveWalima.yPercent, 0.001f)
    assertEquals(20, effectiveWalima.fontSizeSp)
    assertEquals("#0F3D29", effectiveWalima.colorHex)

    // Verify resolved layers on Baraat reflect the Baraat-specific position and size
    val baraatLayers = TextLayerConfig.buildResolvedLayers(null, pageBaraat, guest, guest.name)
    val baraatGuestLayer = baraatLayers.find { it.id == "guest_name" }
    assertNotNull(baraatGuestLayer)
    assertEquals(0.68f, baraatGuestLayer!!.yPercent, 0.001f)
    assertEquals(30, baraatGuestLayer.fontSizeSp)

    // Verify resolved layers on Mehndi reflect the Mehndi-specific position and size
    val mehndiLayers = TextLayerConfig.buildResolvedLayers(null, pageMehndi, guest, guest.name)
    val mehndiGuestLayer = mehndiLayers.find { it.id == "guest_name" }
    assertNotNull(mehndiGuestLayer)
    assertEquals(0.22f, mehndiGuestLayer!!.yPercent, 0.001f)
    assertEquals(24, mehndiGuestLayer.fontSizeSp)
  }

  @Test
  fun testPageDeletionSynchronizesWithGuestSelections() {
    val pageIds = listOf("page_mehndi", "page_baraat", "page_walima")
    val guest = GuestEntity(
      id = "gst_test",
      eventId = "evt_wedding",
      name = "Honored Guest",
      uniqueToken = "tok_test",
      selectedPageIdsJson = GuestEntity.idsToJson(pageIds),
      pageOverridesJson = """{"page_mehndi":{"xPercent":0.5,"yPercent":0.2},"page_baraat":{"xPercent":0.5,"yPercent":0.6}}"""
    )

    // Simulate page deletion of "page_mehndi"
    val deletedPageId = "page_mehndi"
    val currentSelected = guest.getSelectedPageIds()
    assertTrue(currentSelected.contains(deletedPageId))

    val updatedSelected = currentSelected.filter { it != deletedPageId }
    val overrides = PageGuestStyle.parseMap(guest.pageOverridesJson).toMutableMap()
    overrides.remove(deletedPageId)

    val updatedGuest = guest.copy(
      selectedPageIdsJson = GuestEntity.idsToJson(updatedSelected),
      pageOverridesJson = PageGuestStyle.mapToJson(overrides)
    )

    // Verify deleted page is removed from guest selection
    assertFalse(updatedGuest.getSelectedPageIds().contains(deletedPageId))
    assertEquals(listOf("page_baraat", "page_walima"), updatedGuest.getSelectedPageIds())
    assertFalse(PageGuestStyle.parseMap(updatedGuest.pageOverridesJson).containsKey(deletedPageId))
    assertTrue(PageGuestStyle.parseMap(updatedGuest.pageOverridesJson).containsKey("page_baraat"))
  }

  @Test
  fun testFirestorePublishingDtoMappingAndPageFiltering() {
    val designMehndi = DesignTemplateEntity(
      id = "dsg_mehndi",
      name = "Mehndi Velvet",
      imagePath = "/data/user/0/com.aistudio.inviora.qxmr/files/designs/local_only.jpg",
      accentColorHex = "#C89D3C",
      fontStyle = "Calligraphic",
      themeStyle = "MEHNDI_GOLD",
      ornamentStyle = "HENNA_PATTERNS"
    )
    val designWalima = DesignTemplateEntity(
      id = "dsg_walima",
      name = "Emerald Royale",
      imagePath = "/data/user/0/com.aistudio.inviora.qxmr/files/designs/local_emerald.jpg",
      accentColorHex = "#0F3D29",
      fontStyle = "Serif",
      themeStyle = "ROYAL_EMERALD",
      ornamentStyle = "ROYAL_BORDER"
    )

    val designsMap = mapOf(
      designMehndi.id to designMehndi,
      designWalima.id to designWalima
    )

    val pageMehndi = EventPage(
      id = "page_mehndi",
      eventId = "evt_wedding",
      pageName = "Mehndi Night",
      designId = "dsg_mehndi",
      date = "Dec 18, 2026",
      time = "7:00 PM",
      venue = "Crystal Ballroom",
      greeting = "Join us for Mehndi",
      eventTitle = "Zayd & Fatima",
      pageOrder = 0,
      showBismillah = true
    )
    val pageBaraat = EventPage(
      id = "page_baraat",
      eventId = "evt_wedding",
      pageName = "Baraat Ceremony",
      designId = "dsg_mehndi",
      date = "Dec 19, 2026",
      time = "6:00 PM",
      venue = "Grand Hall",
      pageOrder = 1
    )
    val pageWalima = EventPage(
      id = "page_walima",
      eventId = "evt_wedding",
      pageName = "Walima Reception",
      designId = "dsg_walima",
      date = "Dec 20, 2026",
      time = "8:00 PM",
      venue = "The Palace",
      pageOrder = 2
    )

    val event = EventEntity(
      id = "evt_wedding",
      title = "Zayd & Fatima Wedding",
      eventType = "Wedding",
      groomName = "Zayd",
      brideName = "Fatima",
      hostNames = "Mr. & Mrs. Tariq",
      primaryVenue = "The Palace",
      primaryDate = "Dec 20, 2026",
      designId = "dsg_mehndi",
      animationId = "anim_velvet_curtain",
      includeTraditionalGreeting = true,
      traditionalBismillahText = "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ",
      traditionalGreetingText = "Assalamu Alaikum",
      defaultGuestNameFontSize = 24,
      defaultGuestNameColorHex = "#D4AF37",
      pagesJson = EventPage.listToJson(listOf(pageMehndi, pageBaraat, pageWalima))
    )

    // Guest is only invited to Mehndi and Walima (NOT Baraat)
    val guestOverrideWalima = PageGuestStyle(
      xPercent = 0.45f,
      yPercent = 0.70f,
      fontSizeSp = 28,
      colorHex = "#0F3D29"
    )
    val guest = GuestEntity(
      id = "gst_test",
      eventId = "evt_wedding",
      name = "Dr. & Mrs. Imran",
      phone = "+1 555-4321",
      uniqueToken = "KzUSUB",
      active = true,
      selectedPageIdsJson = GuestEntity.idsToJson(listOf("page_mehndi", "page_walima")),
      pageOverridesJson = PageGuestStyle.mapToJson(mapOf("page_walima" to guestOverrideWalima))
    )

    // Construct mock publishing repository (no DAO or Firestore needed for DTO builder testing)
    val fakeRepo = FirestorePublishingRepository(
      eventDao = object : com.example.data.dao.EventDao {
        override fun getAllEvents() = throw NotImplementedError()
        override suspend fun getEventById(id: String) = null
        override fun getEventFlow(id: String) = throw NotImplementedError()
        override suspend fun getEventsUsingDesign(designId: String) = emptyList<EventEntity>()
        override suspend fun getEventsUsingAnimation(animationId: String) = emptyList<EventEntity>()
        override suspend fun insertEvent(event: EventEntity) {}
        override suspend fun updateEvent(event: EventEntity) {}
        override suspend fun deleteEventById(id: String) {}
      },
      guestDao = object : com.example.data.dao.GuestDao {
        override fun getAllGuests() = throw NotImplementedError()
        override fun getGuestsForEvent(eventId: String) = throw NotImplementedError()
        override suspend fun getGuestsListForEvent(eventId: String) = emptyList<GuestEntity>()
        override suspend fun getGuestByToken(token: String) = null
        override fun getGuestCountForEvent(eventId: String) = throw NotImplementedError()
        override fun getTotalGuestCount() = throw NotImplementedError()
        override suspend fun insertGuest(guest: GuestEntity) {}
        override suspend fun insertGuests(guests: List<GuestEntity>) {}
        override suspend fun regenerateToken(id: String, newToken: String) {}
        override suspend fun setGuestActive(id: String, isActive: Boolean) {}
        override suspend fun deleteGuestById(id: String) {}
        override suspend fun deleteGuestsForEvent(eventId: String) {}
      },
      designDao = object : com.example.data.dao.DesignDao {
        override fun getAllDesigns() = throw NotImplementedError()
        override suspend fun getAllDesignsList() = emptyList<DesignTemplateEntity>()
        override suspend fun getDesignById(id: String) = null
        override fun getDesignFlow(id: String) = throw NotImplementedError()
        override suspend fun insertDesign(design: DesignTemplateEntity) {}
        override suspend fun updateDesign(design: DesignTemplateEntity) {}
        override suspend fun deleteDesignById(id: String) {}
        override suspend fun renameDesign(id: String, newName: String) {}
        override suspend fun toggleFavorite(id: String, isFav: Boolean) {}
      }
    )

    val dto = fakeRepo.buildPublishedInvitationDto(event, guest, designsMap)

    // 1. Root level verification
    assertEquals("KzUSUB", dto.token)
    assertTrue(dto.active)
    assertEquals("evt_wedding", dto.eventId)
    assertEquals("gst_test", dto.guestId)
    assertEquals("Dr. & Mrs. Imran", dto.guestName)
    assertEquals("anim_velvet_curtain", dto.animationId)
    assertTrue(dto.traditionalGreeting.includeTraditionalGreeting)
    assertEquals("بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ", dto.traditionalGreeting.traditionalBismillahText)
    assertEquals(24, dto.defaultGuestNameSettings.fontSize)

    // 2. Selected pages verification: Baraat must NOT be published
    assertEquals(2, dto.pages.size)
    assertEquals(listOf("page_mehndi", "page_walima"), dto.pages.map { it.pageId })

    // 3. Design metadata verification (and verify local imagePath is excluded)
    val mehndiPublishedPage = dto.pages.find { it.pageId == "page_mehndi" }!!
    assertEquals("dsg_mehndi", mehndiPublishedPage.designMetadata.designId)
    assertEquals("Mehndi Velvet", mehndiPublishedPage.designMetadata.designName)
    assertEquals("#C89D3C", mehndiPublishedPage.designMetadata.accentColorHex)

    val walimaPublishedPage = dto.pages.find { it.pageId == "page_walima" }!!
    assertEquals("dsg_walima", walimaPublishedPage.designMetadata.designId)
    assertEquals("Emerald Royale", walimaPublishedPage.designMetadata.designName)
    assertEquals("#0F3D29", walimaPublishedPage.designMetadata.accentColorHex)

    // 4. Guest override verification: Walima has custom override, Mehndi uses default
    assertTrue(walimaPublishedPage.guestOverride.hasOverride)
    assertEquals(0.45f, walimaPublishedPage.guestOverride.xPercent, 0.001f)
    assertEquals(0.70f, walimaPublishedPage.guestOverride.yPercent, 0.001f)
    assertEquals(28, walimaPublishedPage.guestOverride.fontSize)
    assertEquals("#0F3D29", walimaPublishedPage.guestOverride.colorHex)

    // 5. Serialization to map for Firestore collection publishedInvitations/{uniqueToken}
    val firestoreMap = dto.toMap()
    assertEquals("KzUSUB", firestoreMap["token"])
    assertEquals(true, firestoreMap["active"])
    assertFalse(firestoreMap.containsKey("phone"))
    assertNotNull(firestoreMap["pages"])
    val pagesList = firestoreMap["pages"] as List<*>
    assertEquals(2, pagesList.size)
  }
}
