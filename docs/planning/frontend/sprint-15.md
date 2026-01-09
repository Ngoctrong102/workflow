# Sprint 15: Workflow Report UI

## Goal
Implement workflow report configuration and management UI, ensuring compliance with workflow report API.

## Phase
Analytics & Polish

## Complexity
Medium

## Dependencies
Sprint 14

## Compliance Check

### Before Starting
1. ✅ Read `@import(features/workflow-report.md)` - Understand workflow report
2. ✅ Read `@import(api/endpoints.md#workflow-reports)` - Understand report API
3. ✅ Verify Sprint 14 is completed

## Tasks

### Report Configuration UI
- [ ] Create report configuration form:
  - [ ] Report name input
  - [ ] Analyst query editor:
    - [ ] SQL textarea with syntax highlighting
    - [ ] Query parameter hints (`:workflow_id`, `:start_date`, `:end_date`)
    - [ ] Query validation button
    - [ ] Query validation feedback
  - [ ] Period type selector:
    - [ ] Fixed periods: Last 24h, Last 7d, Last 30d, Last 90d
    - [ ] Custom period: Start date, End date inputs
  - [ ] Schedule cron expression input:
    - [ ] Cron expression input field
    - [ ] Cron helper/validator
    - [ ] Common cron presets (daily, weekly, monthly)
  - [ ] Recipients input (email list)
  - [ ] Format selector (CSV, Excel, JSON)
  - [ ] Timezone selector
- [ ] Implement form validation:
  - [ ] Validate analyst query syntax
  - [ ] Validate cron expression
  - [ ] Validate period dates (if custom)
- [ ] Implement query validation:
  - [ ] `POST /workflows/{id}/report/validate`
  - [ ] Display validation result
  - [ ] Show error messages if invalid
- [ ] Implement report CRUD:
  - [ ] Create: `POST /workflows/{id}/report`
  - [ ] Update: `PUT /workflows/{id}/report`
  - [ ] Delete: `DELETE /workflows/{id}/report`
  - [ ] Get: `GET /workflows/{id}/report`

### Report Management
- [ ] Display report configuration in workflow dashboard
- [ ] Display report status (active, inactive, paused)
- [ ] Display next generation time
- [ ] Display last generation status
- [ ] Add report actions:
  - [ ] Configure report
  - [ ] Edit report
  - [ ] Pause/Resume report
  - [ ] Delete report

### Report History
- [ ] Create report history list:
  - [ ] Fetch: `GET /workflows/{id}/report/history`
  - [ ] Display generated reports
  - [ ] Display report period, format, size
  - [ ] Display delivery status
- [ ] Implement report download:
  - [ ] `GET /workflows/{id}/report/history/{reportId}/download`

### Report Preview
- [ ] Implement query preview:
  - [ ] `POST /workflows/{id}/report/preview`
  - [ ] Display query results preview (table)
  - [ ] Show row count and execution time
  - [ ] Allow period date selection for preview
  - [ ] Display query parameters used
- [ ] Implement manual report generation:
  - [ ] `POST /workflows/{id}/report/generate`
  - [ ] Optional period date selection
  - [ ] Show generation status

## Deliverables

- ✅ Report configuration UI implemented
- ✅ Report management working
- ✅ Report history working

## Technical Details

### Workflow Report API
- **Endpoints**: `@import(api/endpoints.md#workflow-reports)` ⚠️ **MUST MATCH**

### Report Features
- **Features**: `@import(features/workflow-report.md)` ⚠️ **MUST MATCH**

## Compliance Verification

- [ ] Verify report UI matches `@import(features/workflow-report.md)`
- [ ] Verify API calls match `@import(api/endpoints.md#workflow-reports)`
- [ ] Test report configuration

## Related Documentation

- `@import(features/workflow-report.md)` ⚠️ **MUST MATCH**
- `@import(api/endpoints.md#workflow-reports)` ⚠️ **MUST MATCH**

