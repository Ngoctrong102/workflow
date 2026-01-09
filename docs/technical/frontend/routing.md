# Frontend Routing

## Route Structure

### Main Routes
```
/                          → Dashboard
/workflows                 → Workflow List
/workflows/new             → Create Workflow
/workflows/:id             → Workflow Details
/workflows/:id/edit        → Edit Workflow
/templates                 → Template List
/templates/new             → Create Template
/templates/:id             → Template Details
/templates/:id/edit        → Edit Template
/analytics                 → Analytics Dashboard
/analytics/workflows/:id   → Workflow Analytics
/analytics/channels        → Channel Analytics
/channels                  → Channel List
/channels/new              → Create Channel
/channels/:id              → Channel Details
/channels/:id/edit         → Edit Channel
/executions                → Execution List
/executions/:id            → Execution Details
```

## Route Components

### Dashboard Route
- **Path**: `/`
- **Component**: `Dashboard`
- **Features**: Overview, quick stats, recent activity

### Workflow Routes
- **Path**: `/workflows`
- **Components**:
  - `WorkflowList`: List of workflows
  - `WorkflowBuilder`: Workflow builder interface
  - `WorkflowDetails`: Workflow details and analytics

### Template Routes
- **Path**: `/templates`
- **Components**:
  - `TemplateList`: List of templates
  - `TemplateEditor`: Template editor
  - `TemplateDetails`: Template details

### Analytics Routes
- **Path**: `/analytics`
- **Components**:
  - `AnalyticsDashboard`: Main analytics dashboard
  - `WorkflowAnalytics`: Workflow-specific analytics
  - `ChannelAnalytics`: Channel-specific analytics

### Channel Routes
- **Path**: `/channels`
- **Components**:
  - `ChannelList`: List of channels
  - `ChannelEditor`: Channel configuration
  - `ChannelDetails`: Channel details

### Execution Routes
- **Path**: `/executions`
- **Components**:
  - `ExecutionList`: List of executions
  - `ExecutionDetails`: Execution details and logs

## Route Guards (Future)

### Authentication Guard
- **Purpose**: Protect routes requiring authentication
- **Implementation**: Route guard component
- **Redirect**: Redirect to login if not authenticated

### Authorization Guard
- **Purpose**: Protect routes based on permissions
- **Implementation**: Permission-based route guard
- **Redirect**: Redirect if insufficient permissions

## Navigation

### Navigation Menu
- **Dashboard**: Link to dashboard
- **Workflows**: Link to workflows
- **Templates**: Link to templates
- **Analytics**: Link to analytics
- **Channels**: Link to channels

### Breadcrumbs
- **Purpose**: Show navigation path
- **Implementation**: Breadcrumb component
- **Usage**: Show on detail pages

## Route Parameters

### Workflow ID
- **Parameter**: `:id`
- **Type**: String (UUID or similar)
- **Usage**: Identify specific workflow

### Template ID
- **Parameter**: `:id`
- **Type**: String
- **Usage**: Identify specific template

### Channel ID
- **Parameter**: `:id`
- **Type**: String
- **Usage**: Identify specific channel

### Execution ID
- **Parameter**: `:id`
- **Type**: String
- **Usage**: Identify specific execution

## Query Parameters

### Filters
- **status**: Filter by status
- **channel**: Filter by channel
- **start_date**: Start date for date range
- **end_date**: End date for date range

### Pagination
- **page**: Page number
- **limit**: Items per page

### Search
- **q**: Search query
- **type**: Search type

## Route Transitions

### Loading States
- **Suspense**: Use React Suspense for loading
- **Loading Component**: Show loading indicator
- **Error Boundary**: Handle route errors

### Navigation Transitions
- **Smooth Transitions**: Animate route changes
- **Preserve State**: Preserve component state where appropriate

## Related Documentation

- [Frontend Overview](./overview.md) - Technology stack
- [Components](./components.md) - Component specs
- [Implementation Guide](./implementation-guide.md) - Implementation details


