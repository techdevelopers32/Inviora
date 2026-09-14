# Inviora Guest Web Viewer

Standalone, client-side digital invitation viewer for guests opening invitation links via web browsers (WhatsApp, Safari, Chrome, Android, iOS, Desktop).

## Hosting Structure & URL Routing
- **Future Guest Invitation URL**:
  `https://techdevelopers32.github.io/Inviora/invite/{uniqueToken}`
  *(Example: `https://techdevelopers32.github.io/Inviora/invite/KzUSUB`)*
- **Data Source**: Read-only access from Firebase Firestore collection `publishedInvitations/{uniqueToken}`
- **Zero-framework**: Pure HTML, CSS, JavaScript (no Node.js build step or runtime needed for hosting).

## File Structure
- `index.html`: Main SPA application template.
- `404.html`: GitHub Pages fallback router handling dynamic `/invite/{token}` deep links.
- `styles.css`: Material / Luxury styling, portrait card container (`aspect-ratio: 0.68`), and cinematic curtain animation.
- `app.js`: Token extraction, Firestore loader, dynamic percentage layer positioning, multi-page ceremony switcher, swipe gestures.
- `assets/`: Vector background artwork (`luxury_bg_gold.svg`, `luxury_bg_emerald.svg`).

## GitHub Pages Deployment Options
1. **GitHub Actions (Recommended)**: Set Pages Source to **GitHub Actions**. Deploy `./web` directory directly to GitHub Pages.
2. **Branch Publishing**: Copy or point branch to publish `./web` directly or to `gh-pages` branch.
