# Sprint 14: Workflow Dashboard UI

## Goal
Implement per-workflow dashboard UI with metrics and charts, ensuring compliance with workflow dashboard API.

## Phase
Analytics & Polish

## Complexity
Medium

## Dependencies
Sprint 12

## Compliance Check

### Before Starting
1. ✅ Read `@import(features/workflow-dashboard.md)` - Understand workflow dashboard
2. ✅ Read `@import(api/endpoints.md#workflow-dashboard)` - Understand dashboard API
3. ✅ Verify Sprint 12 is completed

## Tasks

### Workflow Dashboard Page
- [ ] Create `WorkflowDashboard.tsx` page
- [ ] Implement dashboard layout:
  - [ ] Header section (workflow name, status, last execution)
  - [ ] Metrics overview section
  - [ ] Charts section
  - [ ] Execution history section
  - [ ] Error analysis section

### Metrics Display
- [ ] Display KPIs:
  - [ ] Total executions
  - [ ] Success rate
  - [ ] Average execution time
  - [ ] Total actions executed
  - [ ] Error rate
- [ ] Display trend indicators
- [ ] Display comparison with previous period

### Charts
- [ ] Execution trends chart
- [ ] Node performance chart
- [ ] Channel performance chart
- [ ] Error timeline chart

### Data Fetching
- [ ] Implement dashboard API calls:
  - [ ] `GET /workflows/{id}/dashboard`
  - [ ] `GET /workflows/{id}/dashboard/trends`
  - [ ] `GET /workflows/{id}/dashboard/nodes`
  - [ ] `GET /workflows/{id}/dashboard/channels`
  - [ ] `GET /workflows/{id}/dashboard/executions`
  - [ ] `GET /workflows/{id}/dashboard/errors`

### Filters
- [ ] Date range filter
- [ ] Timezone selector
- [ ] Status filter

## Deliverables

- ✅ Workflow dashboard implemented
- ✅ Metrics and charts displaying
- ✅ All dashboard endpoints integrated

## Technical Details

### Workflow Dashboard API
- **Endpoints**: `@import(api/endpoints.md#workflow-dashboard)` ⚠️ **MUST MATCH**

### Dashboard Features
- **Features**: `@import(features/workflow-dashboard.md)` ⚠️ **MUST MATCH**

## Compliance Verification

- [ ] Verify dashboard matches `@import(features/workflow-dashboard.md)`
- [ ] Verify API calls match `@import(api/endpoints.md#workflow-dashboard)`
- [ ] Test all dashboard sections

## Related Documentation

- `@import(features/workflow-dashboard.md)` ⚠️ **MUST MATCH**
- `@import(api/endpoints.md#workflow-dashboard)` ⚠️ **MUST MATCH**

