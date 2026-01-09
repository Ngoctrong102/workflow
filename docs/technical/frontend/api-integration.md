# API Integration Patterns

## Overview

This document describes the API integration patterns used in the frontend application. All API calls follow a consistent pattern using Axios, React Query, and custom service layers.

## Architecture

### Service Layer

All API calls are abstracted through service modules located in `src/services/`. Each service module exports a service object with methods for CRUD operations.

### Service Pattern

```typescript
// services/my-service.ts
import { apiClient } from './api-client'
import type { PagedResponse, MyEntity, CreateMyEntityRequest, UpdateMyEntityRequest } from '@/types'

export interface ListMyEntitiesParams {
  search?: string
  status?: string
  limit?: number
  offset?: number
}

export interface ListMyEntitiesResponse extends PagedResponse<MyEntity> {}

export const myService = {
  /**
   * List all entities
   */
  list: async (params?: ListMyEntitiesParams): Promise<ListMyEntitiesResponse> => {
    const response = await apiClient.get<PagedResponse<MyEntity>>('/v1/my-entities', {
      params,
    })
    return response.data
  },

  /**
   * Get entity by ID
   */
  get: async (id: string): Promise<MyEntity> => {
    const response = await apiClient.get<MyEntity>(`/v1/my-entities/${id}`)
    return response.data
  },

  /**
   * Create new entity
   */
  create: async (entity: CreateMyEntityRequest): Promise<MyEntity> => {
    const response = await apiClient.post<MyEntity>('/v1/my-entities', entity)
    return response.data
  },

  /**
   * Update entity
   */
  update: async (id: string, entity: Omit<UpdateMyEntityRequest, 'id'>): Promise<MyEntity> => {
    const response = await apiClient.put<MyEntity>(`/v1/my-entities/${id}`, entity)
    return response.data
  },

  /**
   * Delete entity
   */
  delete: async (id: string): Promise<void> => {
    await apiClient.delete(`/v1/my-entities/${id}`)
  },
}
```

## React Query Hooks

### Query Hooks

Query hooks use `useQuery` from React Query to fetch data:

```typescript
// hooks/use-my-entities.ts
import { useQuery } from '@tanstack/react-query'
import { myService, type ListMyEntitiesParams } from '@/services/my-service'

export function useMyEntities(params?: ListMyEntitiesParams) {
  return useQuery({
    queryKey: ['my-entities', params],
    queryFn: () => myService.list(params),
    staleTime: 2 * 60 * 1000, // 2 minutes
    gcTime: 5 * 60 * 1000, // 5 minutes
  })
}

export function useMyEntity(id: string | undefined) {
  return useQuery({
    queryKey: ['my-entity', id],
    queryFn: () => myService.get(id!),
    enabled: !!id,
    staleTime: 1 * 60 * 1000, // 1 minute
    gcTime: 5 * 60 * 1000, // 5 minutes
  })
}
```

### Mutation Hooks

Mutation hooks use `useMutation` from React Query to modify data:

```typescript
// hooks/use-my-entities.ts
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useToast } from '@/hooks/use-toast'
import { myService, type CreateMyEntityRequest, type UpdateMyEntityRequest } from '@/services/my-service'
import { getUserFriendlyErrorMessage, type ApiException } from '@/utils/error-handler'

export function useCreateMyEntity() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation({
    mutationFn: (entity: CreateMyEntityRequest) => myService.create(entity),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['my-entities'] })
      toast({
        title: 'Entity Created',
        description: 'Entity has been created successfully',
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: 'destructive',
        title: 'Failed to Create Entity',
        description: getUserFriendlyErrorMessage(error),
      })
    },
  })
}

export function useUpdateMyEntity() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation({
    mutationFn: ({ id, ...entity }: UpdateMyEntityRequest) => myService.update(id, entity),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['my-entities'] })
      queryClient.invalidateQueries({ queryKey: ['my-entity', variables.id] })
      toast({
        title: 'Entity Updated',
        description: 'Entity has been updated successfully',
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: 'destructive',
        title: 'Failed to Update Entity',
        description: getUserFriendlyErrorMessage(error),
      })
    },
  })
}

export function useDeleteMyEntity() {
  const queryClient = useQueryClient()
  const { toast } = useToast()

  return useMutation({
    mutationFn: (id: string) => myService.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['my-entities'] })
      toast({
        title: 'Entity Deleted',
        description: 'Entity has been deleted successfully',
      })
    },
    onError: (error: ApiException) => {
      toast({
        variant: 'destructive',
        title: 'Failed to Delete Entity',
        description: getUserFriendlyErrorMessage(error),
      })
    },
  })
}
```

## API Client Configuration

### Base Configuration

The API client is configured in `src/services/api-client.ts`:

```typescript
import axios from 'axios'
import { API_BASE_URL } from '@/constants'

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor
apiClient.interceptors.request.use(
  (config) => {
    // Add auth token if available
    const token = localStorage.getItem('authToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    // Handle errors globally
    if (error.response) {
      // Server responded with error
      const apiError = handleApiError(error)
      logError(apiError, 'API Error')
    } else if (error.request) {
      // Request made but no response
      logError({ code: 'NETWORK_ERROR', message: 'Network error occurred' }, 'Network Error')
    }
    return Promise.reject(error)
  }
)
```

## Error Handling

### Error Types

All API errors are transformed into `ApiException` format:

```typescript
export interface ApiException {
  code: string
  message: string
  statusCode?: number
  details?: Record<string, unknown>
  requestId?: string
}
```

### Error Handling Utilities

```typescript
// utils/error-handler.ts
import axios from 'axios'
import type { AxiosError } from 'axios'

export function handleApiError(error: unknown): ApiException {
  if (axios.isAxiosError(error)) {
    const axiosError = error as AxiosError<{ error?: ApiException }>
    
    if (axiosError.response?.data?.error) {
      return axiosError.response.data.error
    }
    
    return {
      code: `HTTP_${axiosError.response?.status || 'UNKNOWN'}`,
      message: axiosError.message || 'An error occurred',
      statusCode: axiosError.response?.status,
    }
  }
  
  if (error instanceof Error) {
    return {
      code: 'UNKNOWN_ERROR',
      message: error.message,
    }
  }
  
  return {
    code: 'UNKNOWN_ERROR',
    message: 'An unknown error occurred',
  }
}

export function getUserFriendlyErrorMessage(error: ApiException): string {
  // Map error codes to user-friendly messages
  const errorMessages: Record<string, string> = {
    'WORKFLOW_NOT_FOUND': 'Workflow not found',
    'INVALID_WORKFLOW_DEFINITION': 'Invalid workflow definition',
    'NETWORK_ERROR': 'Network error. Please check your connection.',
    // Add more mappings
  }
  
  return errorMessages[error.code] || error.message || 'An error occurred'
}
```

## Caching Strategy

### React Query Caching

React Query provides automatic caching with configurable stale times:

- **Static Data** (Registry, Templates): 10 minutes stale time, 30 minutes gc time
- **Dynamic Data** (Workflows, Executions): 1-2 minutes stale time, 5 minutes gc time
- **Analytics Data**: 1 minute stale time, 5 minutes gc time

### LocalStorage Caching

Static registry data is cached in localStorage for faster initial load:

```typescript
// utils/local-storage-cache.ts
export function getCachedData<T>(key: string, maxAge: number = 24 * 60 * 60 * 1000): T | null {
  // Check cache and validate age
}

export function setCachedData<T>(key: string, data: T): void {
  // Store data with timestamp
}
```

## Polling

For real-time data (e.g., execution status), use React Query's `refetchInterval`:

```typescript
export function useExecution(executionId: string | undefined) {
  return useQuery({
    queryKey: ['execution', executionId],
    queryFn: () => executionService.get(executionId!),
    enabled: !!executionId,
    refetchInterval: (query) => {
      const data = query.state.data
      if (!data) return false
      
      // Poll every 3 seconds when waiting
      if (data.status === 'waiting') return 3000
      // Poll every 10 seconds when running
      if (data.status === 'running') return 10000
      // Stop polling when completed
      return false
    },
  })
}
```

## Best Practices

### 1. Always Use Service Layer

Never call `apiClient` directly from components. Always use service methods:

```typescript
// ✅ Good
const { data } = useWorkflows()

// ❌ Bad
const { data } = useQuery({
  queryFn: () => apiClient.get('/workflows')
})
```

### 2. Handle Loading and Error States

Always handle loading and error states in components:

```typescript
const { data, isLoading, error } = useWorkflows()

if (isLoading) return <LoadingSkeleton />
if (error) return <ErrorState error={error} />
if (!data) return <EmptyState />

return <WorkflowList workflows={data.workflows} />
```

### 3. Invalidate Queries After Mutations

Always invalidate related queries after mutations:

```typescript
onSuccess: () => {
  queryClient.invalidateQueries({ queryKey: ['workflows'] })
  queryClient.invalidateQueries({ queryKey: ['workflow', id] })
}
```

### 4. Use Optimistic Updates When Appropriate

For better UX, use optimistic updates for mutations:

```typescript
return useMutation({
  mutationFn: updateWorkflow,
  onMutate: async (newData) => {
    await queryClient.cancelQueries({ queryKey: ['workflow', id] })
    const previousData = queryClient.getQueryData(['workflow', id])
    queryClient.setQueryData(['workflow', id], newData)
    return { previousData }
  },
  onError: (err, newData, context) => {
    queryClient.setQueryData(['workflow', id], context?.previousData)
  },
  onSettled: () => {
    queryClient.invalidateQueries({ queryKey: ['workflow', id] })
  },
})
```

### 5. Debounce Search Queries

Debounce search inputs to reduce API calls:

```typescript
const [searchQuery, setSearchQuery] = useState('')
const debouncedSearchQuery = useDebounce(searchQuery, 300)

const { data } = useWorkflows({ search: debouncedSearchQuery })
```

## Related Documentation

- [API Contract](../integration/api-contract.md) - Backend API specifications
- [Error Handling](../../api/error-handling.md) - Error handling patterns
- [State Management](./state-management.md) - State management patterns

