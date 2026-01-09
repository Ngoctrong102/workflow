# Sprint 02: Core Infrastructure (API Client, Routing, State)

## Goal
Setup API client, routing, and state management, ensuring compliance with `docs/api/endpoints.md` and `docs/technical/frontend/routing.md`.

## Phase
Foundation

## Complexity
Medium

## Dependencies
Sprint 01

## Compliance Check

### Before Starting
1. ✅ Read `@import(api/endpoints.md)` - Understand API contracts
2. ✅ Read `@import(technical/frontend/routing.md)` - Understand routing structure
3. ✅ Read `@import(technical/integration/api-contract.md)` - Understand API contract
4. ✅ Check existing API client for violations
5. ✅ Check existing routing for violations
6. ✅ Fix any violations immediately before proceeding

### Existing Code Verification
- [ ] Verify API client matches API contracts
- [ ] Verify routing matches specification
- [ ] Verify state management structure is correct
- [ ] Fix any violations found

## Tasks

### API Client
- [ ] Create API client wrapper (Axios instance or fetch wrapper)
- [ ] Setup base URL configuration from environment variables
- [ ] Implement request interceptors:
  - [ ] Add authentication headers (if needed)
  - [ ] Add request logging
  - [ ] Add request transformation
- [ ] Implement response interceptors:
  - [ ] Handle success responses
  - [ ] Handle error responses
  - [ ] Transform error messages
  - [ ] Add response logging
- [ ] Create API service structure:
  - [ ] `api/workflow.ts` - Workflow API calls
  - [ ] `api/execution.ts` - Execution API calls
  - [ ] `api/trigger.ts` - Trigger API calls
  - [ ] `api/action.ts` - Action Registry API calls
  - [ ] `api/analytics.ts` - Analytics API calls
- [ ] Implement error handling:
  - [ ] Network errors
  - [ ] HTTP errors (4xx, 5xx)
  - [ ] Validation errors
  - [ ] Timeout errors
- [ ] Add TypeScript types for API responses

### Routing
- [ ] Configure React Router (`react-router-dom`)
- [ ] Create route definitions matching `@import(technical/frontend/routing.md)`:
  - [ ] `/` - Dashboard
  - [ ] `/workflows` - Workflow list
  - [ ] `/workflows/new` - Create workflow
  - [ ] `/workflows/:id` - Workflow details/edit
  - [ ] `/workflows/:id/builder` - Workflow builder
  - [ ] `/executions` - Execution list
  - [ ] `/executions/:id` - Execution details
  - [ ] `/analytics` - Analytics dashboard
  - [ ] `/templates` - Template management (if needed)
  - [ ] `/channels` - Channel management (if needed)
- [ ] Setup route guards (if authentication needed)
- [ ] Create navigation components:
  - [ ] `Navigation` component
  - [ ] `Sidebar` component
  - [ ] `Breadcrumbs` component
- [ ] Implement route transitions (optional)

### State Management
- [ ] Setup state management library (Zustand or Redux Toolkit)
- [ ] Create store structure:
  - [ ] `store/workflow.ts` - Workflow state
  - [ ] `store/execution.ts` - Execution state
  - [ ] `store/ui.ts` - UI state (modals, toasts, etc.)
- [ ] Setup API state management (`@tanstack/react-query` or `swr`):
  - [ ] Configure query client
  - [ ] Setup default options
  - [ ] Configure caching strategy
- [ ] Create custom hooks for API calls:
  - [ ] `useWorkflows()` - Fetch workflows
  - [ ] `useWorkflow(id)` - Fetch single workflow
  - [ ] `useCreateWorkflow()` - Create workflow mutation
  - [ ] `useUpdateWorkflow()` - Update workflow mutation
  - [ ] `useDeleteWorkflow()` - Delete workflow mutation
  - [ ] `useExecutions()` - Fetch executions
  - [ ] `useExecution(id)` - Fetch single execution

### Component Structure
- [ ] Create component directory structure
- [ ] Setup component templates
- [ ] Create base layout components:
  - [ ] `Layout` component
  - [ ] `Header` component
  - [ ] `Sidebar` component
  - [ ] `Footer` component (if needed)
- [ ] Setup component exports (`index.ts` files)

## Deliverables

- ✅ API client configured and working
- ✅ Routing setup complete
- ✅ State management configured
- ✅ API hooks created
- ✅ Component structure in place

## Technical Details

### API Client Structure

#### Base API Client
```typescript
// services/api/client.ts
import axios from 'axios';

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor
apiClient.interceptors.request.use(
  (config) => {
    // Add auth token if needed
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor
apiClient.interceptors.response.use(
  (response) => response.data,
  (error) => {
    // Handle errors
    return Promise.reject(error);
  }
);

export default apiClient;
```

#### API Service Example
```typescript
// services/api/workflow.ts
import apiClient from './client';
import type { WorkflowDTO, WorkflowRequestDTO } from '@/types';

export const workflowApi = {
  getAll: (params?: { status?: string; limit?: number; offset?: number }) =>
    apiClient.get<{ workflows: WorkflowDTO[]; total: number }>('/workflows', { params }),
  
  getById: (id: string) =>
    apiClient.get<WorkflowDTO>(`/workflows/${id}`),
  
  create: (data: WorkflowRequestDTO) =>
    apiClient.post<WorkflowDTO>('/workflows', data),
  
  update: (id: string, data: WorkflowRequestDTO) =>
    apiClient.put<WorkflowDTO>(`/workflows/${id}`, data),
  
  delete: (id: string) =>
    apiClient.delete(`/workflows/${id}`),
};
```

### Routing Structure

```typescript
// router/index.tsx
import { createBrowserRouter } from 'react-router-dom';
import Layout from '@/components/Layout';
import Dashboard from '@/pages/Dashboard';
import WorkflowList from '@/pages/WorkflowList';
import WorkflowBuilder from '@/pages/WorkflowBuilder';
// ... other imports

export const router = createBrowserRouter([
  {
    path: '/',
    element: <Layout />,
    children: [
      { index: true, element: <Dashboard /> },
      { path: 'workflows', element: <WorkflowList /> },
      { path: 'workflows/new', element: <WorkflowBuilder /> },
      { path: 'workflows/:id', element: <WorkflowDetails /> },
      { path: 'workflows/:id/builder', element: <WorkflowBuilder /> },
      // ... other routes
    ],
  },
]);
```

### State Management Structure

#### Zustand Store Example
```typescript
// store/workflow.ts
import { create } from 'zustand';

interface WorkflowState {
  selectedWorkflow: WorkflowDTO | null;
  setSelectedWorkflow: (workflow: WorkflowDTO | null) => void;
}

export const useWorkflowStore = create<WorkflowState>((set) => ({
  selectedWorkflow: null,
  setSelectedWorkflow: (workflow) => set({ selectedWorkflow: workflow }),
}));
```

#### React Query Hooks Example
```typescript
// hooks/useWorkflows.ts
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { workflowApi } from '@/services/api/workflow';

export const useWorkflows = (params?: { status?: string }) => {
  return useQuery({
    queryKey: ['workflows', params],
    queryFn: () => workflowApi.getAll(params),
  });
};

export const useCreateWorkflow = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: workflowApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['workflows'] });
    },
  });
};
```

## Compliance Verification

### After Implementation
- [ ] Verify API client matches API contracts (`@import(api/endpoints.md)`)
- [ ] Verify routing matches specification (`@import(technical/frontend/routing.md)`)
- [ ] Verify state management works correctly
- [ ] Test API calls work correctly
- [ ] Test routing works correctly
- [ ] Test state updates work correctly
- [ ] Verify error handling works correctly
- [ ] Verify no violations of architecture specifications

## Related Documentation

- `@import(technical/frontend/routing.md)` ⚠️ **MUST MATCH**
- `@import(technical/integration/api-contract.md)`
- `@import(api/endpoints.md)` ⚠️ **MUST MATCH**
- `@import(technical/frontend/overview.md)`
