# Sprint 20: Workflow Dashboard API

## Goal
Implement workflow dashboard API for per-workflow monitoring and analytics.

## Phase
Analytics & Reporting

## Complexity
Medium

## Dependencies
Sprint 19

## Compliance Check

### Before Starting
1. ✅ Read `@import(features/workflow-dashboard.md)` - Understand dashboard requirements
2. ✅ Read `@import(api/endpoints.md#workflow-dashboard)` - Understand dashboard API
3. ✅ Read `@import(api/schemas.md)` - Understand request/response schemas
4. ✅ Verify Sprint 19 is completed

## Tasks

### Dashboard Service
- [ ] Create `WorkflowDashboardService.java`:
  - `getDashboardOverview()` - Get dashboard overview
  - `getExecutionTrends()` - Get execution trends
  - `getNodePerformance()` - Get node performance
  - `getChannelPerformance()` - Get channel performance
  - `getExecutionHistory()` - Get execution history
  - `getErrorAnalysis()` - Get error analysis
  - See `@import(features/workflow-dashboard.md)`

### Dashboard Controller
- [ ] Create `WorkflowDashboardController.java`:
  - `GET /workflows/{id}/dashboard` - Get dashboard overview
  - `GET /workflows/{id}/dashboard/trends` - Get execution trends
  - `GET /workflows/{id}/dashboard/nodes` - Get node performance
  - `GET /workflows/{id}/dashboard/channels` - Get channel performance
  - `GET /workflows/{id}/dashboard/executions` - Get execution history
  - `GET /workflows/{id}/dashboard/errors` - Get error analysis
  - **MUST MATCH**: `@import(api/endpoints.md#workflow-dashboard)`

### Dashboard DTOs
- [ ] Create `WorkflowDashboardDTO.java`
- [ ] Create `ExecutionTrendDTO.java`
- [ ] Create `NodePerformanceDTO.java`
- [ ] Create `ChannelPerformanceDTO.java`
- [ ] Create `WorkflowErrorAnalysisDTO.java`

### Metrics Calculation
- [ ] Calculate success rates
- [ ] Calculate execution time averages
- [ ] Calculate trends (change percentages)
- [ ] Calculate error rates

## Deliverables

- ✅ Workflow dashboard API fully implemented
- ✅ Dashboard metrics calculation working
- ✅ Dashboard endpoints match specifications

## Technical Details

### Dashboard API
- **Endpoints**: `@import(api/endpoints.md#workflow-dashboard)` ⚠️ **MUST MATCH**
- **Schemas**: `@import(api/schemas.md)` ⚠️ **MUST MATCH**

### Dashboard Features
See `@import(features/workflow-dashboard.md)`.

## Compliance Verification

- [ ] Verify dashboard API matches `@import(api/endpoints.md#workflow-dashboard)`
- [ ] Test dashboard overview endpoint
- [ ] Test all dashboard endpoints

## Related Documentation

- `@import(features/workflow-dashboard.md)` ⚠️ **MUST MATCH**
- `@import(api/endpoints.md#workflow-dashboard)` ⚠️ **MUST MATCH**
- `@import(api/schemas.md)` ⚠️ **MUST MATCH**

