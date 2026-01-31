# Codebase Concerns

**Analysis Date:** 2026-01-31

## Security Considerations

**API Key Exposure:**
- Risk: Claude API key is stored in plaintext in local JSON file and passed directly through IPC channels
- Files: `src/components/Settings.tsx`, `src/store/projects.ts`, `electron/main.ts`
- Current mitigation: None (key displayed as password input type only in UI)
- Recommendations: Use Electron's safeStorage API for encrypting sensitive data at rest. Consider using environment variables for development and secure credential storage for production builds.

**Insecure IPC Communication:**
- Risk: Preload exposes multiple IPC handlers without validation. `saveProjects` accepts arbitrary data without schema validation
- Files: `electron/preload.ts`, `electron/main.ts` (lines 95-101)
- Current mitigation: contextIsolation enabled, nodeIntegration disabled
- Recommendations: Add input validation and type checking in all IPC handlers. Implement whitelist-based data validation before accepting data from renderer process.

**File System Access Without Restrictions:**
- Risk: `selectFolder()` and `scanProjects()` can access any directory on user's system with no path restrictions
- Files: `electron/main.ts` (lines 103-106, 123-141), `electron/scanner.ts`
- Current mitigation: Dialog shows directory selector, but no validation of selected paths
- Recommendations: Implement path whitelist validation. Restrict scanning to user-safe directories (Desktop, Documents, Downloads). Validate paths before file operations.

**Unhandled Error States in Claude Integration:**
- Risk: API errors are logged but not properly handled in all cases. Sensitive data from API errors may be logged
- Files: `src/services/claude.ts` (lines 111-118), `src/components/CommandInput.tsx` (lines 92-96)
- Current mitigation: Basic error logging with console.error
- Recommendations: Implement error telemetry with sensitive data scrubbing. Never log full API responses or raw error details.

## Tech Debt

**Regex-Based JSON Parsing:**
- Issue: Claude response is parsed using regex instead of proper error-aware JSON parsing
- Files: `src/services/claude.ts` (line 85)
- Impact: Fragile parsing. If Claude returns malformed JSON or wraps response in extra text, parsing fails silently
- Fix approach: Implement robust JSON extraction with fallback strategies. Add stricter validation of response format.

**Hardcoded Model Version:**
- Issue: Claude model ID is hardcoded as `claude-3-haiku-20240307` in two places
- Files: `src/services/claude.ts` (lines 67, 148)
- Impact: Outdated model. Haiku may be deprecated. No way to update without code changes
- Fix approach: Extract model ID to configurable constant or settings. Support multiple model selection in UI.

**Untyped Electron IPC Data:**
- Issue: IPC handlers accept `any` types without proper TypeScript typing
- Files: `electron/main.ts` (line 66-76, 78-88), `electron/preload.ts` (line 5)
- Impact: Runtime errors possible. No type safety across process boundary
- Fix approach: Define strict IPC message interfaces. Use TypeScript to enforce typing on both main and preload processes.

**Missing Error Handling in Store Operations:**
- Issue: `saveProjects()` in Zustand store silently catches errors and sets error state, but callers don't consistently check error state
- Files: `src/store/projects.ts` (lines 148-155)
- Impact: Failed saves may go unnoticed. User operations may appear successful when they failed
- Fix approach: Implement consistent error checking. Add user feedback for all save operations.

**Manual Date String Management:**
- Issue: Dates stored as ISO strings throughout codebase. No centralized date handling utility
- Files: `src/store/projects.ts` (multiple lines), `src/components/ProjectCard.tsx` (line 81)
- Impact: Brittle date comparisons. Risk of timezone issues. Hard to maintain
- Fix approach: Create date utility module. Use consistent date library (e.g., date-fns) for all date operations.

## Performance Bottlenecks

**Unoptimized Project Scanning:**
- Problem: `scanProjects()` recursively checks directories without limiting depth or respecting `.gitignore`
- Files: `electron/scanner.ts` (lines 59-94)
- Cause: Deep directory traversal on every scan. No caching of scan results
- Improvement path: Implement depth limiting (max 2 levels). Respect `.gitignore` files. Cache scan results with timestamp-based invalidation.

**Array Operations in Store Sorting/Filtering:**
- Problem: Dashboard spreads and sorts entire project array on every render
- Files: `src/components/Dashboard.tsx` (lines 100-120)
- Cause: No memoization. New array created on every render even if projects unchanged
- Improvement path: Use `useMemo` for sorted/filtered lists. Consider pagination for large project counts.

**Full Project List Serialization:**
- Problem: Every store update serializes entire project array to JSON file
- Files: `src/store/projects.ts` (lines 43-44, 53, 60-61, 71, 87, 96, 111, 120-127)
- Cause: Zustand pattern calls `saveProjects()` after every single update
- Improvement path: Batch updates. Implement debounced saves (500ms). Consider database for large datasets.

## Fragile Areas

**Claude AI Prompt Dependency:**
- Files: `src/services/claude.ts` (lines 4-37)
- Why fragile: Entire command parsing relies on Claude's interpretation of natural language. Small prompt changes may break parsing. No validation of response structure.
- Safe modification: Test extensively with various command formats before changing prompt. Add response validation layer.
- Test coverage: No tests exist for command parsing

**JSON File-Based Persistence:**
- Files: `electron/main.ts` (lines 66-88)
- Why fragile: No transaction support, no backup, no validation. Concurrent writes would corrupt file
- Safe modification: Implement write-to-temp-then-rename pattern. Add data validation on load
- Test coverage: No tests for save/load cycle

**Project Name Matching Logic:**
- Files: `src/services/claude.ts` (lines 94-102)
- Why fragile: Uses string includes/lowercase matching. Could match wrong project. No score-based ranking
- Safe modification: Use fuzzy string matching library. Add confidence threshold. Log matches for debugging
- Test coverage: No tests for project matching

**Tray Icon Generation:**
- Files: `electron/tray.ts` (lines 11-54)
- Why fragile: Attempts to create icon from multiple fallback sources. Base64 image hardcoded. SVG parsing untested
- Safe modification: Use static icon file. Remove fallback complexity. Test on both Intel and Apple Silicon
- Test coverage: No tests

## Missing Critical Features

**No Data Backup:**
- Problem: Single projects.json file with no backup or recovery mechanism
- Blocks: Data loss recovery, cloud sync, multi-device support

**No Project Duplication/Template System:**
- Problem: Users must create projects manually each time
- Blocks: Faster project setup, standardized project structures

**No Conflict Resolution:**
- Problem: No handling for duplicate project names or path conflicts during scanning
- Blocks: Reliable auto-scan feature, bulk import

## Test Coverage Gaps

**No Unit Tests:**
- What's not tested: All service functions, store mutations, component logic
- Files: `src/services/claude.ts`, `src/store/projects.ts`, `electron/scanner.ts`
- Risk: Regression bugs undetected. Refactoring dangerous
- Priority: High

**No Integration Tests:**
- What's not tested: IPC communication, file I/O, Claude API integration
- Files: `electron/main.ts`, `src/services/claude.ts`
- Risk: Process boundary bugs and API failures undetected
- Priority: High

**No E2E Tests:**
- What's not tested: Full workflow (scan → create → modify → save)
- Risk: User-facing bugs in main features
- Priority: Medium

**No Component Tests:**
- What's not tested: React component rendering, state management, user interactions
- Files: `src/components/`, `src/App.tsx`
- Risk: UI bugs in core features
- Priority: Medium

## Dependencies at Risk

**Claude SDK Version:**
- Risk: SDK version pinned to `^0.24.0` (18 months old as of analysis date). May be deprecated
- Impact: Security updates unavailable, new features inaccessible
- Migration plan: Regularly audit SDK version. Implement model version configuration to support multiple API generations

**Electron Version:**
- Risk: `^28.0.0` is not latest. Missing security patches for older version
- Impact: Security vulnerabilities in Electron core
- Migration plan: Establish upgrade schedule. Test thoroughly on major version bumps

**React/TypeScript Versions:**
- Risk: Versions are relatively current but will eventually lag
- Impact: Missing modern React features, performance improvements
- Migration plan: Quarterly dependency audit

## Scaling Limits

**Single-File JSON Storage:**
- Current capacity: ~1MB file (roughly 100-200 projects with notes)
- Limit: File I/O becomes slow, parsing takes longer, disk space inefficient
- Scaling path: Migrate to SQLite database for projects. Keep JSON format for simple settings

**In-Memory Project Array:**
- Current capacity: ~1000 projects before noticeable UI lag
- Limit: Sorting/filtering becomes slow, component renders sluggish
- Scaling path: Implement pagination (20-50 projects per page). Add search/filter optimization

## Known Issues

**Missing API Key Validation:**
- Symptoms: Setting invalid API key doesn't show error until command is run
- Files: `src/components/Settings.tsx`, `src/store/projects.ts`
- Trigger: User pastes invalid API key and saves settings
- Workaround: Check console for error after first command fails

**Scan Path Expansion Limitation:**
- Symptoms: `~` expansion only works for absolute paths like `/Users/username/Desktop`
- Files: `electron/main.ts` (line 104)
- Trigger: Using tilde path like `~/Projects` in settings
- Workaround: Use full absolute path instead

**Duplicate Project Handling:**
- Symptoms: Path-based duplicate detection only works if exact same path. Similar paths create duplicates
- Files: `src/components/Dashboard.tsx` (lines 28, 66)
- Trigger: Project moved or re-added with slightly different path
- Workaround: Manually delete duplicates

**DevTools Always Open in Dev Mode:**
- Symptoms: DevTools can't be hidden during development
- Files: `electron/main.ts` (line 45)
- Trigger: Running `npm run dev`
- Workaround: Comment out `openDevTools()` line before testing UI

---

*Concerns audit: 2026-01-31*
