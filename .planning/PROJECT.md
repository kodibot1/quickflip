# QuickFlip

## What This Is

An Android app for rapidly generating marketplace listings for secondhand furniture and electronics. Snap photos, get AI feedback on photo quality, auto-generate platform-specific descriptions (TradeMe and Facebook Marketplace), and manage listings through their lifecycle. Built for personal use by a couple in Auckland.

## Core Value

Photos in, ready-to-post listings out — as fast as possible. The AI removes the friction of writing descriptions and judging photo quality so you can list more items in less time.

## Requirements

### Validated

(None yet — ship to validate)

### Active

- [ ] Capture up to 5 photos per listing via CameraX
- [ ] Gemini Vision scores each photo (lighting, background, angle, focus, composition 1-5) with improvement suggestions
- [ ] Retake or keep each photo based on feedback
- [ ] Auto-detect item, condition, and key features from photos via Gemini
- [ ] Generate TradeMe listing (formal, professional, max 80 char title)
- [ ] Generate Facebook listing (casual, emoji OK, max 60 char title)
- [ ] Edit listing with photo carousel, tabbed TradeMe/Facebook descriptions
- [ ] Manual price entry (no AI pricing)
- [ ] Per-field copy-to-clipboard buttons (title, description, price, condition, location each copyable individually)
- [ ] Listing status lifecycle: Draft → Listed → Sold
- [ ] Dashboard with stats (X Listed, Y Sold, $Z Total)
- [ ] Duplicate, edit, delete listings
- [ ] Queue AI processing for when offline (save photos, process when back online)
- [ ] Settings: default pickup location (Auckland), default category, Gemini API key, dark mode
- [ ] Local-only photo storage (app internal storage)

### Out of Scope

- Cloud backup / sync — local-only app, no server
- AI-suggested pricing — manual pricing is intentional
- Direct TradeMe/Facebook API integration — copy-paste workflow is sufficient
- Multi-user / accounts — personal tool for two people
- iOS version — Android only

## Context

- Target users: the developer and their partner, selling secondhand items in Auckland NZ
- Primary platforms: TradeMe (NZ's dominant marketplace) and Facebook Marketplace
- TradeMe tone is formal/professional; Facebook tone is casual/conversational with emoji
- Gemini Vision API handles both photo quality feedback and listing generation
- Offline queueing needed since AI features depend on network connectivity

## Constraints

- **Platform**: Android only (Kotlin, Jetpack Compose, Material 3)
- **Min SDK**: 26, Target SDK 34
- **Architecture**: MVVM
- **AI Provider**: Gemini Vision API (com.google.ai.client.generativeai:generativeai:0.2.0)
- **Storage**: Room for listings, DataStore for preferences, local filesystem for photos
- **Camera**: CameraX for photo capture
- **Image Loading**: Coil for Compose
- **Navigation**: Jetpack Navigation Compose

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Per-field copy buttons instead of bulk copy | User wants to paste into individual platform fields | — Pending |
| Manual pricing only | User knows item values, AI guessing adds no value | — Pending |
| Offline queue for AI processing | Network may be unavailable during photo capture | — Pending |
| Local-only storage | No need for cloud, keeps app simple | — Pending |
| Two distinct listing tones | TradeMe and Facebook have different audience expectations | — Pending |

---
*Last updated: 2026-01-31 after initialization*
