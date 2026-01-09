# Sprint 21: Workflow Report Service

## Goal
Implement workflow report service for automated report generation and scheduling.

## Phase
Analytics & Reporting

## Complexity
Medium

## Dependencies
Sprint 20

## Compliance Check

### Before Starting
1. ✅ Read `@import(features/workflow-report.md)` - Understand report requirements
2. ✅ Read `@import(api/endpoints.md#workflow-reports)` - Understand report API
3. ✅ Read `@import(api/schemas.md)` - Understand request/response schemas
4. ✅ Verify Sprint 20 is completed

## Tasks

### Report Service
- [ ] Create `WorkflowReportService.java`:
  - `getReportConfig()` - Get report configuration
  - `createReportConfig()` - Create report configuration
  - `updateReportConfig()` - Update report configuration
  - `deleteReportConfig()` - Delete report configuration
  - `validateQuery()` - Validate analyst query
  - `generateReport()` - Generate report manually
  - `previewReport()` - Preview query results
  - See `@import(features/workflow-report.md)`

### Query Executor
- [ ] Create `ReportQueryExecutor.java`:
  - `executeQuery()` - Execute analyst query with parameters
  - `replaceParameters()` - Replace `:workflow_id`, `:start_date`, `:end_date` safely
  - `validateQuery()` - Validate query (read-only, syntax check)
  - `calculatePeriodDates()` - Calculate period dates based on period_type
  - Handle query timeout (5 minutes)
  - Handle query errors

### Report Generator
- [ ] Create `ReportGenerator.java`:
  - `generateCsvReport()` - Generate CSV file from query results
  - `generateExcelReport()` - Generate Excel file from query results
  - `generateJsonReport()` - Generate JSON file from query results
  - Add metadata header (optional): workflow name, period, timestamp

### Report Scheduler
- [ ] Create `ReportSchedulerService.java`:
  - `scheduleReport()` - Schedule report generation using cron
  - `processScheduledReports()` - Scheduled task to generate reports
  - Calculate period dates based on period_type
  - Execute query with parameters
  - Generate file from query results
  - Send email (or log for temporary implementation)
  - Support timezone handling

### Report Controller
- [ ] Create `WorkflowReportController.java`:
  - `GET /workflows/{id}/report` - Get report config
  - `POST /workflows/{id}/report` - Create report config
  - `PUT /workflows/{id}/report` - Update report config
  - `DELETE /workflows/{id}/report` - Delete report config
  - `POST /workflows/{id}/report/validate` - Validate analyst query
  - `POST /workflows/{id}/report/generate` - Generate report manually
  - `POST /workflows/{id}/report/preview` - Preview query results
  - `GET /workflows/{id}/report/history` - Get report history
  - `GET /workflows/{id}/report/history/{reportId}/download` - Download report
  - `PATCH /workflows/{id}/report/status` - Update report status
  - **MUST MATCH**: `@import(api/endpoints.md#workflow-reports)`

### Report Delivery (Temporary Implementation)
- [ ] Implement logging for report delivery:
  - Log when report is generated
  - Log when email would be sent (or is sent)
  - Log recipients
  - Log file path and size
  - Log any errors during generation or delivery
- [ ] Store report files temporarily
- [ ] **Note**: Full email delivery implementation deferred (currently log only)

## Deliverables

- ✅ Workflow report service fully implemented
- ✅ Report generation working (PDF, Excel, CSV)
- ✅ Report scheduling working
- ✅ Report delivery working

## Technical Details

### Report API
- **Endpoints**: `@import(api/endpoints.md#workflow-reports)` ⚠️ **MUST MATCH**
- **Schemas**: `@import(api/schemas.md)` ⚠️ **MUST MATCH**

### Report Features
- **Features**: `@import(features/workflow-report.md)` ⚠️ **MUST MATCH**

## Compliance Verification

- [ ] Verify report API matches `@import(api/endpoints.md#workflow-reports)`
- [ ] Test report generation for all formats
- [ ] Test report scheduling

## Related Documentation

- `@import(features/workflow-report.md)` ⚠️ **MUST MATCH**
- `@import(api/endpoints.md#workflow-reports)` ⚠️ **MUST MATCH**
- `@import(api/schemas.md)` ⚠️ **MUST MATCH**

