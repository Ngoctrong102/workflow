# Frontend Component Specifications

## Component Categories

### UI Components (Shadcn/ui)
- **Button**: Standard button component
- **Input**: Text input component
- **Select**: Dropdown select component
- **Modal**: Modal dialog component
- **Card**: Card container component
- **Table**: Data table component
- **Form**: Form components
- **Toast**: Notification toast component

### Workflow Builder Components

#### Canvas Component
- **Purpose**: Main canvas for workflow builder
- **Features**:
  - Zoom and pan
  - Grid background
  - Minimap
  - Node rendering
  - Connection rendering
- **Props**:
  - `workflow`: Workflow definition
  - `onNodeClick`: Node click handler
  - `onConnectionCreate`: Connection creation handler

#### Node Component
- **Purpose**: Individual workflow node
- **Types**: Trigger, Action, Logic, Data nodes
- **Features**:
  - Draggable
  - Selectable
  - Configurable
  - Visual representation
- **Props**:
  - `node`: Node data
  - `selected`: Selection state
  - `onClick`: Click handler
  - `onConfigChange`: Configuration change handler

#### Node Palette Component
- **Purpose**: Sidebar with draggable nodes
- **Features**:
  - Categorized nodes
  - Search functionality
  - Drag to canvas
- **Props**:
  - `onNodeDragStart`: Drag start handler

#### Properties Panel Component
- **Purpose**: Edit node properties
- **Features**:
  - Form fields for node configuration
  - Validation
  - Save/Cancel buttons
- **Props**:
  - `node`: Selected node
  - `onSave`: Save handler
  - `onCancel`: Cancel handler

#### Connection Component
- **Purpose**: Visual connection between nodes
- **Features**:
  - SVG path rendering
  - Hover effects
  - Delete on click
- **Props**:
  - `source`: Source node ID
  - `target`: Target node ID
  - `onDelete`: Delete handler

### Dashboard Components

#### Metric Card Component
- **Purpose**: Display metric value
- **Features**:
  - Value display
  - Label
  - Trend indicator
  - Click to drill down
- **Props**:
  - `label`: Metric label
  - `value`: Metric value
  - `trend`: Trend data
  - `onClick`: Click handler

#### Chart Component
- **Purpose**: Display charts
- **Types**: Line, Bar, Pie charts
- **Features**:
  - Configurable data
  - Interactive
  - Export functionality
- **Props**:
  - `type`: Chart type
  - `data`: Chart data
  - `options`: Chart options

#### Activity Feed Component
- **Purpose**: Display recent activity
- **Features**:
  - List of recent executions
  - Status indicators
  - Click to view details
- **Props**:
  - `activities`: Activity data
  - `onActivityClick`: Click handler

### Analytics Components

#### Filter Panel Component
- **Purpose**: Filter analytics data
- **Features**:
  - Date range picker
  - Workflow selector
  - Channel selector
  - Status selector
- **Props**:
  - `filters`: Current filters
  - `onFilterChange`: Filter change handler

#### Analytics Table Component
- **Purpose**: Display analytics data in table
- **Features**:
  - Sortable columns
  - Pagination
  - Export functionality
- **Props**:
  - `data`: Table data
  - `columns`: Column definitions
  - `onSort`: Sort handler
  - `onExport`: Export handler

### Template Components

#### Template List Component
- **Purpose**: Display list of templates
- **Features**:
  - Search
  - Filter by channel
  - Create new template
  - Edit template
- **Props**:
  - `templates`: Template list
  - `onTemplateClick`: Template click handler

#### Template Editor Component
- **Purpose**: Edit template content
- **Features**:
  - Rich text editor (for email)
  - Variable insertion
  - Preview
  - Save/Cancel
- **Props**:
  - `template`: Template data
  - `onSave`: Save handler
  - `onCancel`: Cancel handler

## Component Patterns

### Container/Presentational Pattern
- **Container Components**: Handle logic and state
- **Presentational Components**: Display UI only

### Compound Components
- **Workflow Builder**: Compound component with sub-components
- **Form**: Compound form components

### Higher-Order Components
- **WithAuth**: Authentication wrapper (future)
- **WithErrorBoundary**: Error boundary wrapper

## Component Props

### Standard Props
- **className**: CSS classes
- **style**: Inline styles
- **children**: Child components
- **onClick**: Click handlers
- **disabled**: Disabled state

### Workflow-Specific Props
- **workflow**: Workflow definition
- **node**: Node data
- **connection**: Connection data
- **onWorkflowChange**: Workflow change handler

## Component State

### Local State
- **useState**: Component-specific state
- **useReducer**: Complex state logic

### Shared State
- **Context**: Shared state via context
- **State Management**: Global state management

## Component Styling

### CSS Modules
- **Scoped Styles**: Component-specific styles
- **Class Names**: BEM or similar naming

### Tailwind CSS
- **Utility Classes**: Tailwind utility classes
- **Custom Classes**: Custom Tailwind classes

### Styled Components (Optional)
- **Dynamic Styling**: Styled-components for dynamic styles

## Component Testing

### Unit Tests
- **Render Tests**: Test component rendering
- **Interaction Tests**: Test user interactions
- **Props Tests**: Test prop handling

### Integration Tests
- **Component Integration**: Test component interactions
- **API Integration**: Test API calls

## Component Documentation

### Common Components

#### LoadingSpinner
Displays a loading spinner with optional text and full-screen overlay.

**Props:**
- `size?: 'sm' | 'md' | 'lg'` - Spinner size
- `text?: string` - Optional text to display
- `fullScreen?: boolean` - Display as full-screen overlay

**Usage:**
```tsx
<LoadingSpinner size="md" text="Loading..." fullScreen />
```

#### LoadingSkeleton
Displays skeleton loaders for different content types.

**Props:**
- `variant?: 'page' | 'card' | 'table' | 'list' | 'form'` - Skeleton variant
- `count?: number` - Number of skeleton items
- `className?: string` - Additional CSS classes

**Usage:**
```tsx
<LoadingSkeleton variant="page" count={3} />
```

#### LoadingOverlay
Overlays a loading indicator on top of content.

**Props:**
- `isLoading: boolean` - Loading state
- `text?: string` - Optional loading text
- `children: ReactNode` - Content to overlay

**Usage:**
```tsx
<LoadingOverlay isLoading={isLoading} text="Loading...">
  <YourContent />
</LoadingOverlay>
```

#### ErrorBoundary
Catches React errors and displays a fallback UI.

**Props:**
- `children: ReactNode` - Child components
- `fallback?: ReactNode` - Custom fallback UI

**Usage:**
```tsx
<ErrorBoundary>
  <YourComponent />
</ErrorBoundary>
```

#### ErrorState
Displays an error message with optional retry button.

**Props:**
- `title?: string` - Error title
- `message?: string` - Error message
- `onRetry?: () => void` - Retry callback
- `className?: string` - Additional CSS classes

**Usage:**
```tsx
<ErrorState 
  title="Something went wrong"
  message="Failed to load data"
  onRetry={() => refetch()}
/>
```

#### EmptyState
Displays a message when no data is available.

**Props:**
- `icon?: LucideIcon` - Optional icon
- `title: string` - Title text
- `description?: string` - Description text
- `action?: { label: string; onClick: () => void }` - Optional action button
- `className?: string` - Additional CSS classes

**Usage:**
```tsx
<EmptyState
  icon={FileText}
  title="No workflows found"
  description="Create your first workflow to get started"
  action={{ label: "Create Workflow", onClick: () => navigate('/workflows/new') }}
/>
```

#### PageHeader
Displays page title, description, and actions.

**Props:**
- `title: string` - Page title
- `description?: string` - Page description
- `actions?: ReactNode` - Action buttons
- `showBreadcrumbs?: boolean` - Show breadcrumbs
- `className?: string` - Additional CSS classes

**Usage:**
```tsx
<PageHeader
  title="Workflows"
  description="Manage your workflows"
  actions={<Button>Create Workflow</Button>}
/>
```

#### MetricCard
Displays a metric value with optional trend indicator.

**Props:**
- `title: string` - Metric title
- `value: string | number` - Metric value
- `description?: string` - Optional description
- `trend?: { value: number; isPositive: boolean }` - Trend data
- `icon?: ReactNode` - Optional icon
- `href?: string` - Optional link URL
- `onClick?: () => void` - Optional click handler

**Usage:**
```tsx
<MetricCard
  title="Total Workflows"
  value={42}
  description="Active workflows"
  trend={{ value: 12, isPositive: true }}
  icon={<Workflow />}
  href="/workflows"
/>
```

### Workflow Builder Components

#### WorkflowCanvas
Main canvas for workflow builder with React Flow.

**Props:**
- `nodes?: Node[]` - Workflow nodes
- `edges?: Edge[]` - Workflow edges
- `onNodesChange?: (changes: NodeChange[]) => void` - Node change handler
- `onEdgesChange?: (changes: EdgeChange[]) => void` - Edge change handler
- `onConnect?: (connection: Connection) => void` - Connection handler
- `onAddNode?: (node: Node) => void` - Add node handler

**Usage:**
```tsx
<WorkflowCanvas
  nodes={nodes}
  edges={edges}
  onNodesChange={handleNodesChange}
  onEdgesChange={handleEdgesChange}
  onConnect={handleConnect}
/>
```

#### NodePalette
Sidebar with draggable workflow nodes.

**Props:**
- `onNodeDragStart?: (event: React.DragEvent, nodeType: string) => void` - Drag start handler
- `onClose?: () => void` - Close handler

**Usage:**
```tsx
<NodePalette onNodeDragStart={handleNodeDragStart} />
```

#### PropertiesPanel
Right sidebar for configuring selected nodes.

**Props:**
- `selectedNode: Node | null` - Selected node
- `nodes?: Node[]` - All nodes
- `onSave: (nodeId: string, config: Record<string, unknown>) => void` - Save handler
- `onCancel: () => void` - Cancel handler
- `onClose?: () => void` - Close handler

**Usage:**
```tsx
<PropertiesPanel
  selectedNode={selectedNode}
  nodes={nodes}
  onSave={handleSave}
  onCancel={handleCancel}
/>
```

### Chart Components

#### ExecutionTrendChart
Line chart displaying execution trends over time.

**Props:**
- `data: ExecutionTrendData[]` - Chart data
- `isLoading?: boolean` - Loading state

**Usage:**
```tsx
<ExecutionTrendChart data={trendData} isLoading={isLoading} />
```

#### DeliveryMetricsChart
Bar chart displaying delivery metrics by channel.

**Props:**
- `data: Array<{ channel: string; sent: number; delivered: number; failed: number }>` - Chart data

**Usage:**
```tsx
<DeliveryMetricsChart data={deliveryData} />
```

#### ChannelDistributionChart
Pie chart displaying channel distribution.

**Props:**
- `data: Array<{ name: string; value: number }>` - Chart data

**Usage:**
```tsx
<ChannelDistributionChart data={distributionData} />
```

## Related Documentation

- [Frontend Overview](./overview.md) - Technology stack
- [Design System](./design-system.json) - UI/UX specs
- [Routing](./routing.md) - Route definitions
- [API Integration](./api-integration.md) - API integration patterns
- [State Management](./state-management.md) - State management patterns
- [Development Guide](./development-guide.md) - Development workflow


