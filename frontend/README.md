# Frontend - No-Code Notification Platform

## Overview

This is the frontend application for the No-Code Notification Platform, built with React, TypeScript, Vite, and Tailwind CSS.

## Tech Stack

- **React 19** - UI library
- **TypeScript** - Type safety
- **Vite** - Build tool and dev server
- **Tailwind CSS** - Utility-first CSS framework
- **Shadcn/ui** - Component library (Radix UI + Tailwind)
- **React Router** - Client-side routing
- **React Query (TanStack Query)** - Server state management
- **Zustand** - Client state management
- **Axios** - HTTP client
- **React Hook Form** - Form management
- **React Flow** - Workflow builder canvas
- **Recharts** - Chart library

## Getting Started

### Prerequisites

- Node.js 20.19+ or 22.12+
- npm 9+

### Installation

```bash
npm install
```

### Development

```bash
npm run dev
```

The application will be available at `http://localhost:5173`

### Build

```bash
npm run build
```

### Preview Production Build

```bash
npm run preview
```

## Testing

### Setup

Testing is configured with Vitest and React Testing Library. The test environment includes:
- React Testing Library for component testing
- Vitest for test runner
- jsdom for DOM simulation
- Custom test utilities for providers (React Query, Router, etc.)

**Note**: There is a known issue with `html-encoding-sniffer` dependency that may cause warnings during test runs. These warnings do not affect test execution.

### Run Tests

```bash
# Run tests once
npm test

# Run tests in watch mode
npm run test:watch

# Run tests with UI
npm run test:ui

# Run tests with coverage
npm run test:coverage
```

### Test Structure

- **Unit tests**: `src/**/*.test.ts` and `src/**/*.test.tsx`
- **Test setup**: `src/test/setup.ts` - Global test configuration
- **Test utilities**: `src/test/test-utils.tsx` - Custom render with providers

### Test Coverage

Current test coverage includes:
- ✅ Component tests (Button, LoadingSpinner, LoadingSkeleton, MetricCard, Navigation)
- ✅ Utility tests (error-handler, workflow-validation, utils, performance, toast-helpers)
- ✅ Hook tests (use-debounce, use-workflows)
- ✅ Service tests (workflow-service)

### Writing Tests

#### Component Tests

```typescript
import { describe, it, expect } from 'vitest'
import { render, screen } from '@/test/test-utils'
import { MyComponent } from './MyComponent'

describe('MyComponent', () => {
  it('should render', () => {
    render(<MyComponent />)
    expect(screen.getByText('Hello')).toBeInTheDocument()
  })
})
```

#### Hook Tests

```typescript
import { renderHook, waitFor } from '@testing-library/react'
import { useMyHook } from './use-my-hook'

describe('useMyHook', () => {
  it('should return expected value', async () => {
    const { result } = renderHook(() => useMyHook())
    await waitFor(() => {
      expect(result.current).toBeDefined()
    })
  })
})
```

#### Service Tests

```typescript
import { describe, it, expect, vi } from 'vitest'
import { myService } from './my-service'
import { api } from './api-client'

vi.mock('./api-client')

describe('myService', () => {
  it('should call API', async () => {
    vi.mocked(api.get).mockResolvedValue({ data: 'test' })
    const result = await myService.getData()
    expect(api.get).toHaveBeenCalled()
  })
})
```

## Project Structure

```
frontend/
├── src/
│   ├── components/      # React components
│   │   ├── ui/         # Shadcn/ui components
│   │   ├── common/     # Common components
│   │   ├── dashboard/  # Dashboard components
│   │   ├── workflow/   # Workflow builder components
│   │   ├── template/   # Template components
│   │   ├── channel/    # Channel components
│   │   ├── trigger/    # Trigger components
│   │   ├── analytics/  # Analytics components
│   │   └── error/      # Error handling components
│   ├── pages/          # Page components
│   ├── hooks/          # Custom React hooks
│   ├── services/      # API services
│   ├── store/          # Zustand stores
│   ├── types/          # TypeScript types
│   ├── utils/          # Utility functions
│   ├── constants/      # Constants
│   ├── router/         # React Router configuration
│   ├── providers/      # React context providers
│   └── test/           # Test setup and utilities
├── public/             # Static assets
└── dist/               # Build output
```

## Features

### Core Features

- **Dashboard**: Overview with metrics, charts, and activity feed
- **Workflow Builder**: Visual workflow creation with drag-and-drop
- **Template Management**: Create and manage notification templates
- **Channel Management**: Configure notification channels
- **Trigger Management**: Set up workflow triggers
- **Analytics**: View delivery metrics and error analytics

### Key Components

- **Error Boundary**: Catches React errors and displays fallback UI
- **Confirmation Dialogs**: Replaces `window.confirm` with styled dialogs
- **Toast Notifications**: User feedback for actions
- **Loading States**: Skeleton loaders and spinners
- **Code Splitting**: Route-based lazy loading for performance

## Configuration

### Environment Variables

Create a `.env` file in the root directory:

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

### Path Aliases

- `@/` - Maps to `src/`

## Performance Optimization

- **Code Splitting**: Routes are lazy-loaded
- **Bundle Optimization**: Vendor chunks are separated
- **Component Memoization**: React.memo for expensive components
- **Tree Shaking**: Unused code is removed

## Error Handling

- **Error Boundaries**: Catch React component errors
- **API Error Handling**: Transform Axios errors to ApiException
- **User-Friendly Messages**: Map error codes to readable messages
- **Error Logging**: Log errors with context for debugging

## Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

## Troubleshooting

### Test Warnings

If you see warnings about `html-encoding-sniffer` during test runs, these are harmless and can be ignored. They do not affect test execution.

### Build Errors

If you encounter build errors:
1. Clear `node_modules` and reinstall: `rm -rf node_modules && npm install`
2. Clear Vite cache: `rm -rf node_modules/.vite`
3. Check Node.js version: `node --version` (should be 20.19+ or 22.12+)

### Port Already in Use

If port 5173 is already in use:
```bash
# Kill process on port 5173
lsof -ti:5173 | xargs kill -9
```

## Development Guidelines

### Code Style

- Use TypeScript for all files
- Follow ESLint rules
- Use Prettier for formatting
- Use functional components with hooks
- Prefer composition over inheritance

### Component Guidelines

- Keep components small and focused
- Use TypeScript interfaces for props
- Memoize expensive components with React.memo
- Extract reusable logic into custom hooks

### API Integration

- Use React Query for data fetching
- Handle errors with try-catch and error boundaries
- Show loading states during API calls
- Provide user feedback with toast notifications

## Component Documentation

### Common Components

#### LoadingSpinner
Displays a loading spinner with optional text.

```tsx
<LoadingSpinner size="md" text="Loading..." fullScreen />
```

#### LoadingSkeleton
Displays skeleton loaders for different content types.

```tsx
<LoadingSkeleton variant="page" count={3} />
```

#### LoadingOverlay
Overlays a loading indicator on top of content.

```tsx
<LoadingOverlay isLoading={isLoading} text="Loading...">
  <YourContent />
</LoadingOverlay>
```

#### ErrorBoundary
Catches React errors and displays a fallback UI.

```tsx
<ErrorBoundary>
  <YourComponent />
</ErrorBoundary>
```

### Hooks

#### useDebounce
Debounces a value to reduce updates.

```tsx
const debouncedValue = useDebounce(value, 300)
```

#### useErrorHandler
Handles errors with toast notifications and logging.

```tsx
const { handleError } = useErrorHandler({
  showToast: true,
  context: 'ComponentName',
})
```

#### useRetry
Provides retry functionality for failed operations.

```tsx
const { retry, isRetrying } = useRetry({
  maxRetries: 3,
  retryDelay: 1000,
})
```

### Utilities

#### Performance Monitoring

```tsx
import { performanceMonitor } from '@/utils/performance'

// Measure function execution
const result = await performanceMonitor.measureAsync('operation', async () => {
  // Your async operation
})

// Get metrics
const metrics = performanceMonitor.getMetrics()
```

#### Error Handling

```tsx
import { handleApiError, getUserFriendlyErrorMessage } from '@/utils/error-handler'

try {
  await apiCall()
} catch (error) {
  const apiError = handleApiError(error)
  const message = getUserFriendlyErrorMessage(apiError)
  // Show message to user
}
```

## API Integration

### Service Pattern

All API services follow a consistent pattern:

```typescript
// services/my-service.ts
import { api } from './api-client'

export const myService = {
  list: (params?: ListParams) => api.get<PagedResponse<Item>>('/v1/items', { params }),
  get: (id: string) => api.get<Item>(`/v1/items/${id}`),
  create: (data: CreateRequest) => api.post<Item>('/v1/items', data),
  update: (id: string, data: UpdateRequest) => api.put<Item>(`/v1/items/${id}`, data),
  delete: (id: string) => api.delete(`/v1/items/${id}`),
}
```

### React Query Hooks

```typescript
// hooks/use-items.ts
import { useQuery, useMutation } from '@tanstack/react-query'
import { itemService } from '@/services/item-service'

export function useItems(params?: ListParams) {
  return useQuery({
    queryKey: ['items', params],
    queryFn: () => itemService.list(params),
  })
}

export function useCreateItem() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation({
    mutationFn: (data: CreateRequest) => itemService.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['items'] })
      toast({ title: 'Item created' })
    },
    onError: (error) => {
      toast({ variant: 'destructive', title: 'Failed', description: error.message })
    },
  })
}
```

## Troubleshooting

### Test Warnings

If you see warnings about `html-encoding-sniffer` during test runs, these are harmless and can be ignored. They do not affect test execution.

### Build Errors

If you encounter build errors:
1. Clear `node_modules` and reinstall: `rm -rf node_modules && npm install`
2. Clear Vite cache: `rm -rf node_modules/.vite`
3. Check Node.js version: `node --version` (should be 20.19+ or 22.12+)

### Port Already in Use

If port 5173 is already in use:
```bash
# Kill process on port 5173
lsof -ti:5173 | xargs kill -9
```

### API Connection Issues

If you're having trouble connecting to the backend:
1. Check `VITE_API_BASE_URL` in `.env`
2. Verify backend is running
3. Check CORS configuration in backend
4. Check browser console for network errors

### Performance Issues

If experiencing performance issues:
1. Check React DevTools Profiler
2. Use performance monitoring utilities
3. Verify code splitting is working
4. Check bundle size with `npm run build`

## Related Documentation

- [API Contract](../../docs/technical/integration/api-contract.md)
- [Error Handling](../../docs/api/error-handling.md)
- [Design System](../../docs/technical/frontend/design-system.json)
- [Frontend Technical Guide](../../docs/technical/frontend/implementation-guide.md)
