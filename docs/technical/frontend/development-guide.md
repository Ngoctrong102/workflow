# Frontend Development Guide

## Getting Started

### Prerequisites

- Node.js 20.19+ or 22.12+
- npm 9+
- Git

### Initial Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd workflow/frontend
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

3. **Configure environment variables**
   ```bash
   cp .env.example .env
   # Edit .env and set VITE_API_BASE_URL
   ```

4. **Start development server**
   ```bash
   npm run dev
   ```

5. **Open browser**
   Navigate to `http://localhost:5173`

## Project Structure

```
frontend/
├── src/
│   ├── components/          # React components
│   │   ├── ui/             # Shadcn/ui base components
│   │   ├── common/         # Shared components
│   │   ├── dashboard/      # Dashboard components
│   │   ├── workflow/       # Workflow builder components
│   │   ├── template/       # Template components
│   │   ├── channel/        # Channel components
│   │   ├── trigger/        # Trigger components
│   │   ├── analytics/      # Analytics components
│   │   └── error/          # Error handling components
│   ├── pages/              # Page components (routes)
│   ├── hooks/              # Custom React hooks
│   ├── services/           # API service layer
│   ├── store/              # Zustand stores
│   ├── types/              # TypeScript type definitions
│   ├── utils/              # Utility functions
│   ├── constants/          # Constants and configuration
│   ├── router/            # React Router configuration
│   ├── providers/         # React context providers
│   └── test/               # Test utilities
├── public/                 # Static assets
└── dist/                   # Build output
```

## Development Workflow

### 1. Create a New Feature

#### Step 1: Create Types

Define TypeScript types in `src/types/`:

```typescript
// src/types/my-feature.ts
export interface MyFeature {
  id: string
  name: string
  description?: string
}

export interface CreateMyFeatureRequest {
  name: string
  description?: string
}
```

#### Step 2: Create Service

Create API service in `src/services/`:

```typescript
// src/services/my-feature-service.ts
import { apiClient } from './api-client'
import type { MyFeature, CreateMyFeatureRequest } from '@/types/my-feature'

export const myFeatureService = {
  list: async () => {
    const response = await apiClient.get<MyFeature[]>('/v1/my-features')
    return response.data
  },
  
  create: async (data: CreateMyFeatureRequest) => {
    const response = await apiClient.post<MyFeature>('/v1/my-features', data)
    return response.data
  },
}
```

#### Step 3: Create Hooks

Create React Query hooks in `src/hooks/`:

```typescript
// src/hooks/use-my-features.ts
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { myFeatureService } from '@/services/my-feature-service'
import { useToast } from '@/hooks/use-toast'

export function useMyFeatures() {
  return useQuery({
    queryKey: ['my-features'],
    queryFn: () => myFeatureService.list(),
  })
}

export function useCreateMyFeature() {
  const queryClient = useQueryClient()
  const { toast } = useToast()
  
  return useMutation({
    mutationFn: (data: CreateMyFeatureRequest) => myFeatureService.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['my-features'] })
      toast({ title: 'Feature created' })
    },
  })
}
```

#### Step 4: Create Components

Create components in appropriate directory:

```typescript
// src/components/my-feature/MyFeatureList.tsx
import { useMyFeatures } from '@/hooks/use-my-features'

export function MyFeatureList() {
  const { data, isLoading, error } = useMyFeatures()
  
  if (isLoading) return <LoadingSkeleton />
  if (error) return <ErrorState error={error} />
  
  return (
    <div>
      {data?.map((feature) => (
        <div key={feature.id}>{feature.name}</div>
      ))}
    </div>
  )
}
```

#### Step 5: Create Page

Create page component in `src/pages/`:

```typescript
// src/pages/MyFeatureList.tsx
import { MyFeatureList } from '@/components/my-feature/MyFeatureList'

export default function MyFeatureListPage() {
  return (
    <div className="container mx-auto p-6">
      <h1>My Features</h1>
      <MyFeatureList />
    </div>
  )
}
```

#### Step 6: Add Route

Add route in `src/router/index.tsx`:

```typescript
const MyFeatureList = lazy(() => import('@/pages/MyFeatureList'))

// In routes array
{
  path: 'my-features',
  element: <LazyPage component={MyFeatureList} />,
}
```

### 2. Component Development

#### Component Structure

```typescript
import { memo } from 'react'
import { cn } from '@/lib/utils'

interface MyComponentProps {
  title: string
  description?: string
  className?: string
}

export const MyComponent = memo(function MyComponent({
  title,
  description,
  className,
}: MyComponentProps) {
  return (
    <div className={cn('p-4', className)}>
      <h2>{title}</h2>
      {description && <p>{description}</p>}
    </div>
  )
})
```

#### Component Guidelines

1. **Use TypeScript interfaces for props**
2. **Memoize expensive components with `React.memo`**
3. **Use `cn()` utility for conditional classes**
4. **Extract reusable logic into custom hooks**
5. **Keep components small and focused**

### 3. Styling

#### Tailwind CSS

Use Tailwind utility classes:

```typescript
<div className="flex items-center justify-between p-4 bg-white rounded-lg shadow">
  <h2 className="text-lg font-semibold text-secondary-900">Title</h2>
</div>
```

#### Design System Colors

Use design system colors from `tailwind.config.js`:

```typescript
// Primary colors
className="bg-primary-500 text-primary-50"

// Secondary colors
className="bg-secondary-100 text-secondary-900"

// Status colors
className="bg-success-500 text-success-50"
className="bg-error-500 text-error-50"
className="bg-warning-500 text-warning-50"
```

### 4. Form Handling

#### React Hook Form Pattern

```typescript
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'

const formSchema = z.object({
  name: z.string().min(1, 'Name is required'),
  email: z.string().email('Invalid email'),
})

type FormData = z.infer<typeof formSchema>

export function MyForm() {
  const { register, handleSubmit, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(formSchema),
  })
  
  const onSubmit = (data: FormData) => {
    console.log(data)
  }
  
  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <input {...register('name')} />
      {errors.name && <span>{errors.name.message}</span>}
      
      <input {...register('email')} />
      {errors.email && <span>{errors.email.message}</span>}
      
      <button type="submit">Submit</button>
    </form>
  )
}
```

### 5. Error Handling

#### Component Error Handling

```typescript
import { ErrorBoundary } from '@/components/error/ErrorBoundary'
import { ErrorState } from '@/components/common/ErrorState'

export function MyPage() {
  const { data, error } = useMyData()
  
  if (error) {
    return <ErrorState error={error} onRetry={() => refetch()} />
  }
  
  return <div>{/* content */}</div>
}
```

#### API Error Handling

```typescript
import { handleApiError, getUserFriendlyErrorMessage } from '@/utils/error-handler'
import { useToast } from '@/hooks/use-toast'

try {
  await myService.create(data)
} catch (error) {
  const apiError = handleApiError(error)
  const message = getUserFriendlyErrorMessage(apiError)
  toast({ variant: 'destructive', title: 'Error', description: message })
}
```

### 6. Loading States

#### Loading Patterns

```typescript
// Skeleton loader
if (isLoading) return <LoadingSkeleton variant="page" />

// Spinner
if (isLoading) return <LoadingSpinner />

// Overlay
<LoadingOverlay isLoading={isLoading}>
  <YourContent />
</LoadingOverlay>
```

## Code Style

### TypeScript

- Use TypeScript for all files
- Define interfaces for all props and data structures
- Use type inference where appropriate
- Avoid `any` type

### Naming Conventions

- **Components**: PascalCase (`MyComponent.tsx`)
- **Hooks**: camelCase with `use` prefix (`useMyHook.ts`)
- **Services**: camelCase (`my-service.ts`)
- **Types**: PascalCase (`MyType.ts`)
- **Constants**: UPPER_SNAKE_CASE (`API_BASE_URL`)

### File Organization

- One component per file
- Co-locate related files (component + types + tests)
- Use index files for exports

## Performance Optimization

### Code Splitting

Routes are automatically lazy-loaded:

```typescript
const MyPage = lazy(() => import('@/pages/MyPage'))
```

### Component Memoization

Memoize expensive components:

```typescript
export const ExpensiveComponent = memo(function ExpensiveComponent({ data }) {
  // Component logic
})
```

### Data Memoization

Use `useMemo` for expensive calculations:

```typescript
const filteredData = useMemo(() => {
  return data.filter(item => item.active)
}, [data])
```

## Testing

### Running Tests

```bash
# Run all tests
npm test

# Run tests in watch mode
npm run test:watch

# Run tests with coverage
npm run test:coverage
```

### Writing Tests

See [Testing Guide](./testing.md) for detailed testing patterns.

## Debugging

### React DevTools

Install React DevTools browser extension for component inspection.

### React Query DevTools

React Query DevTools are available in development mode.

### Browser Console

Check browser console for:
- API errors
- React warnings
- Performance warnings

## Common Issues

### Build Errors

1. Clear `node_modules` and reinstall
2. Clear Vite cache: `rm -rf node_modules/.vite`
3. Check Node.js version

### API Connection Issues

1. Check `VITE_API_BASE_URL` in `.env`
2. Verify backend is running
3. Check CORS configuration

### Type Errors

1. Run `npm run build` to see all type errors
2. Check type definitions in `src/types/`
3. Verify API response types match

## Related Documentation

- [API Integration](./api-integration.md) - API integration patterns
- [State Management](./state-management.md) - State management patterns
- [Components](./components.md) - Component specifications
- [Design System](./design-system.json) - UI/UX specifications

