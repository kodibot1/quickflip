# Requirements: QuickFlip

**Defined:** 2026-01-31
**Core Value:** Photos in, ready-to-post listings out — as fast as possible

## v1 Requirements

### Camera & Photos

- [ ] **CAM-01**: User can capture up to 5 photos per listing via CameraX
- [ ] **CAM-02**: User can import photos from gallery
- [ ] **CAM-03**: User can crop and rotate photos before use
- [ ] **CAM-04**: User can retake or keep each photo after AI feedback
- [ ] **CAM-05**: Gemini scores each photo (lighting, background, angle, focus, composition 1-5) with actionable suggestions

### AI Generation

- [ ] **AI-01**: Gemini auto-detects item type from photos
- [ ] **AI-02**: Gemini assesses condition and detects visible defects
- [ ] **AI-03**: Gemini generates TradeMe listing (formal, professional, max 80 char title)
- [ ] **AI-04**: Gemini generates Facebook listing (casual, emoji OK, max 60 char title)
- [ ] **AI-05**: Gemini suggests price estimate based on item, condition, and NZ market

### Listing Management

- [ ] **LIST-01**: Edit listing with photo carousel and tabbed TradeMe/Facebook descriptions
- [ ] **LIST-02**: Manual price entry (with AI suggestion as reference)
- [ ] **LIST-03**: Condition dropdown (New/Like New/Good/Fair/Poor)
- [ ] **LIST-04**: Category field (auto-detected, user-editable)
- [ ] **LIST-05**: Pickup location (default: Auckland)
- [ ] **LIST-06**: Per-field copy-to-clipboard buttons (title, description, price, condition, location)
- [ ] **LIST-07**: Listing status lifecycle: Draft → Listed → Sold
- [ ] **LIST-08**: Duplicate, edit, delete listings

### Dashboard

- [ ] **DASH-01**: Home screen shows all listings
- [ ] **DASH-02**: Stats bar: X Listed, Y Sold, $Z Total earned
- [ ] **DASH-03**: FAB opens camera to create new listing

### Settings

- [ ] **SET-01**: Default pickup location
- [ ] **SET-02**: Default category
- [ ] **SET-03**: Gemini API key field
- [ ] **SET-04**: Dark mode toggle

## v2 Requirements

### Offline & Queue

- **OFFQ-01**: Queue photos for AI processing when offline
- **OFFQ-02**: Auto-process queue when network available via WorkManager

### Enhanced Pricing

- **PRICE-01**: eBay API sold listing lookup for comparable items
- **PRICE-02**: TradeMe closed listing price comparison

### Power User

- **PWR-01**: Listing templates for repeat item types
- **PWR-02**: Cloud backup/sync across devices

## Out of Scope

| Feature | Reason |
|---------|--------|
| Direct cross-posting to TradeMe/Facebook APIs | API complexity, ToS risks, clipboard is sufficient |
| Full inventory management | Scope creep, not needed for casual reselling |
| Multi-user / team features | Personal tool for two people |
| Payment processing | Platforms handle payments, liability risk |
| Shipping integration | Most sales are local pickup, platforms handle shipping |
| Social media marketing | Marginal value, scope creep |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| CAM-01 | — | Pending |
| CAM-02 | — | Pending |
| CAM-03 | — | Pending |
| CAM-04 | — | Pending |
| CAM-05 | — | Pending |
| AI-01 | — | Pending |
| AI-02 | — | Pending |
| AI-03 | — | Pending |
| AI-04 | — | Pending |
| AI-05 | — | Pending |
| LIST-01 | — | Pending |
| LIST-02 | — | Pending |
| LIST-03 | — | Pending |
| LIST-04 | — | Pending |
| LIST-05 | — | Pending |
| LIST-06 | — | Pending |
| LIST-07 | — | Pending |
| LIST-08 | — | Pending |
| DASH-01 | — | Pending |
| DASH-02 | — | Pending |
| DASH-03 | — | Pending |
| SET-01 | — | Pending |
| SET-02 | — | Pending |
| SET-03 | — | Pending |
| SET-04 | — | Pending |

**Coverage:**
- v1 requirements: 25 total
- Mapped to phases: 0
- Unmapped: 25

---
*Requirements defined: 2026-01-31*
*Last updated: 2026-01-31 after initial definition*
