# Workflow Report Feature

## Overview

Each workflow can define its own automated report that executes a custom analyst query on execution data within a specified time period. The query results are generated as a file and sent to business team members at scheduled intervals via cron job. This enables business teams to receive custom analytics reports about workflow execution data without manual intervention.

## Requirements

### Report Configuration

#### Per-Workflow Report
- Each workflow can have **one** report configuration
- Report configuration is optional (workflow can exist without a report)
- Report configuration is tied to the workflow lifecycle
- When workflow is deleted, report configuration is also deleted (or disabled)

#### Report Settings
- **Report Name**: Customizable report name (default: workflow name + "Report")
- **Analyst Query**: Custom SQL query to analyze execution data
  - Query will be executed on execution data for the specified workflow
  - Query can access all execution tables: `executions`, `node_executions`, `execution_wait_states`
  - Query can use PostgreSQL JSONB functions to query field-level data
  - Query should return results in a format suitable for export (CSV, Excel, etc.)
  - Query can use parameters: `:workflow_id`, `:start_date`, `:end_date`
- **Time Period**: Time period for query execution
  - **Period Type**: Fixed period (e.g., last 24 hours, last 7 days, last 30 days)
  - **Custom Period**: Custom date range (start date, end date)
- **Schedule**: Cron expression for report generation
  - Example: `0 9 * * *` (daily at 9:00 AM)
  - Example: `0 9 * * 1` (every Monday at 9:00 AM)
  - Example: `0 9 1 * *` (first day of month at 9:00 AM)
- **Recipients**: List of email addresses for business team members
- **Format**: Report file format
  - CSV (default for query results)
  - Excel (for data analysis with formatting)
  - JSON (for structured data)
- **Timezone**: Timezone for schedule and date calculations (default: system timezone)
- **Status**: Active/Inactive/Paused

### Analyst Query

#### Query Configuration
- **Query Type**: SQL query that analyzes execution data
- **Query Parameters**: 
  - `:workflow_id` - Automatically replaced with workflow ID
  - `:start_date` - Automatically replaced with period start date (TIMESTAMP)
  - `:end_date` - Automatically replaced with period end date (TIMESTAMP)
- **Query Scope**: Query can access:
  - `executions` table - All execution records
  - `node_executions` table - All node execution records
  - `execution_wait_states` table - All wait state records
  - PostgreSQL JSONB functions for field-level queries

#### Query Examples

**Example 1: Execution Summary**
```sql
SELECT 
    COUNT(*) as total_executions,
    COUNT(*) FILTER (WHERE status = 'COMPLETED') as successful_executions,
    COUNT(*) FILTER (WHERE status = 'FAILED') as failed_executions,
    AVG(duration) as avg_duration
FROM executions
WHERE workflow_id = :workflow_id
  AND started_at BETWEEN :start_date AND :end_date;
```

**Example 2: Node Execution Analysis**
```sql
SELECT 
    node_id,
    node_label,
    node_type,
    COUNT(*) as execution_count,
    AVG(duration) as avg_duration,
    COUNT(*) FILTER (WHERE status = 'FAILED') as failed_count
FROM node_executions
WHERE execution_id IN (
    SELECT id FROM executions 
    WHERE workflow_id = :workflow_id 
      AND started_at BETWEEN :start_date AND :end_date
)
GROUP BY node_id, node_label, node_type;
```

**Example 3: Field-Level Query**
```sql
SELECT 
    e.id,
    e.started_at,
    e.context->'nodeOutputs'->'send-email'->>'messageId' as message_id,
    e.context->'nodeOutputs'->'send-email'->>'status' as email_status
FROM executions e
WHERE e.workflow_id = :workflow_id
  AND e.started_at BETWEEN :start_date AND :end_date
  AND e.context->'nodeOutputs'->'send-email'->>'status' IS NOT NULL;
```

**Example 4: Error Analysis**
```sql
SELECT 
    error_details->>'errorType' as error_type,
    error_details->>'nodeId' as failed_node_id,
    COUNT(*) as error_count
FROM executions
WHERE workflow_id = :workflow_id
  AND status = 'FAILED'
  AND started_at BETWEEN :start_date AND :end_date
GROUP BY error_details->>'errorType', error_details->>'nodeId'
ORDER BY error_count DESC;
```

#### Query Validation
- Query must be valid SQL
- Query must use parameterized queries (`:workflow_id`, `:start_date`, `:end_date`)
- Query should return results in tabular format (rows and columns)
- Query execution time should be reasonable (timeout: 5 minutes)
- Query should not modify data (read-only)

### Report Generation

#### Automatic Generation
- Reports are generated automatically based on cron schedule
- Report generation runs as a scheduled background job
- Query is executed with parameters:
  - `:workflow_id` - Set to workflow ID
  - `:start_date` - Set to period start date (based on period type)
  - `:end_date` - Set to period end date (based on period type)

#### Query Execution
- Query is executed as a read-only SQL query
- Query parameters are safely replaced (parameterized query)
- Query results are fetched and stored in memory
- Query execution is logged for monitoring
- Query timeout: 5 minutes (configurable)

#### Report File Generation
- Query results are exported to file format (CSV, Excel, or JSON)
- File includes:
  - Query results (rows and columns from query)
  - Metadata header (optional):
    - Workflow name and ID
    - Report period (start date, end date)
    - Generation timestamp
    - Query executed
- File is stored temporarily for email delivery
- File is deleted after delivery (or retained based on configuration)

#### Period Calculation
- **Fixed Period Types**:
  - Last 24 hours: `start_date = now() - interval '24 hours'`, `end_date = now()`
  - Last 7 days: `start_date = now() - interval '7 days'`, `end_date = now()`
  - Last 30 days: `start_date = now() - interval '30 days'`, `end_date = now()`
  - Last 90 days: `start_date = now() - interval '90 days'`, `end_date = now()`
- **Custom Period**: User specifies start_date and end_date
- Period dates are calculated in the configured timezone

### Report Delivery

#### Email Delivery
- Report file is sent via email to all recipients
- Email includes:
  - Subject: "[Workflow Report] {Workflow Name} - {Report Period}"
  - Body: Brief summary and report attachment
  - Attachment: Generated report file (CSV, Excel, or JSON)
- Email is sent using email service
- Multiple recipients receive the same report

#### Delivery Status (Temporary Implementation)
- **Current Implementation**: Log delivery status to application logs
  - Log when report is generated
  - Log when email is sent (or would be sent)
  - Log recipients
  - Log file path and size
  - Log any errors during generation or delivery
- **Future Implementation**: Track delivery status in database
  - Track delivery status (sent, failed)
  - Log delivery attempts
  - Retry failed deliveries (up to 3 attempts)
  - Notify on delivery failure

### Report Management

#### Configuration Management
- **Create Report Config**: Configure report for workflow
- **Edit Report Config**: Update report settings
- **View Report Config**: View current configuration
- **Delete Report Config**: Remove report configuration
- **Enable/Disable**: Activate or deactivate report

#### Report History
- **View Generated Reports**: List of all generated reports
- **Report Details**: View report generation details
  - Generation timestamp
  - Report period
  - Recipients
  - Delivery status
  - File size
- **Download Reports**: Download previously generated reports
- **Report Retention**: Keep generated reports for 3 months (configurable)

### Integration with Workflow Dashboard

#### Dashboard Integration
- Report configuration accessible from workflow dashboard
- "Report Settings" section in dashboard
- Quick access to:
  - Configure report
  - View report history
  - Download recent reports
  - Test report generation

#### Report Preview
- Preview report before scheduling
- Test report generation with sample data
- Validate report configuration

## User Experience

### For Business Teams
- **Automatic Reports**: Receive reports automatically without requesting
- **Consistent Format**: Reports follow consistent format and structure
- **Easy to Understand**: Reports use clear language and visualizations
- **Actionable Insights**: Reports include recommendations and insights

### For Technical Users (Developers)
- **Easy Configuration**: Simple form to configure report
- **Flexible Scheduling**: Support for various schedule options
- **Customizable Content**: Choose what to include in report
- **Monitoring**: Track report generation and delivery

## Technical Requirements

### Database Schema
- New table: `workflow_reports` to store report configurations
- Fields:
  - `id`: Primary key (UUID)
  - `workflow_id`: Reference to workflow (VARCHAR, NOT NULL, FOREIGN KEY)
  - `name`: Report name (VARCHAR, NOT NULL)
  - `analyst_query`: SQL query for analysis (TEXT, NOT NULL)
  - `period_type`: Fixed period type (VARCHAR: 'last_24h', 'last_7d', 'last_30d', 'last_90d', 'custom')
  - `period_start_date`: Custom period start date (TIMESTAMP, nullable)
  - `period_end_date`: Custom period end date (TIMESTAMP, nullable)
  - `schedule_cron`: Cron expression for scheduling (VARCHAR, NOT NULL)
  - `recipients`: Array of email addresses (TEXT[], NOT NULL)
  - `format`: File format (VARCHAR: 'csv', 'excel', 'json', default: 'csv')
  - `timezone`: Timezone for schedule and date calculations (VARCHAR, default: 'UTC')
  - `status`: Report status (VARCHAR: 'active', 'inactive', 'paused', default: 'active')
  - `last_generated_at`: Last generation timestamp (TIMESTAMP, nullable)
  - `next_generation_at`: Next scheduled generation (TIMESTAMP, nullable)
  - `last_generation_status`: Last generation status (VARCHAR: 'success', 'failed', nullable)
  - `last_generation_error`: Last generation error message (TEXT, nullable)
  - `generation_count`: Total number of generations (INTEGER, default: 0)
  - `created_at`: Creation timestamp (TIMESTAMP, NOT NULL, default: NOW())
  - `updated_at`: Last update timestamp (TIMESTAMP, NOT NULL, default: NOW())
  - `deleted_at`: Soft delete timestamp (TIMESTAMP, nullable)

### Backend
- **Service Layer**: WorkflowReportService
  - Create/update/delete report configuration
  - Validate analyst query
  - Execute analyst query with parameters
  - Generate report file from query results
  - Schedule report generation (cron job)
  - Send report via email (or log for temporary implementation)
- **Scheduled Job**: Background job (cron) to generate and send reports
  - Execute query with calculated period dates
  - Generate file from query results
  - Send email (or log delivery)
- **Query Executor**: Execute SQL queries safely
  - Parameter replacement (`:workflow_id`, `:start_date`, `:end_date`)
  - Query validation (read-only, timeout)
  - Error handling
- **File Generator**: Generate CSV/Excel/JSON files from query results
- **Email Integration**: Send reports via email service (or log for temporary implementation)

### Frontend
- **Report Configuration UI**: Form to configure report
  - Query editor (SQL textarea with syntax highlighting)
  - Period type selector
  - Cron expression input/helper
  - Recipients input
  - Format selector
  - Timezone selector
- **Query Validation UI**: Validate query before saving
- **Report History UI**: List of generated reports
- **Report Preview**: Preview query results before scheduling
  - Execute query with sample parameters
  - Display query results preview
- **Integration**: Add to workflow dashboard

### API Endpoints
- `GET /workflows/{workflow_id}/report` - Get report configuration
- `POST /workflows/{workflow_id}/report` - Create report configuration
  - Request body includes: `name`, `analyst_query`, `period_type`, `schedule_cron`, `recipients`, `format`, `timezone`
- `PUT /workflows/{workflow_id}/report` - Update report configuration
- `DELETE /workflows/{workflow_id}/report` - Delete report configuration
- `POST /workflows/{workflow_id}/report/generate` - Manually generate report
  - Optional request body: `period_start`, `period_end` (for custom period)
- `POST /workflows/{workflow_id}/report/preview` - Preview query results
  - Request body: `period_start`, `period_end` (optional, defaults to last 7 days)
  - Returns: Query results preview (limited rows)
- `POST /workflows/{workflow_id}/report/validate` - Validate analyst query
  - Request body: `analyst_query`
  - Returns: Validation result (valid/invalid, error message if invalid)
- `GET /workflows/{workflow_id}/report/history` - Get report history
- `GET /workflows/{workflow_id}/report/history/{report_id}/download` - Download report file

## Data Model

See [Database Schema - Workflow Reports](../database-schema/entities.md#workflow-reports)

## API Endpoints

See [API - Workflow Reports](../api/endpoints.md#workflow-reports)

## Query Capabilities

Reports leverage PostgreSQL JSONB functions to query execution data. Users can write custom SQL queries to analyze execution data in any way they need.

### Available Tables
- **executions**: All workflow execution records
- **node_executions**: All node execution records
- **execution_wait_states**: All wait state records

### Query Parameters
- `:workflow_id` - Automatically replaced with workflow ID
- `:start_date` - Automatically replaced with period start date (TIMESTAMP)
- `:end_date` - Automatically replaced with period end date (TIMESTAMP)

### Query Capabilities
- **Field-Level Queries**: Query specific fields within JSONB structures using PostgreSQL JSONB operators
- **Node Data Queries**: Query input/output data from individual nodes
- **Aggregation**: Aggregate data across executions and nodes
- **Custom Metrics**: Calculate custom metrics based on execution data
- **Complex Joins**: Join between executions, node_executions, and other tables
- **Time-based Analysis**: Filter and group by time periods

### Example Report Queries

See [Execution Query Capabilities](./execution-query-capabilities.md) for detailed query examples and patterns.

**Note**: All queries must use parameterized parameters (`:workflow_id`, `:start_date`, `:end_date`) for security and correctness.

## Related Features

- [Execution Data Structure](./execution-data-structure.md) - Execution data structure details
- [Execution Query Capabilities](./execution-query-capabilities.md) - Query capabilities using PostgreSQL JSON functions
- [Workflow Dashboard](./workflow-dashboard.md) - Dashboard integration
- [Analytics](./analytics.md) - Analytics data for reports
- [Report Scheduling](./analytics.md#reports) - General report scheduling

## User Flows

See [User Flows - Workflow Report Configuration](../user-flows/workflow-report-configuration.md)





