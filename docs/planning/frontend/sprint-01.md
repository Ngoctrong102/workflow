# Sprint 01: Project Setup & Infrastructure

## Goal
Initialize React project with TypeScript and core dependencies, ensuring compliance with `docs/technical/frontend/overview.md` and design system.

## Phase
Foundation

## Complexity
Simple

## Dependencies
None

## Compliance Check

### Before Starting
1. ✅ Read `@import(technical/frontend/overview.md)` - Understand frontend architecture
2. ✅ Read `@import(technical/frontend/implementation-guide.md)` - Understand implementation guide
3. ✅ Read `@import(technical/frontend/design-system.json)` - Understand design system
4. ✅ Check existing project setup for violations
5. ✅ Fix any violations immediately before proceeding

### Existing Code Verification
- [ ] Verify project structure matches specification
- [ ] Verify dependencies match requirements
- [ ] Verify TypeScript configuration is correct
- [ ] Verify Tailwind CSS is configured correctly
- [ ] Verify Shadcn/ui is initialized correctly
- [ ] Fix any violations found

## Tasks

### Project Setup
- [ ] Verify React app with TypeScript exists (Vite or CRA)
- [ ] Verify `package.json` with dependencies
- [ ] Verify core dependencies are installed:
  - `react`, `react-dom`
  - `react-router-dom`
  - `axios` or `fetch` wrapper
  - `react-hook-form`
  - State management library (`zustand` or `@reduxjs/toolkit`)
  - `@tanstack/react-query` or `swr` (for API state)
  - `tailwindcss`
  - `@radix-ui/react-*` (via Shadcn/ui)
  - `lucide-react` (icons)
- [ ] Verify project directory structure matches specification:
  ```
  frontend/src/
  ├── components/
  ├── pages/
  ├── hooks/
  ├── services/
  ├── store/
  ├── utils/
  ├── types/
  ├── constants/
  ├── lib/
  ├── providers/
  ├── router/
  └── App.tsx
  ```

### UI Library Setup
- [ ] Verify Tailwind CSS is installed and configured
- [ ] Verify `tailwind.config.js` exists with design system configuration
- [ ] Verify Shadcn/ui is initialized
- [ ] Verify base Shadcn/ui components are installed:
  - Button
  - Input
  - Card
  - Dialog
  - Select
  - Table
  - Toast
- [ ] Verify component paths are configured (`components.json`)

### Build Configuration
- [ ] Verify build tool configuration (Vite/CRA)
- [ ] Verify environment variables setup (`.env`, `.env.example`)
- [ ] Verify path aliases configured (`@/components`, `@/utils`, etc.)
- [ ] Verify TypeScript configuration (`tsconfig.json`)
- [ ] Verify ESLint configuration
- [ ] Verify Prettier configuration (optional)

### Design System Integration
- [ ] Verify design system colors are configured in Tailwind
- [ ] Verify typography system is configured
- [ ] Verify spacing scale is configured
- [ ] Read `@import(technical/frontend/design-system.json)` and apply

## Deliverables

- ✅ Working React application that starts successfully
- ✅ Tailwind CSS configured with design system
- ✅ Shadcn/ui initialized with base components
- ✅ Basic project structure in place and compliant
- ✅ TypeScript configured correctly
- ✅ Build configuration working

## Technical Details

### Project Structure (MUST MATCH)
```
frontend/
├── src/
│   ├── components/
│   │   ├── ui/          # Shadcn/ui components
│   │   └── ...          # Custom components
│   ├── pages/           # Page components
│   ├── hooks/           # Custom React hooks
│   ├── services/        # API services
│   ├── store/           # State management
│   ├── utils/           # Utility functions
│   ├── types/           # TypeScript types
│   ├── constants/       # Constants
│   ├── lib/             # Library configurations
│   ├── providers/       # Context providers
│   ├── router/          # Routing configuration
│   ├── App.tsx
│   └── main.tsx
├── public/
├── package.json
├── tsconfig.json
├── tailwind.config.js
└── vite.config.ts (or similar)
```

### Dependencies Required
- React 18+
- TypeScript 5+
- React Router DOM
- Axios (or fetch wrapper)
- React Hook Form
- Zustand (or Redux Toolkit)
- TanStack Query (or SWR)
- Tailwind CSS
- Shadcn/ui components
- Lucide React (icons)

### Tailwind Configuration
```javascript
// tailwind.config.js
module.exports = {
  content: ['./src/**/*.{js,jsx,ts,tsx}'],
  theme: {
    extend: {
      colors: {
        // Design system colors from design-system.json
      },
      // Typography, spacing, etc.
    },
  },
  plugins: [],
}
```

### Environment Variables
```env
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_APP_NAME=Notification Platform
```

## Compliance Verification

### After Implementation
- [ ] Verify application starts without errors
- [ ] Verify project structure matches specification
- [ ] Verify Tailwind CSS works correctly
- [ ] Verify Shadcn/ui components work correctly
- [ ] Verify TypeScript compilation works
- [ ] Verify build works
- [ ] Verify design system colors are applied
- [ ] Verify no violations of architecture specifications

## Related Documentation

- `@import(technical/frontend/overview.md)`
- `@import(technical/frontend/implementation-guide.md)`
- `@import(technical/frontend/design-system.json)` ⚠️ **MUST MATCH**
- `@import(technical/frontend/components.md)`
