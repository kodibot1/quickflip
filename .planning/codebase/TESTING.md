# Testing Patterns

**Analysis Date:** 2026-01-31

## Test Framework

**Status:** Not detected

**Current State:**
- No test framework is configured in the project
- No test files found in the codebase
- No testing scripts in `package.json`
- No jest, vitest, mocha, or other test runner config files
- No test coverage tooling configured

**Implications:**
- All testing is manual or external
- No automated test execution pipeline
- No test coverage reports generated
- Code quality assurance relies on TypeScript strict mode and manual validation

## Code that Would Benefit from Tests

### High Priority Test Candidates

**1. `src/services/claude.ts` - `processCommand()`**
- Location: `src/services/claude.ts` (lines 39-119)
- Why test: Critical business logic for parsing AI responses
- Test scenarios needed:
  - Valid JSON response parsing
  - Missing/malformed JSON response handling
  - API error scenarios
  - Project name matching logic
  - Response validation
- Current protection: Only TypeScript types, no runtime validation beyond try-catch

**2. `src/services/claude.ts` - `generateProjectDescription()`**
- Location: `src/services/claude.ts` (lines 121-167)
- Why test: Handles API calls and null/error cases
- Test scenarios needed:
  - No API key provided
  - No readme or package.json data
  - API call failure
  - Response parsing
  - Text extraction and trimming
- Current protection: Basic null checks, try-catch

**3. `src/store/projects.ts` - Zustand Store**
- Location: `src/store/projects.ts` (lines 34-156)
- Why test: Core state management for entire application
- Test scenarios needed:
  - Project CRUD operations (create, read, update, delete)
  - Focus state management
  - Status/priority updates
  - Note management
  - Settings persistence
  - Async load/save operations
  - Error handling during persistence
- Current protection: TypeScript types, no validation

**4. `electron/scanner.ts` - `scanProjects()`**
- Location: `electron/scanner.ts` (lines 12-57)
- Why test: File system operations, critical for project discovery
- Test scenarios needed:
  - Valid directory scan
  - Non-existent path handling
  - Hidden directory filtering
  - Project detection logic
  - Read error handling
- Current protection: Basic error logging, null check on path

**5. `electron/scanner.ts` - `readProjectFiles()`**
- Location: `electron/scanner.ts` (lines 96-133)
- Why test: File I/O and data extraction
- Test scenarios needed:
  - README file detection (multiple variants)
  - package.json parsing
  - File size truncation (2000 char limit)
  - Missing files handling
  - JSON parse errors
- Current protection: Try-catch, null handling

**6. `src/components/Dashboard.tsx` - Sorting and Filtering**
- Location: `src/components/Dashboard.tsx` (lines 99-126)
- Why test: Complex sorting/filtering logic affects UI correctness
- Test scenarios needed:
  - Priority order consistency
  - Status order consistency
  - Date comparison accuracy
  - Focus project always at top
  - Filter option effectiveness
- Current protection: None (pure logic calculation)

## Testing Recommendations

### Immediate Priority

1. **Add Unit Test Framework**
   - Recommend: Vitest (lightweight, Vite-integrated, fast)
   - Alternative: Jest (more mature ecosystem)
   - Config location: `vitest.config.ts` (new)
   - Package: `npm install -D vitest`

2. **Test Entry Points**
   Start with testing service layer:
   ```typescript
   // Example test structure needed
   describe('claude.processCommand()', () => {
     test('should parse valid JSON response', async () => { ... })
     test('should handle missing JSON in response', async () => { ... })
     test('should handle API errors', async () => { ... })
     test('should match project names flexibly', async () => { ... })
   })
   ```

3. **Zustand Store Testing**
   ```typescript
   describe('useProjectsStore', () => {
     test('should add a new project', () => { ... })
     test('should update project status', () => { ... })
     test('should manage focus state', () => { ... })
     test('should save projects asynchronously', () => { ... })
   })
   ```

4. **File System Operations**
   - Use mock file system for scanner tests
   - Avoid real disk I/O in tests
   - Test error scenarios without actual files

### Test Coverage Gaps

**1. Error Handling:**
- API failures not tested (`claude.ts`)
- File system errors not covered (`scanner.ts`)
- Async operation failures (`projects.ts`)

**2. Edge Cases:**
- Empty project lists
- Missing/null values in APIs
- Large file content truncation
- Invalid date strings
- Malformed JSON responses

**3. Integration Scenarios:**
- Projects loaded, then scanned (duplicate handling)
- Rapid state updates
- Concurrent API calls
- File system permission errors

**4. Component Logic:**
- ProjectCard sorting and filtering
- Task/note toggle operations
- Form validation and submission
- Modal open/close lifecycle

## Current Manual Testing Approach

The codebase relies on:
1. **TypeScript Compilation** - Catches type errors at build time
2. **Manual UI Testing** - Developer runs app and tests features
3. **Try-Catch Blocks** - Runtime error handling with user feedback
4. **Console Logging** - Error visibility in dev tools
5. **Type Validation** - Strong typing prevents many runtime errors

## Mock Requirements (When Tests Added)

**For API Testing:**
- Mock Anthropic SDK client
- Mock successful/failed API responses
- Mock JSON parsing scenarios

**For File System Testing:**
- Mock `fs` module
- Mock directory structures
- Mock read/write operations

**For Electron Testing:**
- Mock `electron` IPC channels
- Mock dialog results
- Mock file system operations

**For Store Testing:**
- Reset store state between tests
- Mock async operations
- Verify persistence calls

## Performance Considerations

**If Tests Added:**
- Test fast: No real API calls, no real file I/O
- Mock all external dependencies
- Test in parallel where possible
- Vitest supports watch mode for development

## Future Test Priorities

1. **Phase 1 (Critical):** Service layer tests (`claude.ts`, `scanner.ts`)
2. **Phase 2 (High):** Store tests (`projects.ts`)
3. **Phase 3 (Medium):** Component logic tests (sorting, filtering)
4. **Phase 4 (Nice to have):** E2E tests with Electron

## Getting Started with Tests

### Minimal Setup Needed

1. Install testing framework:
   ```bash
   npm install -D vitest @vitest/ui
   ```

2. Create test config `vitest.config.ts`:
   ```typescript
   import { defineConfig } from 'vitest/config'
   import react from '@vitejs/plugin-react'

   export default defineConfig({
     plugins: [react()],
     test: {
       environment: 'jsdom',
       globals: true,
     }
   })
   ```

3. Add to `package.json`:
   ```json
   "test": "vitest",
   "test:ui": "vitest --ui"
   ```

4. Create `src/services/__tests__/claude.test.ts` with first tests

### Test File Naming Convention (When Implemented)

Recommended pattern:
- `src/services/__tests__/claude.test.ts`
- `src/store/__tests__/projects.test.ts`
- `electron/__tests__/scanner.test.ts`
- `src/components/__tests__/Dashboard.test.tsx`

Or co-located pattern:
- `src/services/claude.test.ts`
- `src/store/projects.test.ts`

---

*Testing analysis: 2026-01-31*
