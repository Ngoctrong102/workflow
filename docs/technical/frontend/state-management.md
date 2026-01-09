# State Management Patterns

## Overview

This document describes the state management patterns used in the frontend application. The application uses a combination of Zustand for client-side state and React Query for server-side state.

## Architecture

### State Types

1. **Server State**: Data fetched from APIs (managed by React Query)
2. **Client State**: UI state, form state, workflow builder state (managed by Zustand)
3. **Form State**: Form inputs and validation (managed by React Hook Form)

## Zustand Stores

### Workflow Store

The workflow store manages the workflow builder state:

```typescript
// store/workflow-store.ts
import { create } from 'zustand'
import type { Node, Edge } from 'reactflow'
import type { WorkflowDefinition } from '@/types/workflow'

interface WorkflowStore {
  // State
  nodes: Node[]
  edges: Edge[]
  selectedNode: Node | null
  workflowName: string
  workflowDescription: string
  workflowStatus: 'draft' | 'active' | 'paused' | 'archived'
  isDirty: boolean
  
  // Actions
  setNodes: (nodes: Node[]) => void
  setEdges: (edges: Edge[]) => void
  setSelectedNode: (node: Node | null) => void
  setWorkflowName: (name: string) => void
  setWorkflowDescription: (description: string) => void
  setWorkflowStatus: (status: WorkflowStore['workflowStatus']) => void
  addNode: (node: Node) => void
  removeNode: (nodeId: string) => void
  updateNode: (nodeId: string, updates: Partial<Node>) => void
  addEdge: (edge: Edge) => void
  removeEdge: (edgeId: string) => void
  setDirty: (dirty: boolean) => void
  reset: () => void
  loadWorkflow: (workflow: WorkflowDefinition) => void
  getWorkflowDefinition: () => WorkflowDefinition
}

export const useWorkflowStore = create<WorkflowStore>((set, get) => ({
  // Initial state
  nodes: [],
  edges: [],
  selectedNode: null,
  workflowName: '',
  workflowDescription: '',
  workflowStatus: 'draft',
  isDirty: false,
  
  // Actions
  setNodes: (nodes) => set({ nodes, isDirty: true }),
  setEdges: (edges) => set({ edges, isDirty: true }),
  setSelectedNode: (node) => set({ selectedNode: node }),
  setWorkflowName: (name) => set({ workflowName: name, isDirty: true }),
  setWorkflowDescription: (description) => set({ workflowDescription: description, isDirty: true }),
  setWorkflowStatus: (status) => set({ workflowStatus: status }),
  
  addNode: (node) => set((state) => ({
    nodes: [...state.nodes, node],
    isDirty: true,
  })),
  
  removeNode: (nodeId) => set((state) => ({
    nodes: state.nodes.filter((n) => n.id !== nodeId),
    edges: state.edges.filter((e) => e.source !== nodeId && e.target !== nodeId),
    selectedNode: state.selectedNode?.id === nodeId ? null : state.selectedNode,
    isDirty: true,
  })),
  
  updateNode: (nodeId, updates) => set((state) => ({
    nodes: state.nodes.map((n) => (n.id === nodeId ? { ...n, ...updates } : n)),
    selectedNode: state.selectedNode?.id === nodeId
      ? { ...state.selectedNode, ...updates }
      : state.selectedNode,
    isDirty: true,
  })),
  
  addEdge: (edge) => set((state) => ({
    edges: [...state.edges, edge],
    isDirty: true,
  })),
  
  removeEdge: (edgeId) => set((state) => ({
    edges: state.edges.filter((e) => e.id !== edgeId),
    isDirty: true,
  })),
  
  setDirty: (dirty) => set({ isDirty: dirty }),
  
  reset: () => set({
    nodes: [],
    edges: [],
    selectedNode: null,
    workflowName: '',
    workflowDescription: '',
    workflowStatus: 'draft',
    isDirty: false,
  }),
  
  loadWorkflow: (workflow) => {
    // Convert workflow definition to React Flow nodes and edges
    const nodes: Node[] = workflow.nodes.map((node) => ({
      id: node.id,
      type: 'workflow',
      position: node.position,
      data: {
        type: node.type,
        label: node.data.label,
        config: node.data.config,
      },
    }))
    
    const edges: Edge[] = workflow.edges.map((edge) => ({
      id: edge.id,
      source: edge.source,
      target: edge.target,
      sourceHandle: edge.sourceHandle,
      targetHandle: edge.targetHandle,
    }))
    
    set({
      nodes,
      edges,
      workflowName: workflow.name,
      workflowDescription: workflow.description || '',
      workflowStatus: workflow.status || 'draft',
      isDirty: false,
    })
  },
  
  getWorkflowDefinition: () => {
    const state = get()
    return {
      name: state.workflowName,
      description: state.workflowDescription,
      status: state.workflowStatus,
      nodes: state.nodes.map((node) => ({
        id: node.id,
        type: node.data.type,
        position: node.position,
        data: {
          label: node.data.label,
          config: node.data.config,
        },
      })),
      edges: state.edges.map((edge) => ({
        id: edge.id,
        source: edge.source,
        target: edge.target,
        sourceHandle: edge.sourceHandle,
        targetHandle: edge.targetHandle,
      })),
    }
  },
}))
```

### UI Store

The UI store manages global UI state:

```typescript
// store/ui-store.ts
import { create } from 'zustand'

interface UIStore {
  sidebarOpen: boolean
  theme: 'light' | 'dark'
  
  setSidebarOpen: (open: boolean) => void
  toggleSidebar: () => void
  setTheme: (theme: 'light' | 'dark') => void
}

export const useUIStore = create<UIStore>((set) => ({
  sidebarOpen: true,
  theme: 'light',
  
  setSidebarOpen: (open) => set({ sidebarOpen: open }),
  toggleSidebar: () => set((state) => ({ sidebarOpen: !state.sidebarOpen })),
  setTheme: (theme) => set({ theme }),
}))
```

## React Query for Server State

### Query Configuration

React Query is configured globally in `src/providers/query-provider.tsx`:

```typescript
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: (failureCount, error) => {
        // Don't retry on 4xx errors
        if (error && typeof error === 'object' && 'statusCode' in error) {
          const statusCode = error.statusCode as number
          if (statusCode >= 400 && statusCode < 500) {
            return false
          }
        }
        return failureCount < 3
      },
      staleTime: 5 * 60 * 1000, // 5 minutes default
      gcTime: 10 * 60 * 1000, // 10 minutes
    },
  },
})
```

### Caching Strategy

Different data types have different caching strategies:

- **Registry Data** (Triggers, Actions): 10 minutes stale, 30 minutes gc
- **Workflow List**: 2 minutes stale, 5 minutes gc
- **Workflow Details**: 1 minute stale, 5 minutes gc
- **Executions**: 30 seconds stale, 2 minutes gc
- **Analytics**: 1 minute stale, 5 minutes gc

## React Hook Form for Form State

### Form Pattern

Forms use React Hook Form for state management:

```typescript
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'

const formSchema = z.object({
  name: z.string().min(1, 'Name is required'),
  description: z.string().optional(),
})

type FormData = z.infer<typeof formSchema>

export function MyForm() {
  const { register, handleSubmit, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(formSchema),
  })
  
  const onSubmit = (data: FormData) => {
    // Handle form submission
  }
  
  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <input {...register('name')} />
      {errors.name && <span>{errors.name.message}</span>}
      <button type="submit">Submit</button>
    </form>
  )
}
```

## State Management Patterns

### 1. Server State → React Query

Use React Query for all server-side data:

```typescript
// ✅ Good
const { data, isLoading, error } = useWorkflows()

// ❌ Bad
const [workflows, setWorkflows] = useState([])
useEffect(() => {
  fetchWorkflows().then(setWorkflows)
}, [])
```

### 2. Client State → Zustand

Use Zustand for UI state and complex client-side state:

```typescript
// ✅ Good
const { sidebarOpen, toggleSidebar } = useUIStore()
const { nodes, addNode } = useWorkflowStore()

// ❌ Bad (for complex state)
const [sidebarOpen, setSidebarOpen] = useState(true)
const [nodes, setNodes] = useState([])
```

### 3. Form State → React Hook Form

Use React Hook Form for all form state:

```typescript
// ✅ Good
const { register, handleSubmit } = useForm()

// ❌ Bad
const [formData, setFormData] = useState({})
```

### 4. Local Component State → useState

Use `useState` for simple, component-local state:

```typescript
// ✅ Good
const [isOpen, setIsOpen] = useState(false)
const [selectedTab, setSelectedTab] = useState('tab1')

// ❌ Bad (for shared state)
const [sidebarOpen, setSidebarOpen] = useState(true) // Should be in Zustand
```

## Best Practices

### 1. Keep Stores Focused

Each store should manage a single domain:

```typescript
// ✅ Good
// workflow-store.ts - Workflow builder state
// ui-store.ts - UI state
// user-store.ts - User state

// ❌ Bad
// app-store.ts - Everything
```

### 2. Use Selectors for Performance

Use selectors to prevent unnecessary re-renders:

```typescript
// ✅ Good
const workflowName = useWorkflowStore((state) => state.workflowName)
const nodes = useWorkflowStore((state) => state.nodes)

// ❌ Bad
const { workflowName, nodes, edges, selectedNode, ... } = useWorkflowStore()
```

### 3. Derive State from Store

Derive computed state from store values:

```typescript
// ✅ Good
const hasUnsavedChanges = useWorkflowStore((state) => state.isDirty)
const nodeCount = useWorkflowStore((state) => state.nodes.length)

// ❌ Bad
const [hasUnsavedChanges, setHasUnsavedChanges] = useState(false)
```

### 4. Invalidate Queries After Mutations

Always invalidate related queries after mutations:

```typescript
onSuccess: () => {
  queryClient.invalidateQueries({ queryKey: ['workflows'] })
}
```

### 5. Use Optimistic Updates

Use optimistic updates for better UX:

```typescript
onMutate: async (newData) => {
  await queryClient.cancelQueries({ queryKey: ['workflow', id] })
  const previousData = queryClient.getQueryData(['workflow', id])
  queryClient.setQueryData(['workflow', id], newData)
  return { previousData }
},
```

## Related Documentation

- [API Integration](./api-integration.md) - API integration patterns
- [Component Documentation](./components.md) - Component patterns
- [React Query Documentation](https://tanstack.com/query/latest)
- [Zustand Documentation](https://zustand-demo.pmnd.rs/)

