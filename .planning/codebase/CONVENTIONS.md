# Coding Conventions

**Analysis Date:** 2026-01-31

## Naming Patterns

**Files:**
- Component files: PascalCase (e.g., `ProjectCard.tsx`, `Dashboard.tsx`, `CommandInput.tsx`)
- Service/utility files: camelCase (e.g., `claude.ts`, `scanner.ts`, `projects.ts`)
- Type definition files: snake_case with `.ts` extension (e.g., `project.ts`)
- Test/spec files: Not present in codebase

**Functions:**
- Component functions: PascalCase (e.g., `function ProjectCard()`, `function Dashboard()`)
- Export default components: PascalCase naming
- Regular functions: camelCase (e.g., `loadProjectsData()`, `scanProjects()`, `handleAddProject()`)
- Event handlers: camelCase with `handle` prefix (e.g., `handleSaveNextStep()`, `handleAddNote()`, `handleScan()`)
- Async functions: camelCase, clearly indicating async operations (e.g., `processCommand()`, `generateProjectDescription()`)

**Variables:**
- State variables: camelCase (e.g., `showDetails`, `isProcessing`, `newNote`, `filteredProjects`)
- Type-safe state: camelCase (e.g., `sortBy`, `filterBy`)
- Constants: camelCase in context, but globally scoped constants use UPPER_SNAKE_CASE where appropriate
- Component props: camelCase (e.g., `isOpen`, `onClose`)
- Local variables in functions: camelCase (e.g., `focusProject`, `priorityOrder`, `diffDays`)

**Types:**
- Interface names: PascalCase (e.g., `ProjectCardProps`, `ProjectsState`, `ProjectStatus`)
- Type unions: PascalCase for each option, pipe-separated (e.g., `'active' | 'paused' | 'done'`)
- Record types: PascalCase for the first parameter (e.g., `Record<ProjectStatus, string>`)

## Code Style

**Formatting:**
- No explicit formatter configured (not ESLint or Prettier in project)
- Indentation: 2 spaces (observed in all files)
- Line length: Reasonable wrapping around 80-100 characters
- Quote style: Single quotes for strings (e.g., `'active'`, `'project'`)
- Semicolons: Used consistently at end of statements
- Trailing commas: Used in multi-line objects and arrays

**Linting:**
- TypeScript strict mode enabled: `"strict": true` in `tsconfig.json`
- Unused locals detection: `"noUnusedLocals": true`
- Unused parameters detection: `"noUnusedParameters": true`
- Switch case exhaustiveness: `"noFallthroughCasesInSwitch": true`
- Additional rules: `skipLibCheck: true`, `isolatedModules: true`, `noEmit: true`

## Import Organization

**Order:**
1. React and external library imports (e.g., `import { useState, useEffect } from 'react'`)
2. Local imports from store (e.g., `import { useProjectsStore } from '../store/projects'`)
3. Local imports from components (e.g., `import ProjectCard from './ProjectCard'`)
4. Local imports from services (e.g., `import { processCommand } from '../services/claude'`)
5. Local imports from types (e.g., `import { Project, ProjectStatus } from '../types/project'`)

**Path Aliases:**
- `@/*` resolves to `src/*` (configured in `tsconfig.json`)
- Not heavily used in observed code, preferring relative imports

**Import Style:**
- Named imports for specific exports (e.g., `import { create } from 'zustand'`)
- Default imports for components (e.g., `import Dashboard from './components/Dashboard'`)
- Mixed imports when needed (e.g., `import { useState, useRef, useEffect } from 'react'`)

## Error Handling

**Patterns:**
- Try-catch blocks for async operations (e.g., in `processCommand()`, `generateProjectDescription()`)
- Null checks before operations (e.g., `if (!apiKey)`, `if (!result)`)
- Graceful fallbacks returning empty values or default states
- Console.error for logging errors (e.g., `console.error('Error scanning projects:', error)`)
- User-facing error messages in feedback/alert format
- Error messages include context (e.g., 'API Error: ${errorMessage}')
- Type guards for data validation (e.g., checking if `textContent.type === 'text'` before accessing text)

**Example Patterns:**
```typescript
// Pattern from claude.ts
try {
  const response = await client.messages.create({...});
  const textContent = response.content.find((c) => c.type === 'text');
  if (!textContent || textContent.type !== 'text') {
    return { success: false, message: 'Failed to get response from Claude' };
  }
  // Continue processing
} catch (error: any) {
  console.error('Claude API error:', error);
  const errorMessage = error?.message || error?.error?.message || 'Unknown error';
  return { success: false, message: `API Error: ${errorMessage}` };
}
```

## Logging

**Framework:** Native `console` object (no external logging library)

**Patterns:**
- `console.error()` for errors and failures
- `console.log()` for informational messages
- Logged in error handlers and catch blocks
- Error context included in log messages (e.g., `console.error('Error scanning projects:', error)`)
- Not heavily used for debug logging in production code

**Examples:**
- `console.error('Error loading projects:', error)` in `main.ts`
- `console.error('Error scanning projects:', error)` in `Dashboard.tsx`
- `console.log('Running in browser mode - Electron APIs not available')` in `App.tsx`

## Comments

**When to Comment:**
- Explain system prompts and AI instructions (e.g., `SYSTEM_PROMPT` in `claude.ts` is well-commented)
- Document complex business logic (e.g., task parsing logic in `ProjectCard.tsx`)
- Explain environment-specific behavior (e.g., Electron vs browser checks)
- Mark UI regions (e.g., `{/* Header */}`, `{/* Main content */}`)
- Indicate intentional limitations or workarounds

**JSDoc/TSDoc:**
- Minimal usage observed
- Interface definitions include type hints but no JSDoc comments
- Function signatures rely on TypeScript types for documentation

**Inline Comments:**
- Used sparingly for clarification
- Comments for non-obvious logic (e.g., how Zustand store works)
- Region markers for large component sections (e.g., `{/* Action buttons */}`)

## Function Design

**Size:**
- Most functions are concise (under 50 lines)
- Component functions range from 10-100 lines
- Logic-heavy functions kept under 30 lines
- Long components with multiple sections use regional comments

**Parameters:**
- Single object parameter for components (e.g., `ProjectCardProps`)
- Explicit typed parameters for utility functions
- Props interface defined for all components
- Destructuring used in function signatures (e.g., `({ project }: ProjectCardProps)`)

**Return Values:**
- React components return JSX elements
- Utility functions return typed objects (e.g., `CommandResult`, `ScannedProject[]`)
- Async functions return Promises
- Early returns for validation/guard clauses
- Consistent return types across service functions

**Example Function Pattern:**
```typescript
// From projects.ts - Zustand action
updateProject: (id, updates) => {
  set((state) => ({
    projects: state.projects.map((p) =>
      p.id === id ? { ...p, ...updates, lastWorkedOn: new Date().toISOString() } : p
    ),
  }));
  get().saveProjects();
},
```

## Module Design

**Exports:**
- Named exports for utility functions (e.g., `export async function processCommand()`)
- Default exports for React components (e.g., `export default function Dashboard()`)
- Type exports for TypeScript definitions (e.g., `export type ProjectStatus`)
- Interface exports for component props

**Barrel Files:**
- Not used in the codebase
- Direct imports from specific files

**Example Module Patterns:**
```typescript
// Service modules (claude.ts)
export async function processCommand(...): Promise<CommandResult> {...}
export async function generateProjectDescription(...): Promise<string> {...}

// Store modules (projects.ts)
export const useProjectsStore = create<ProjectsState>(...)

// Type modules (project.ts)
export type ProjectStatus = 'active' | 'paused' | 'done'
export interface Project { ... }
```

## React-Specific Conventions

**Hooks Usage:**
- `useState()` for local component state
- `useEffect()` for side effects (event listeners, initialization)
- Custom hook pattern: `useProjectsStore()` from Zustand
- Hooks cleanup in return statements

**Component Pattern:**
- Function components only (no class components)
- Props destructuring in function parameters
- Early returns for conditional rendering
- Consistent return type (always JSX.Element)

**State Management:**
- Zustand for global state (`useProjectsStore`)
- Local state with `useState` for UI-specific values (modals, inputs)
- Separation of concerns: store handles data, components handle UI

**Example Component Structure:**
```typescript
interface ComponentProps {
  project: Project;
}

export default function Component({ project }: ComponentProps) {
  const [showDetails, setShowDetails] = useState(false);
  const { updateProject } = useProjectsStore();

  useEffect(() => {
    // Setup
    return () => {
      // Cleanup
    };
  }, [dependencies]);

  const handleAction = () => { /* ... */ };

  return (
    <div>
      {/* JSX */}
    </div>
  );
}
```

---

*Convention analysis: 2026-01-31*
