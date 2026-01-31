# Technology Stack

**Analysis Date:** 2026-01-31

## Languages

**Primary:**
- TypeScript 5.3.0 - Used for all source code (React components, Electron main/preload, services)
- JSX/TSX - React component syntax

**Secondary:**
- JavaScript - Configuration files (vite.config.ts, postcss.config.js, tailwind.config.js)
- HTML - Entry point `src/index.html`
- CSS - Via Tailwind CSS utility classes

## Runtime

**Environment:**
- Node.js (version specified via `@types/node 20.10.0`)

**Package Manager:**
- npm
- Lockfile: `package-lock.json` (present)

## Frameworks

**Core:**
- React 18.2.0 - UI framework
- Electron 28.0.0 - Desktop application framework for macOS
- Vite 5.0.0 - Development server and build tool

**Testing:**
- Not configured in this project

**Build/Dev:**
- vite-plugin-electron 0.28.0 - Electron integration with Vite
- vite-plugin-electron-renderer 0.14.5 - Renderer process plugin
- electron-builder 24.9.1 - Package and build Electron app (DMG/ZIP for macOS)
- @vitejs/plugin-react 4.2.0 - React plugin for Vite

**Styling:**
- Tailwind CSS 3.4.0 - Utility-first CSS framework
- PostCSS 8.4.32 - CSS transformations
- autoprefixer 10.4.16 - Vendor prefix automation

## Key Dependencies

**Critical:**
- @anthropic-ai/sdk 0.24.0 - Official Anthropic API client for Claude integration
  - Used in `src/services/claude.ts` for LLM command processing
  - Handles natural language project command interpretation
  - Browser-compatible via `dangerouslyAllowBrowser: true`

**State Management:**
- zustand 4.5.0 - Lightweight state management for projects and settings
  - Persistent state store in `src/store/projects.ts`
  - Handles IPC-based persistence via Electron

**Utilities:**
- uuid 9.0.1 - Generate unique project IDs
- @types/react 18.2.0 - React type definitions
- @types/react-dom 18.2.0 - React DOM type definitions
- @types/uuid 9.0.7 - UUID type definitions
- @types/node 20.10.0 - Node.js type definitions

**Development:**
- typescript 5.3.0 - TypeScript compiler and language support
- concurrently 8.2.2 - Run multiple dev tasks (dev server + Electron)
- wait-on 7.2.0 - Wait for dev server before starting Electron

## Configuration

**Environment:**
- No `.env` file required - API key stored in application settings
- Claude API key configured via Settings UI: `src/components/Settings.tsx`
- Stored in Zustand store with Electron persistence
- Passed to Claude SDK at runtime via `dangerouslyAllowBrowser` mode

**Build:**
- `vite.config.ts` - Vite configuration with Electron and React plugins
- `tsconfig.json` - TypeScript compiler options
  - Target: ES2020
  - Module: ESNext
  - JSX: react-jsx
  - Path alias: `@/*` → `src/*`
- `tailwind.config.js` - Custom color theme for project status/priority
- `postcss.config.js` - PostCSS with Tailwind and autoprefixer
- `electron-builder` config in `package.json` - DMG and ZIP output for macOS

## Platform Requirements

**Development:**
- Node.js 18+ (inferred from dependencies)
- macOS (for Electron development with native title bar styling)
- Anthropic API key (for Claude integration)

**Production:**
- macOS 10.10+ (Electron minimum requirement)
- Deployment: DMG and ZIP packages via electron-builder
- App ID: `com.projecttracker.app`
- Product name: `Project Tracker`
- Category: Productivity (public.app-category.productivity)

## Build Output

**Development:**
- Vite dev server: Port 5173 (strict)
- Electron main process from `electron/main.ts`
- Renderer from React components in `src/`

**Production:**
- `dist/` - Bundled React app
- `dist-electron/` - Compiled Electron main and preload
- `release/` - Final DMG/ZIP builds

---

*Stack analysis: 2026-01-31*
