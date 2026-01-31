# Architecture

**Analysis Date:** 2026-01-31

## Pattern Overview

**Overall:** Electron + React desktop app with client-side state management and Electron IPC bridge

**Key Characteristics:**
- Two-process architecture: Main process (Electron) and Renderer process (React)
- Centralized state management via Zustand store
- IPC bridge for secure communication between processes
- File-system persistence for project data
- Claude API integration for natural language command processing
- Tray/menu bar integration for quick access

## Layers

**Presentation Layer (React Components):**
- Purpose: Render UI and collect user input
- Location: `src/components/` and `src/App.tsx`
- Contains: React functional components with Tailwind styling
- Depends on: Zustand store for state, Claude service for command processing
- Used by: Electron renderer process

**State Management Layer (Zustand):**
- Purpose: Centralize project state and provide actions
- Location: `src/store/projects.ts`
- Contains: Project list, settings, loading state, and all state mutations
- Depends on: IPC bridge (electronAPI) for persistence
- Used by: All React components

**Service Layer:**
- Purpose: Handle external integrations and business logic
- Location: `src/services/claude.ts`
- Contains: Command processing via Claude API, project description generation
- Depends on: Anthropic SDK, project types
- Used by: CommandInput component via processCommand function

**Type System:**
- Purpose: Define domain models and API contracts
- Location: `src/types/project.ts`
- Contains: Project, ProjectStatus, ProjectPriority, AppSettings, CommandResult types
- Depends on: Nothing (foundational)
- Used by: All layers

**Electron Main Process:**
- Purpose: Manage application lifecycle and file I/O
- Location: `electron/main.ts`
- Contains: Window management, IPC handlers, data persistence
- Depends on: Node.js fs, path, Electron APIs
- Used by: Tray and preload modules

**Electron Preload Bridge:**
- Purpose: Expose safe IPC methods to renderer
- Location: `electron/preload.ts`
- Contains: contextBridge setup, electronAPI interface definition
- Depends on: Electron contextBridge and ipcRenderer
- Used by: React components via window.electronAPI

**File System Scanner:**
- Purpose: Detect projects in filesystem
- Location: `electron/scanner.ts`
- Contains: scanProjects function, readProjectFiles function, project detection logic
- Depends on: Node.js fs and path
- Used by: Main process IPC handlers

**Tray/Menu Bar:**
- Purpose: Provide quick access to focus and projects
- Location: `electron/tray.ts`
- Contains: Tray icon management, context menu building, tray menu updates
- Depends on: Electron Tray, Menu, app module
- Used by: Main process on app ready

## Data Flow

**User Creates/Manages Project:**

1. User clicks "Add Project" button or "Scan for Projects" in Dashboard
2. Dashboard calls `window.electronAPI.selectFolder()` or `window.electronAPI.scanProjects()`
3. Main process handles IPC call, returns folder data or scanned projects
4. Dashboard calls `window.electronAPI.readProjectFiles()` to get metadata
5. Dashboard creates Project object and calls `addProject()` store action
6. Store persists to file via `window.electronAPI.saveProjects()`
7. Main process receives save, updates tray menu
8. ProjectCard components re-render with new project

**User Issues Natural Language Command:**

1. User types in CommandInput and presses Enter
2. CommandInput calls `processCommand()` with input and projects
3. Claude service calls Anthropic API with project list and command
4. API returns structured JSON response with action and target project
5. CommandInput applies action: `addNote()`, `setFocus()`, `updateStatus()`, etc.
6. Store actions mutate state and call `saveProjects()`
7. Main process receives save, updates tray menu with focused project
8. UI updates reflect changes (focus bar highlights, card reorders)

**User Focuses on Project:**

1. User clicks "Focus" button on ProjectCard or via tray menu
2. CommandInput or tray calls `setFocus(projectId)` action
3. Store sets isFocus=true for target, false for others
4. Store calls `saveProjects()` to persist
5. Main process receives update and calls `updateTrayMenu()` with focused project
6. FocusBar component displays focused project prominently
7. Dashboard re-sorts cards with focused project first

**Application Startup:**

1. Electron createWindow() loads React app in BrowserWindow
2. App.tsx useEffect calls `loadProjects()` via store
3. Store invokes `window.electronAPI.loadProjects()`
4. Main process reads projects.json from userData directory
5. Store sets projects and settings state
6. Dashboard renders projects, CommandInput and FocusBar appear
7. Main process createTray() sets up menu bar access

## Key Abstractions

**Project Entity:**
- Purpose: Represents a tracked software project
- Examples: `src/types/project.ts` defines Project interface
- Pattern: Plain TypeScript object with id, name, path, metadata, status fields
- Lifecycle: Created by user, persisted in projects.json, loaded on startup

**Store Actions:**
- Purpose: Define all state mutations as typed functions
- Examples: `addProject`, `updateStatus`, `setFocus`, `addNote` in `src/store/projects.ts`
- Pattern: Each action triggers `saveProjects()` to persist changes
- Benefits: Single source of truth for state changes, automatic persistence

**IPC Handler:**
- Purpose: Bridge between React component and Electron main process
- Examples: IPC handlers in `electron/main.ts` for load-projects, save-projects, scan-projects
- Pattern: Main process registers handlers with `ipcMain.handle()`, renderer calls via `electronAPI`
- Benefits: Secure context isolation, explicit communication contract

**Claude Command System:**
- Purpose: Parse natural language into project mutations
- Examples: "add note to weather app" → addNote action
- Pattern: System prompt defines available actions, API returns structured JSON response
- Benefits: Natural interaction without learning command syntax

## Entry Points

**Renderer Entry:**
- Location: `src/main.tsx`
- Triggers: React app initialization on window load
- Responsibilities: Mount React app to DOM, initialize root component

**Main Process Entry:**
- Location: `electron/main.ts`
- Triggers: Electron app ready event
- Responsibilities: Create main window, set up IPC handlers, create tray, manage app lifecycle

**Component Entry:**
- Location: `src/App.tsx`
- Triggers: Rendered by main.tsx
- Responsibilities: Set up Electron listeners, initialize store, render layout components

**Store Entry:**
- Location: `src/store/projects.ts`
- Triggers: First component that calls `useProjectsStore()`
- Responsibilities: Create Zustand store, register state and actions, manage persistence

## Error Handling

**Strategy:** Graceful degradation with user feedback

**Patterns:**
- Try-catch in async operations (processCommand, loadProjects, saveProjects)
- Error state in store (error field) displayed as toast notifications
- Console logging for debugging without blocking UI
- Fallback defaults (empty arrays, false flags) on file read errors
- User-friendly error messages in CommandInput feedback

**Examples:**
- `src/services/claude.ts` returns success: false with message on API failure
- `src/store/projects.ts` catches file I/O errors and sets error state
- `src/components/CommandInput.tsx` displays error feedback for 3 seconds
- Main process catches fs errors but continues operation

## Cross-Cutting Concerns

**Logging:**
- No centralized logging framework
- Console.log/console.error scattered throughout for debugging
- Main process logs file I/O errors, IPC issues, scanning errors
- Service layer logs API errors

**Validation:**
- CommandInput validates input.trim() before submission
- Main process checks file/folder existence before operations
- Scanner checks file extensions to identify projects
- Store actions expect properly typed Project objects from caller

**Authentication:**
- Claude API key stored in app settings in projects.json
- User enters API key in Settings component as password field
- API key passed directly to Claude service on each command
- No token refresh or expiry handling (stateless API calls)

**Persistence:**
- All state changes trigger saveProjects() automatically
- Data stored in app.getPath('userData')/projects.json
- No database, no migrations, no schema versioning
- Full file overwrite on every save (no transaction safety)

---

*Architecture analysis: 2026-01-31*
