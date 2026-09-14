/**
 * Inviora Guest Web Viewer
 * Standalone client-side viewer for published digital invitations.
 * 
 * Target Firestore collection: publishedInvitations/{uniqueToken}
 */

// Firebase client configuration (Client-side read-only access)
const FIREBASE_CONFIG = {
  apiKey: "AIzaSyCb08SkeDxYqmSv8x6Fu6I2CaRLlHDBOXI",
  authDomain: "inviora-cbbd9.firebaseapp.com",
  projectId: "inviora-cbbd9",
  storageBucket: "inviora-cbbd9.firebasestorage.app",
  messagingSenderId: "126604990983"
};

// Global App State
const state = {
  token: null,
  invitation: null,
  currentPageIndex: 0,
  isRevealed: false,
  touchStartX: 0,
  touchEndX: 0
};

/**
 * 1. URL Token Extraction
 * Extracts the invitation token from various routing structures:
 * - Path: /Inviora/invite/{token}
 * - Path: /invite/{token}
 * - Path: /Inviora/web/invite/{token}
 * - Query: ?invite={token} or ?token={token}
 * - Hash: #/invite/{token}
 */
function extractInvitationToken() {
  const url = new URL(window.location.href);

  // 1. Query parameters
  const queryToken = url.searchParams.get('invite') || url.searchParams.get('token') || url.searchParams.get('id');
  if (queryToken && queryToken.trim()) {
    return queryToken.trim();
  }

  // 2. Hash routing: #/invite/{token}
  const hash = window.location.hash;
  if (hash) {
    const hashMatch = hash.match(/\/invite\/([a-zA-Z0-9_-]+)/i);
    if (hashMatch && hashMatch[1]) {
      return hashMatch[1];
    }
  }

  // 3. Pathname segments: /invite/{token}
  const pathname = window.location.pathname;
  const pathSegments = pathname.split('/').filter(Boolean);
  const inviteIndex = pathSegments.findIndex(segment => segment.toLowerCase() === 'invite');
  if (inviteIndex !== -1 && inviteIndex + 1 < pathSegments.length) {
    return pathSegments[inviteIndex + 1];
  }

  // 4. If path ends with token after /invite/
  const directMatch = pathname.match(/\/invite\/([a-zA-Z0-9_-]+)/i);
  if (directMatch && directMatch[1]) {
    return directMatch[1];
  }

  return null;
}

/**
 * Computes the base path for assets based on the current URL
 * to ensure assets resolve to /Inviora/assets/... on GitHub Pages (deployed from /web)
 * or ./assets/... during local development.
 */
function getAssetBaseUrl() {
  const path = window.location.pathname;
  if (path.includes('/Inviora/')) {
    return '/Inviora/';
  }
  if (path.includes('/web/')) {
    return '/web/';
  }
  return './';
}

/**
 * 2. Design Image & Artwork Provider (Temporary Test-Image Isolation)
 * Keeps design-image retrieval completely isolated.
 * Ready to receive Firebase Storage URLs in future steps without refactoring.
 */
function getDesignArtwork(page, designMetadata) {
  // Production hook: If public web URL or Firebase Storage URL is present in the published page:
  if (page && page.publicImageUrl) {
    return page.publicImageUrl;
  }
  if (designMetadata && designMetadata.publicImageUrl) {
    return designMetadata.publicImageUrl;
  }

  // Temporary test-image fallback:
  // Returns luxury vector SVG background matching the ceremony/design theme style
  const theme = (designMetadata && designMetadata.themeStyle) ? designMetadata.themeStyle.toUpperCase() : 'ROYAL_GOLD';
  const baseUrl = getAssetBaseUrl();

  if (theme.includes('EMERALD') || theme.includes('PEACOCK') || theme.includes('MEHNDI')) {
    return `${baseUrl}assets/luxury_bg_emerald.svg`;
  }
  return `${baseUrl}assets/luxury_bg_gold.svg`;
}

/**
 * 3. Dynamic Text Positioning Engine
 * Maps normalized percentage coordinates (xPercent, yPercent: 0.0 - 1.0)
 * directly into the CSS container of the card, matching Android InvitationCardRenderer.
 */
function buildResolvedLayers(invitation, page) {
  const isWedding = (invitation.eventType || '').toLowerCase() === 'wedding' || !invitation.eventType;
  const isLightText = (page.designMetadata?.themeStyle || '').toUpperCase().includes('EMERALD');

  // Arabic Bismillah text
  const bismillahAr = (page.bismillahArabic && page.bismillahArabic.trim()) 
    ? page.bismillahArabic 
    : "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ";

  // English translation
  const bismillahEn = (page.bismillahEnglish && page.bismillahEnglish.trim()) 
    ? page.bismillahEnglish 
    : "In the name of Allah, the Most Gracious, the Most Merciful.";

  // Couple / Title text
  let coupleTitle = "";
  if (isWedding) {
    if (invitation.groomName && invitation.brideName) {
      coupleTitle = `${invitation.groomName} & ${invitation.brideName}`;
    } else if (invitation.eventTitle) {
      coupleTitle = invitation.eventTitle;
    } else {
      coupleTitle = page.eventTitle || "Wedding Celebration";
    }
  } else {
    coupleTitle = page.eventTitle || invitation.eventTitle || "Celebration";
  }

  // Guest Name & Style resolution hierarchy:
  // 1. Guest-specific page override
  // 2. Page-level custom guest defaults
  // 3. Event-level default guest name settings
  let guestStyle = {
    xPercent: 0.5,
    yPercent: 0.38,
    fontSize: 18,
    colorHex: isLightText ? "#F7E7A9" : "#8C6D23",
    fontFamily: "Serif",
    fontWeight: "Bold",
    fontStyle: "Italic",
    alignment: "Center"
  };

  if (page.guestOverride && page.guestOverride.hasOverride) {
    guestStyle = { ...page.guestOverride };
  } else if (page.pageGuestSettings && page.pageGuestSettings.hasCustomGuestDefault) {
    guestStyle = {
      xPercent: page.pageGuestSettings.defaultGuestXPercent,
      yPercent: page.pageGuestSettings.defaultGuestYPercent,
      fontSize: page.pageGuestSettings.defaultGuestFontSize,
      colorHex: page.pageGuestSettings.defaultGuestColorHex,
      fontFamily: page.pageGuestSettings.defaultGuestFontFamily,
      fontWeight: page.pageGuestSettings.defaultGuestFontWeight,
      fontStyle: page.pageGuestSettings.defaultGuestFontStyle,
      alignment: page.pageGuestSettings.defaultGuestAlignment
    };
  } else if (invitation.defaultGuestNameSettings) {
    guestStyle = { ...invitation.defaultGuestNameSettings };
  }

  // Greeting text fallback logic:
  // 1. Page's own greeting if not blank
  // 2. Published invitation's invitationWording if page greeting is blank
  // 3. Host names if both are blank
  let resolvedGreeting = "";
  if (page.greeting && page.greeting.trim()) {
    resolvedGreeting = page.greeting.trim();
  } else if (invitation.invitationWording && invitation.invitationWording.trim()) {
    resolvedGreeting = invitation.invitationWording.trim();
  } else if (invitation.hostNames && invitation.hostNames.trim()) {
    resolvedGreeting = invitation.hostNames.trim();
  }

  // Base default layers matching Android TextLayerConfig.kt
  const defaultLayers = [
    {
      id: "bismillah_arabic",
      text: bismillahAr,
      xPercent: 0.5,
      yPercent: 0.08,
      fontSizeSp: 20,
      fontWeight: "600",
      fontFamily: "'Amiri', serif",
      alignment: "Center",
      colorHex: isLightText ? "#F7E7A9" : "#8C6D23",
      isVisible: isWedding && page.showBismillah
    },
    {
      id: "bismillah_english",
      text: bismillahEn,
      xPercent: 0.5,
      yPercent: 0.13,
      fontSizeSp: 10,
      fontStyle: "Italic",
      fontFamily: "'Cormorant Garamond', Georgia, serif",
      alignment: "Center",
      colorHex: isLightText ? "#D1C7BD" : "#5C544C",
      isVisible: isWedding && page.showBismillah
    },
    {
      id: "couple_names",
      text: coupleTitle,
      xPercent: 0.5,
      yPercent: isWedding ? 0.22 : 0.14,
      fontSizeSp: 24,
      fontWeight: "700",
      fontFamily: "'Cinzel', 'Playfair Display', serif",
      alignment: "Center",
      colorHex: isLightText ? "#FFFFFF" : "#1E1A16",
      isVisible: true
    },
    {
      id: "page_name",
      text: page.pageName ? `— ${page.pageName.toUpperCase()} —` : "",
      xPercent: 0.5,
      yPercent: isWedding ? 0.31 : 0.22,
      fontSizeSp: 13,
      fontWeight: "600",
      fontFamily: "'Cinzel', serif",
      alignment: "Center",
      colorHex: isLightText ? "#F7E7A9" : "#8C6D23",
      isVisible: !!(page.pageName && page.pageName.trim())
    },
    {
      id: "guest_name",
      text: invitation.guestName || "",
      xPercent: guestStyle.xPercent,
      yPercent: guestStyle.yPercent,
      fontSizeSp: guestStyle.fontSize,
      fontWeight: guestStyle.fontWeight || "Bold",
      fontStyle: guestStyle.fontStyle || "Italic",
      fontFamily: guestStyle.fontFamily?.toLowerCase().includes("sans") ? "'Plus Jakarta Sans', sans-serif" : "'Cormorant Garamond', Georgia, serif",
      alignment: guestStyle.alignment || "Center",
      colorHex: guestStyle.colorHex || (isLightText ? "#F7E7A9" : "#8C6D23"),
      isVisible: !!(invitation.guestName && invitation.guestName.trim())
    },
    {
      id: "greeting",
      text: resolvedGreeting,
      xPercent: 0.5,
      yPercent: isWedding ? 0.46 : 0.37,
      fontSizeSp: 12,
      fontStyle: "Italic",
      fontFamily: "'Cormorant Garamond', Georgia, serif",
      alignment: "Center",
      colorHex: isLightText ? "#E2DDD5" : "#4A443D",
      isVisible: !!resolvedGreeting
    },
    {
      id: "date",
      text: page.date || "",
      xPercent: 0.5,
      yPercent: 0.58,
      fontSizeSp: 14,
      fontWeight: "600",
      fontFamily: "'Plus Jakarta Sans', sans-serif",
      alignment: "Center",
      colorHex: isLightText ? "#FFFFFF" : "#1E1A16",
      isVisible: !!(page.date && page.date.trim())
    },
    {
      id: "time",
      text: page.time || "",
      xPercent: 0.5,
      yPercent: 0.64,
      fontSizeSp: 13,
      fontFamily: "'Plus Jakarta Sans', sans-serif",
      alignment: "Center",
      colorHex: isLightText ? "#D1C7BD" : "#4A443D",
      isVisible: !!(page.time && page.time.trim())
    },
    {
      id: "venue",
      text: page.venue || "",
      xPercent: 0.5,
      yPercent: 0.72,
      fontSizeSp: 13,
      fontWeight: "600",
      fontFamily: "'Plus Jakarta Sans', sans-serif",
      alignment: "Center",
      colorHex: isLightText ? "#FFFFFF" : "#2C2621",
      isVisible: !!(page.venue && page.venue.trim())
    },
    {
      id: "details",
      text: page.additionalDetails || "",
      xPercent: 0.5,
      yPercent: 0.82,
      fontSizeSp: 11,
      fontFamily: "'Plus Jakarta Sans', sans-serif",
      alignment: "Center",
      colorHex: isLightText ? "#C2B8AD" : "#70675E",
      isVisible: !!(page.additionalDetails && page.additionalDetails.trim())
    }
  ];

  // Merge any saved customizations from page.textLayoutJson
  if (page.textLayoutJson) {
    try {
      const savedLayers = JSON.parse(page.textLayoutJson);
      const savedMap = {};
      savedLayers.forEach(l => { if (l.id) savedMap[l.id] = l; });

      return defaultLayers.map(def => {
        const custom = savedMap[def.id];
        if (!custom) return def;

        // For guest_name, preserve guest layout hierarchy unless overridden
        const x = (def.id === 'guest_name') ? def.xPercent : (custom.xPercent ?? def.xPercent);
        const y = (def.id === 'guest_name') ? def.yPercent : (custom.yPercent ?? def.yPercent);
        const size = (def.id === 'guest_name') ? def.fontSizeSp : (custom.fontSizeSp ?? def.fontSizeSp);

        return {
          ...def,
          xPercent: x,
          yPercent: y,
          fontSizeSp: size,
          fontWeight: custom.fontWeight ?? def.fontWeight,
          fontStyle: custom.fontStyle ?? def.fontStyle,
          colorHex: custom.colorHex ?? def.colorHex,
          alignment: custom.alignment ?? def.alignment,
          isVisible: custom.isVisible !== undefined ? custom.isVisible : def.isVisible
        };
      });
    } catch (e) {
      console.warn("Could not parse textLayoutJson:", e);
    }
  }

  return defaultLayers;
}

/**
 * 4. Renders the Active Invitation Page Card
 */
function renderPageCard(pageIndex) {
  const invitation = state.invitation;
  if (!invitation || !invitation.pages || invitation.pages.length === 0) return;

  const page = invitation.pages[pageIndex];
  if (!page) return;

  state.currentPageIndex = pageIndex;

  const cardElement = document.getElementById('invitationCard');
  const layersContainer = document.getElementById('cardLayers');
  const artworkElement = document.getElementById('cardArtwork');
  const prevBtn = document.getElementById('cardPrevBtn');
  const nextBtn = document.getElementById('cardNextBtn');

  // Set background artwork
  const artworkUrl = getDesignArtwork(page, page.designMetadata);
  artworkElement.src = artworkUrl;
  artworkElement.alt = page.pageName || 'Invitation Card Artwork';

  // Clear previous layers
  layersContainer.innerHTML = '';

  // Build resolved layers using normalized percentage coordinates
  const layers = buildResolvedLayers(invitation, page);

  layers.forEach(layer => {
    if (!layer.isVisible || !layer.text) return;

    const el = document.createElement('div');
    el.className = 'text-layer';
    el.style.setProperty('--x-percent', layer.xPercent);
    el.style.setProperty('--y-percent', layer.yPercent);
    el.style.setProperty('--font-size-sp', layer.fontSizeSp);
    el.style.setProperty('--color', layer.colorHex);
    el.style.setProperty('--font-family', layer.fontFamily);
    el.style.setProperty('--font-weight', layer.fontWeight === 'Bold' ? '700' : (layer.fontWeight === 'SemiBold' ? '600' : '400'));
    el.style.setProperty('--font-style', layer.fontStyle === 'Italic' ? 'italic' : 'normal');
    el.style.setProperty('--text-align', layer.alignment ? layer.alignment.toLowerCase() : 'center');

    // High contrast shadow matching Android InvitationCardRenderer
    const isLight = isColorLight(layer.colorHex);
    const shadowColor = isLight ? 'rgba(0, 0, 0, 0.85)' : 'rgba(255, 255, 255, 0.85)';
    el.style.setProperty('--shadow', `1px 1px 4px ${shadowColor}`);

    el.innerText = layer.text;
    layersContainer.appendChild(el);
  });

  // Navigation Arrows Visibility
  if (prevBtn && nextBtn) {
    if (pageIndex === 0) {
      prevBtn.classList.add('hidden');
    } else {
      prevBtn.classList.remove('hidden');
    }

    if (pageIndex === invitation.pages.length - 1) {
      nextBtn.classList.add('hidden');
    } else {
      nextBtn.classList.remove('hidden');
    }
  }

  // Update Page Navigation Tabs & Counter
  renderNavigationTabs(pageIndex);
}

/**
 * 5. Ceremony Tabs & Page Counter
 */
function renderNavigationTabs(currentIndex) {
  const tabsContainer = document.getElementById('ceremonyTabs');
  const counterElement = document.getElementById('pageCounter');
  const invitation = state.invitation;

  if (!tabsContainer || !invitation) return;

  tabsContainer.innerHTML = '';

  if (invitation.pages.length <= 1) {
    if (counterElement) counterElement.innerText = '';
    return;
  }

  if (counterElement) {
    counterElement.innerText = `Ceremony ${currentIndex + 1} of ${invitation.pages.length}`;
  }

  invitation.pages.forEach((page, idx) => {
    const tabBtn = document.createElement('button');
    tabBtn.className = `ceremony-tab-btn ${idx === currentIndex ? 'active' : ''}`;
    tabBtn.innerText = page.pageName || `Ceremony ${idx + 1}`;
    tabBtn.onclick = () => {
      renderPageCard(idx);
    };
    tabsContainer.appendChild(tabBtn);
  });
}

/**
 * Helper to determine text brightness for optimal contrast shadow
 */
function isColorLight(hexColor) {
  if (!hexColor || hexColor.length < 7) return true;
  try {
    const r = parseInt(hexColor.slice(1, 3), 16);
    const g = parseInt(hexColor.slice(3, 5), 16);
    const b = parseInt(hexColor.slice(5, 7), 16);
    const brightness = (r * 299 + g * 587 + b * 114) / 1000;
    return brightness > 155;
  } catch (e) {
    return true;
  }
}

/**
 * 6. Cinematic Reveal (anim_velvet_curtain experience)
 */
function setupCinematicReveal(animationId) {
  const overlay = document.getElementById('cinematicOverlay');
  const seal = document.getElementById('centerSealWrapper');
  const sealMonogram = document.getElementById('sealMonogram');
  const invitation = state.invitation;

  // Set monogram initials on the golden seal
  if (sealMonogram && invitation) {
    if (invitation.groomName && invitation.brideName) {
      sealMonogram.innerText = `${invitation.groomName.charAt(0)}&${invitation.brideName.charAt(0)}`;
    } else if (invitation.eventTitle) {
      sealMonogram.innerText = invitation.eventTitle.charAt(0).toUpperCase();
    } else {
      sealMonogram.innerText = 'I';
    }
  }

  // Open curtain on tap/click
  seal.onclick = () => {
    triggerCurtainOpen();
  };

  const replayBtn = document.getElementById('replayExperienceBtn');
  if (replayBtn) {
    replayBtn.onclick = () => {
      triggerCurtainReplay();
    };
  }
}

function triggerCurtainOpen() {
  const overlay = document.getElementById('cinematicOverlay');
  if (!overlay) return;

  overlay.classList.add('parted');
  state.isRevealed = true;

  // After animation finishes, hide overlay pointer events
  setTimeout(() => {
    overlay.classList.add('hidden');
  }, 2300);
}

function triggerCurtainReplay() {
  const overlay = document.getElementById('cinematicOverlay');
  if (!overlay) return;

  overlay.classList.remove('hidden');
  void overlay.offsetWidth; // force reflow
  overlay.classList.remove('parted');
  state.isRevealed = false;
}

/**
 * 7. Touch Swipe Support for Mobile Browsers
 */
function setupTouchSwipe() {
  const card = document.getElementById('invitationCard');
  if (!card) return;

  card.addEventListener('touchstart', (e) => {
    state.touchStartX = e.changedTouches[0].screenX;
  }, { passive: true });

  card.addEventListener('touchend', (e) => {
    state.touchEndX = e.changedTouches[0].screenX;
    handleSwipeGesture();
  }, { passive: true });
}

function handleSwipeGesture() {
  const threshold = 45;
  const deltaX = state.touchEndX - state.touchStartX;
  const invitation = state.invitation;
  if (!invitation || !invitation.pages) return;

  if (deltaX < -threshold) {
    // Swiped left -> Next page
    if (state.currentPageIndex < invitation.pages.length - 1) {
      renderPageCard(state.currentPageIndex + 1);
    }
  } else if (deltaX > threshold) {
    // Swiped right -> Previous page
    if (state.currentPageIndex > 0) {
      renderPageCard(state.currentPageIndex - 1);
    }
  }
}

/**
 * 8. Status State Display Helpers
 */
function showScreen(screenId) {
  ['loadingScreen', 'errorScreen', 'rootPromptScreen', 'invitationStage'].forEach(id => {
    const el = document.getElementById(id);
    if (el) {
      if (id === screenId) {
        el.classList.remove('hidden');
      } else {
        el.classList.add('hidden');
      }
    }
  });
}

function showError(title, message) {
  showScreen('errorScreen');
  document.getElementById('errorTitle').innerText = title;
  document.getElementById('errorMessage').innerText = message;
  const overlay = document.getElementById('cinematicOverlay');
  if (overlay) overlay.classList.add('hidden');
}

/**
 * 9. Firestore Data Loading
 */
async function loadPublishedInvitation(token) {
  showScreen('loadingScreen');

  try {
    // Initialize Firebase
    if (!firebase.apps.length) {
      firebase.initializeApp(FIREBASE_CONFIG);
    }
    const db = firebase.firestore();

    const docRef = db.collection('publishedInvitations').doc(token);
    const snapshot = await docRef.get();

    if (!snapshot.exists) {
      showError(
        "Invitation Not Found",
        "We could not find an invitation matching this link. Please check with your host or confirm the invitation link."
      );
      return;
    }

    const data = snapshot.data();

    // Active status verification
    if (data.active === false) {
      showError(
        "Invitation No Longer Available",
        "This digital invitation is no longer active. Please contact the host for updated details."
      );
      return;
    }

    // Sort pages by pageOrder
    if (data.pages && Array.isArray(data.pages)) {
      data.pages.sort((a, b) => (a.pageOrder || 0) - (b.pageOrder || 0));
    }

    state.invitation = data;
    state.currentPageIndex = 0;

    // Reveal stage
    showScreen('invitationStage');
    renderPageCard(0);
    setupCinematicReveal(data.animationId || 'anim_velvet_curtain');

  } catch (error) {
    console.error("Error loading invitation from Firestore:", error);
    showError(
      "Unable to Load Invitation",
      "We were unable to load your invitation at this time. Please check your internet connection and refresh the page."
    );
  }
}

/**
 * App Initialization
 */
function init() {
  // Arrow navigation buttons
  const prevBtn = document.getElementById('cardPrevBtn');
  const nextBtn = document.getElementById('cardNextBtn');
  if (prevBtn) {
    prevBtn.onclick = () => {
      if (state.currentPageIndex > 0) renderPageCard(state.currentPageIndex - 1);
    };
  }
  if (nextBtn) {
    nextBtn.onclick = () => {
      if (state.invitation && state.currentPageIndex < state.invitation.pages.length - 1) {
        renderPageCard(state.currentPageIndex + 1);
      }
    };
  }

  setupTouchSwipe();

  // Extract token from URL
  const token = extractInvitationToken();
  state.token = token;

  if (token) {
    loadPublishedInvitation(token);
  } else {
    // If opened without a token (e.g. at root /Inviora/), show entry prompt
    showScreen('rootPromptScreen');
    const input = document.getElementById('manualTokenInput');
    const submitBtn = document.getElementById('manualTokenSubmit');
    if (submitBtn && input) {
      submitBtn.onclick = () => {
        const val = input.value.trim();
        if (val) {
          state.token = val;
          loadPublishedInvitation(val);
        }
      };
      input.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
          submitBtn.click();
        }
      });
    }
  }
}

// Start app when DOM is ready
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', init);
} else {
  init();
}
