# Roadmap: QuickFlip

## Overview

QuickFlip delivers "photos in, ready-to-post listings out" through 12 phases building from foundation to production. We start with local-only data and camera integration (Phases 1-3), enable the core AI value proposition with offline resilience (Phases 4-7), deliver competitive differentiators like dual-tone generation (Phases 8-9), complete the user experience (Phases 10-11), and polish for production (Phase 12). Every phase delivers verifiable user capabilities, building toward a fast, AI-powered listing generator for Auckland secondhand sellers.

## Phases

**Phase Numbering:**
- Integer phases (1, 2, 3): Planned milestone work
- Decimal phases (2.1, 2.2): Urgent insertions (marked with INSERTED)

Decimal phases appear between their surrounding integers in numeric order.

- [ ] **Phase 1: Foundation** - Project scaffolding, architecture, database schema
- [ ] **Phase 2: Camera Integration** - CameraX photo capture with lifecycle management
- [ ] **Phase 3: Photo Management** - Import, crop, rotate, storage
- [ ] **Phase 4: Settings & Preferences** - API key, defaults, dark mode
- [ ] **Phase 5: AI Photo Quality Analysis** - Gemini photo scoring and feedback
- [ ] **Phase 6: AI Listing Generation** - Item detection, dual-tone descriptions
- [ ] **Phase 7: AI Pricing Suggestions** - Price estimates from photos
- [ ] **Phase 8: Listing Editor** - Edit UI with carousel and platform tabs
- [ ] **Phase 9: Listing Lifecycle** - Status management and CRUD operations
- [ ] **Phase 10: Dashboard** - Home screen, stats, FAB
- [ ] **Phase 11: Offline Queue** - WorkManager for network failures
- [ ] **Phase 12: Polish & Production** - Performance, testing, onboarding

## Phase Details

### Phase 1: Foundation
**Goal**: Establish project structure, dependencies, and database foundation for local-first development
**Depends on**: Nothing (first phase)
**Requirements**: None (infrastructure)
**Success Criteria** (what must be TRUE):
  1. Android project builds successfully with all core dependencies (Compose, Room, Hilt, CameraX, Coil, DataStore)
  2. Room database schema compiles with Listing entity and basic DAO operations
  3. Hilt dependency injection graph works across app, data, and domain layers
  4. Navigation skeleton supports Camera → Listing Editor → Detail → Settings flows
  5. Material 3 theming applies consistently with light/dark mode support
**Plans**: TBD (5-10 plans)

Plans:
- [ ] 01-01: TBD during planning

### Phase 2: Camera Integration
**Goal**: Users can capture photos with proper lifecycle management and memory efficiency
**Depends on**: Phase 1
**Requirements**: CAM-01
**Success Criteria** (what must be TRUE):
  1. User can open camera from any screen and see live preview
  2. User can capture up to 5 photos per listing session
  3. Camera releases resources when user backgrounds app or navigates away
  4. Photos save to app-specific internal storage with relative paths (not absolute)
  5. App handles camera permissions gracefully with rationale UI
**Plans**: TBD (5-10 plans)

Plans:
- [ ] 02-01: TBD during planning

### Phase 3: Photo Management
**Goal**: Users can import, edit, and organize photos before AI processing
**Depends on**: Phase 2
**Requirements**: CAM-02, CAM-03
**Success Criteria** (what must be TRUE):
  1. User can import photos from gallery via system picker
  2. User can crop photos with preview before saving
  3. User can rotate photos 90 degrees left/right
  4. Photos display in carousel without memory leaks on low-end devices
  5. Photo order can be rearranged via drag-and-drop
**Plans**: TBD (5-10 plans)

Plans:
- [ ] 03-01: TBD during planning

### Phase 4: Settings & Preferences
**Goal**: Users can configure app defaults and API credentials securely
**Depends on**: Phase 1
**Requirements**: SET-01, SET-02, SET-03, SET-04
**Success Criteria** (what must be TRUE):
  1. User can enter Gemini API key which persists securely across app restarts
  2. User can set default pickup location (defaults to "Auckland")
  3. User can set default category for new listings
  4. User can toggle dark mode which applies immediately across all screens
  5. Settings validate inputs and show clear error messages for invalid API keys
**Plans**: TBD (5-10 plans)

Plans:
- [ ] 04-01: TBD during planning

### Phase 5: AI Photo Quality Analysis
**Goal**: Users receive actionable AI feedback on photo quality before generating listings
**Depends on**: Phase 2, Phase 4
**Requirements**: CAM-04, CAM-05
**Success Criteria** (what must be TRUE):
  1. User sees quality scores (1-5) for each photo across 5 dimensions (lighting, background, angle, focus, composition)
  2. User receives specific improvement suggestions per photo (e.g., "Move closer to item" not just "Poor angle")
  3. User can retake photos directly from feedback screen without losing other photos
  4. User can override AI recommendations and keep photos marked as low quality
  5. AI processing shows progress indicator and handles rate limits gracefully
**Plans**: TBD (5-10 plans)

Plans:
- [ ] 05-01: TBD during planning

### Phase 6: AI Listing Generation
**Goal**: Users get dual-tone descriptions (TradeMe formal + Facebook casual) auto-generated from photos
**Depends on**: Phase 5
**Requirements**: AI-01, AI-02, AI-03, AI-04
**Success Criteria** (what must be TRUE):
  1. User sees auto-detected item type from photos (e.g., "IKEA Kallax Shelf")
  2. User sees condition assessment with visible defects listed (e.g., "Good - minor scratches on top surface")
  3. User sees TradeMe listing with formal tone and max 80 char title
  4. User sees Facebook listing with casual tone, emoji, and max 60 char title
  5. Both descriptions highlight key features detected from photos (dimensions, material, condition)
**Plans**: TBD (5-10 plans)

Plans:
- [ ] 06-01: TBD during planning

### Phase 7: AI Pricing Suggestions
**Goal**: Users receive price estimates as reference points for manual pricing
**Depends on**: Phase 6
**Requirements**: AI-05
**Success Criteria** (what must be TRUE):
  1. User sees suggested price range based on item type, condition, and NZ market
  2. Price suggestion shows reasoning (e.g., "Based on similar IKEA furniture in Good condition")
  3. User can accept suggestion or enter manual price
  4. Price suggestion updates when user changes condition or item type
  5. Pricing works offline by caching recent market data
**Plans**: TBD (5-10 plans)

Plans:
- [ ] 07-01: TBD during planning

### Phase 8: Listing Editor
**Goal**: Users can edit listings with photo carousel and platform-specific tabs
**Depends on**: Phase 6
**Requirements**: LIST-01, LIST-02, LIST-03, LIST-04, LIST-05, LIST-06
**Success Criteria** (what must be TRUE):
  1. User sees photo carousel at top of editor with swipe navigation
  2. User sees tabbed interface for TradeMe vs Facebook descriptions
  3. User can edit all fields (title, description, price, condition, category, location)
  4. User sees per-field copy-to-clipboard buttons that show "Copied!" feedback
  5. Editor validates inputs (title length, required fields) before allowing save
**Plans**: TBD (5-10 plans)

Plans:
- [ ] 08-01: TBD during planning

### Phase 9: Listing Lifecycle
**Goal**: Users can manage listings through Draft → Listed → Sold workflow
**Depends on**: Phase 8
**Requirements**: LIST-07, LIST-08
**Success Criteria** (what must be TRUE):
  1. User can save listing as Draft without posting
  2. User can mark listing as Listed after posting to platforms
  3. User can mark listing as Sold and see it move to sold section
  4. User can duplicate existing listing to reuse photos and descriptions
  5. User can delete listings with confirmation dialog
**Plans**: TBD (5-10 plans)

Plans:
- [ ] 09-01: TBD during planning

### Phase 10: Dashboard
**Goal**: Users see all listings and stats on home screen with quick access to create new
**Depends on**: Phase 9
**Requirements**: DASH-01, DASH-02, DASH-03
**Success Criteria** (what must be TRUE):
  1. User sees all listings organized by status (Draft/Listed/Sold)
  2. User sees stats bar showing count listed, count sold, total earnings
  3. User can tap FAB to open camera and start new listing
  4. User can tap listing card to view details or edit
  5. User can filter/sort listings by date, price, or status
**Plans**: TBD (5-10 plans)

Plans:
- [ ] 10-01: TBD during planning

### Phase 11: Offline Queue
**Goal**: Users can capture photos and queue AI processing when offline, auto-process when connected
**Depends on**: Phase 5, Phase 6, Phase 7
**Requirements**: OFFQ-01, OFFQ-02 (v2 pulled into comprehensive scope)
**Success Criteria** (what must be TRUE):
  1. User can save photos offline and see "Queued for processing" status
  2. App auto-processes queue when network becomes available
  3. User sees progress notifications for background processing
  4. Queue handles retries with exponential backoff on failures
  5. Queue processing is idempotent (no duplicate listings from retries)
**Plans**: TBD (5-10 plans)

Plans:
- [ ] 11-01: TBD during planning

### Phase 12: Polish & Production
**Goal**: App performs smoothly on low-end devices and provides excellent onboarding UX
**Depends on**: Phase 10, Phase 11
**Requirements**: None (polish)
**Success Criteria** (what must be TRUE):
  1. App launches in under 2 seconds on low-end devices (2GB RAM)
  2. Photo capture and AI processing complete within acceptable time budgets
  3. New users complete onboarding and understand API key setup
  4. App passes device fragmentation testing on Android 11-14
  5. Crash reporting and analytics track production issues
**Plans**: TBD (5-10 plans)

Plans:
- [ ] 12-01: TBD during planning

## Progress

**Execution Order:**
Phases execute in numeric order: 1 → 2 → 3 → 4 → 5 → 6 → 7 → 8 → 9 → 10 → 11 → 12

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. Foundation | 0/TBD | Not started | - |
| 2. Camera Integration | 0/TBD | Not started | - |
| 3. Photo Management | 0/TBD | Not started | - |
| 4. Settings & Preferences | 0/TBD | Not started | - |
| 5. AI Photo Quality Analysis | 0/TBD | Not started | - |
| 6. AI Listing Generation | 0/TBD | Not started | - |
| 7. AI Pricing Suggestions | 0/TBD | Not started | - |
| 8. Listing Editor | 0/TBD | Not started | - |
| 9. Listing Lifecycle | 0/TBD | Not started | - |
| 10. Dashboard | 0/TBD | Not started | - |
| 11. Offline Queue | 0/TBD | Not started | - |
| 12. Polish & Production | 0/TBD | Not started | - |

---
*Roadmap created: 2026-01-31*
*Depth: comprehensive (12 phases)*
*Coverage: 25/25 v1 requirements + 2 v2 requirements pulled into scope*
