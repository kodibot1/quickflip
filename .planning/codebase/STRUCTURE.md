# Codebase Structure

**Analysis Date:** 2026-01-31

## Directory Layout

```
app_planner/
├── src/                    # React renderer code
│   ├── main.tsx            # React entry point
│   ├── App.tsx             # Root component
│   ├── index.css           # Global styles (Tailwind)
│   ├── components/         # React UI components
│   ├── store/              # Zustand state management
│   ├── services/           # Business logic (Claude API)
│   ├── types/              # TypeScript type definitions
│   └── vite-env.d.ts       # Vite env type definitions
├── electron/               # Electron main and preload code
│   ├── main.ts             # Electron main process
│   ├── preload.ts          # IPC bridge to renderer
│   ├── tray.ts             # Menu bar tray management
│   └── scanner.ts          # File system project scanning
├── dist/                   # Built React app (output)
├── dist-electron/          # Built Electron code (output)
├── public/                 # Static assets
├── resources/              # App resources (icons, etc)
├── data/                   # User data directory stub
├── package.json            # Dependencies and scripts
├── vite.config.ts          # Vite build configuration
├── tailwind.config.js      # Tailwind CSS configuration
├── postcss.config.js       # PostCSS configuration
└── electron-builder.yml    # (in package.json) macOS app config
```

## Directory Purposes

**`src/`:**
- Purpose: All React component and state logic
- Contains: TSX/TS components, Zustand store, Claude service
- Key files: `App.tsx`, `main.tsx`, `store/projects.ts`

**`src/components/`:**
- Purpose: Reusable React UI components
- Contains: Dashboard, ProjectCard, CommandInput, FocusBar, Settings
- Key files: `ProjectCard.tsx` (largest, handles project editing)

**`src/store/`:**
- Purpose: Centralized Zustand state store
- Contains: `projects.ts` with ProjectsState interface and all actions
- Key files: `projects.ts` (157 lines, single store)

**`src/services/`:**
- Purpose: External API integrations and business logic
- Contains: Claude API integration for command processing
- Key files: `claude.ts` (processCommand, generateProjectDescription)

**`src/types/`:**
- Purpose: TypeScript type definitions and interfaces
- Contains: Project, AppSettings, CommandResult types
- Key files: `project.ts` (domain models)

**`electron/`:**
- Purpose: Electron main process and IPC bridge code
- Contains: Window management, file I/O, tray menu, filesystem scanning
- Key files: `main.ts` (189 lines), `preload.ts` (IPC interface), `scanner.ts` (filesystem)

**`dist/`:**
- Purpose: Built and bundled React app for production
- Generated: Yes, by `vite build`
- Committed: No
- Contains: index.html, assets with bundled JS/CSS

**`dist-electron/`:**
- Purpose: Built Electron main and preload code
- Generated: Yes, by `vite build` with electron plugin
- Committed: No
- Contains: main.js, preload.js

**`resources/`:**
- Purpose: Application resources (icons)
- Contains: iconTemplate.png for macOS menu bar
- Key files: `icon.icns` (referenced in electron-builder config)

**`data/`:**
- Purpose: Placeholder for local data (not used in current app)
- Contains: Empty or unused
- Note: Actual data stored in system userData via Electron

## Key File Locations

**Entry Points:**

- `src/main.tsx`: React root, creates root element and renders App
- `src/App.tsx`: App root component, initializes store and event listeners
- `electron/main.ts`: Electron main process, creates window and registers IPC handlers
- `vite.config.ts`: Build configuration for Vite and Electron plugins

**Configuration:**

- `package.json`: Dependencies, build scripts, Electron builder config
- `vite.config.ts`: Vite build settings, Electron plugin setup
- `tailwind.config.js`: Tailwind utility setup
- `tsconfig.json`: TypeScript compiler options
- `postcss.config.js`: PostCSS and Tailwind processing

**Core Logic:**

- `src/store/projects.ts`: All state management and persistence logic
- `src/services/claude.ts`: Claude API integration for commands
- `electron/main.ts`: IPC handler implementations (load/save/scan/select)
- `electron/scanner.ts`: Project detection and file reading logic
- `electron/preload.ts`: API bridge definition

**UI Components:**

- `src/App.tsx`: Layout shell with title bar and main regions
- `src/components/Dashboard.tsx`: Project grid, filters, sort, add button
- `src/components/ProjectCard.tsx`: Individual project card with expand/edit
- `src/components/CommandInput.tsx`: Natural language command input
- `src/components/FocusBar.tsx`: Shows currently focused project
- `src/components/Settings.tsx`: Modal for API key and scan path

**Testing:**

- No test files found in codebase

## Naming Conventions

**Files:**

- React components: PascalCase + .tsx (e.g., `ProjectCard.tsx`, `Dashboard.tsx`)
- Utilities/services: camelCase + .ts (e.g., `scanner.ts`, `claude.ts`)
- Store: camelCase + .ts (e.g., `projects.ts`)
- Types: camelCase + .ts (e.g., `project.ts`)

**Directories:**

- Feature directories: lowercase plural (e.g., `components/`, `services/`, `store/`)
- Electron code: single `electron/` directory, not split by function

**Functions:**

- React components: PascalCase (e.g., `function Dashboard()`)
- Regular functions: camelCase (e.g., `scanProjects()`, `processCommand()`)
- Store actions: camelCase (e.g., `addProject()`, `updateStatus()`)
- Event handlers in components: camelCase with `handle` prefix (e.g., `handleAddProject()`, `handleScan()`)

**Variables/Constants:**

- React state: camelCase (e.g., `isLoading`, `showDetails`)
- Constants: UPPER_SNAKE_CASE (e.g., `SYSTEM_PROMPT`)
- Type names: PascalCase (e.g., `Project`, `ProjectStatus`)

**IPC Handlers:**

- Kebab-case with dashes (e.g., `load-projects`, `save-projects`, `scan-projects`)
- Matches kebab-case used in ipcMain.handle() calls

## Where to Add New Code

**New Feature:**

Primary code:
- Component in `src/components/[FeatureName].tsx` if UI-focused
- Service in `src/services/[feature].ts` if API/integration-focused
- Store actions in `src/store/projects.ts` (single store for all state)

Persistence:
- Add new fields to Project interface in `src/types/project.ts`
- Add new store actions in `src/store/projects.ts` that call saveProjects()
- Ensure IPC handlers in `electron/main.ts` save all state

**New Component/Module:**

Implementation:
- Functional React component with TypeScript in `src/components/[Name].tsx`
- Use hooks: `useState`, `useEffect`, `useRef`
- Use store hook: `useProjectsStore()`
- Style with Tailwind utility classes

Integration:
- Import in parent component (usually App.tsx or Dashboard.tsx)
- Subscribe to store changes via useProjectsStore()
- Call store actions on user interaction

**Utilities:**

Shared helpers:
- Pure functions in `src/services/[feature].ts` for domain logic
- No state, no hooks, only functions
- Export as named exports
- Import in components or store actions

API Integration:
- Create new service file in `src/services/[api].ts`
- Handle errors with try-catch and user-friendly messages
- Return typed data matching `src/types/`

**Electron Features:**

Main process additions:
- Add IPC handler in `electron/main.ts` with `ipcMain.handle()`
- Expose in `electron/preload.ts` via contextBridge
- Import and use in components via `window.electronAPI.methodName()`

File operations:
- Use fs and path in `electron/main.ts` or `electron/scanner.ts`
- Wrap with try-catch and console.error on failure
- No UI blocking calls (async preferred)

## Special Directories

**`dist/` and `dist-electron/`:**
- Purpose: Production builds
- Generated: By `npm run build` (vite build + electron-builder)
- Committed: No, in .gitignore
- Note: Regenerated on every build

**`node_modules/`:**
- Purpose: NPM dependencies
- Generated: By `npm install`
- Committed: No, in .gitignore

**`.claude/` and `.planning/`:**
- Purpose: Project metadata and planning documents
- Generated: By development/planning tools
- Committed: Yes, tracked in git

**`release/`:**
- Purpose: Built macOS application bundle
- Generated: By `npm run build` (electron-builder)
- Committed: No, in .gitignore
- Note: Contains signed/notarized app for distribution

---

*Structure analysis: 2026-01-31*
