# Analytics Feature

## Overview

Analytics provides comprehensive tracking, reporting, and insights into workflow execution, action execution, and system performance. Historical data is retained for 6 months.

**Query Capabilities**: All execution data is stored in PostgreSQL JSONB columns, enabling powerful querying of individual fields within input/output data of each node. See [Execution Query Capabilities](./execution-query-capabilities.md) for detailed query examples.

## Analytics Categories

### 1. Workflow Analytics

#### Execution Metrics
- **Total Executions**: Total workflow executions
- **Successful Executions**: Successful completions
- **Failed Executions**: Failed executions
- **Average Execution Time**: Average time to complete workflow
- **Execution Rate**: Executions per time period

#### Workflow Performance
- **Most Used Workflows**: Top workflows by execution count
- **Slowest Workflows**: Workflows with longest execution time
- **Error-prone Workflows**: Workflows with highest failure rate
- **Workflow Trends**: Execution trends over time

### 2. Action Execution Analytics

#### Metrics
- **Total Actions Executed**: Total actions executed across all workflows
- **Successful Actions**: Successfully completed actions
- **Failed Actions**: Failed action executions
- **Action Success Rate**: Percentage of successful actions
- **Average Action Time**: Average time to execute actions

#### Action Type Breakdown
- **Per Action Type**: Execution metrics per action type (API Call, Publish Event, Function, Custom Actions)
- **Action Type Comparison**: Compare performance across action types
- **Action Health**: Monitor action status and errors

#### Time-based Analysis
- **Hourly**: Action execution metrics by hour
- **Daily**: Action execution metrics by day
- **Weekly**: Action execution metrics by week
- **Monthly**: Action execution metrics by month

### 3. Node Execution Analytics

#### Metrics
- **Total Node Executions**: Total node executions across all workflows
- **Successful Node Executions**: Successfully completed node executions
- **Failed Node Executions**: Failed node executions
- **Node Success Rate**: Percentage of successful node executions
- **Average Node Execution Time**: Average time to execute nodes

#### Node Type Breakdown
- **Per Node Type**: Execution metrics per node type (Trigger, Logic, Action)
- **Per Node Sub-Type**: Execution metrics per node sub-type (API Call, Condition, Delay, etc.)
- **Node Type Comparison**: Compare performance across node types

#### Time-based Analysis
- **Hourly**: Node execution metrics by hour
- **Daily**: Node execution metrics by day
- **Weekly**: Node execution metrics by week
- **Monthly**: Node execution metrics by month

### 4. Error Analytics

#### Error Types
- **Configuration Errors**: Invalid workflow/node configuration
- **Action Execution Errors**: Action execution failures
- **Network Errors**: Connection failures
- **API Errors**: External API errors
- **Timeout Errors**: Request timeouts
- **Node Execution Errors**: Node execution failures

#### Error Trends
- **Error Rate**: Percentage of errors
- **Error Distribution**: Errors by type
- **Error Timeline**: Error trends over time
- **Recovery Time**: Time to recover from errors

## Dashboard

### Overview Dashboard
- **Key Metrics**: High-level KPIs
- **Recent Activity**: Recent workflow executions
- **System Health**: Overall system status
- **Quick Actions**: Common actions

### Workflow Dashboard
- **Workflow List**: List of all workflows
- **Workflow Details**: Detailed workflow analytics
- **Execution History**: Historical executions
- **Performance Metrics**: Workflow performance

### Per-Workflow Dashboard
Each workflow has its own dedicated monitoring dashboard. See [Workflow Dashboard Feature](./workflow-dashboard.md) for detailed requirements.

### Channel Dashboard
- **Channel Status**: Status of all notification channels
- **Channel Performance**: Performance metrics per channel
- **Channel Health**: Health monitoring
- **Provider Status**: Third-party provider status

## Reports

### Standard Reports
- **Daily Report**: Daily delivery summary
- **Weekly Report**: Weekly performance summary
- **Monthly Report**: Monthly analytics report
- **Custom Reports**: User-defined reports

### Report Features
- **Export**: Export reports as CSV, PDF, Excel
- **Scheduling**: Schedule automatic report generation
- **Email Delivery**: Email reports to recipients
- **Customization**: Customize report content

## Data Retention

### Retention Policy
- **Historical Data**: 6 months retention
- **Aggregated Data**: Keep aggregated data longer (optional)
- **Archival**: Archive old data (optional for MVP)

### Data Cleanup
- **Automatic Cleanup**: Remove data older than 6 months
- **Manual Cleanup**: Manual data deletion
- **Backup**: Backup before cleanup (optional)

## Logs

### Delivery Logs
- **Log Entry**: Each delivery attempt logged
- **Log Details**: Status, timestamp, error messages
- **Log Search**: Search logs by criteria
- **Log Export**: Export logs for analysis

### Execution Logs
- **Workflow Execution**: Log each workflow execution
- **Node Execution**: Log each node execution
- **Data Flow**: Log data transformations
- **Error Logs**: Detailed error logs

## Real-time Monitoring

### Live Metrics (Not Required for MVP)
- Real-time delivery status
- Live execution monitoring
- Real-time error alerts

### MVP Alternative
- **Periodic Updates**: Refresh metrics periodically
- **Manual Refresh**: User-triggered refresh
- **Scheduled Updates**: Scheduled metric updates

## Data Model

See [Database Schema - Analytics](../database-schema/entities.md#analytics)

## API Endpoints

See [API - Analytics](../api/endpoints.md#analytics)

## Related Features

- [Workflow Builder](./workflow-builder.md) - Analytics for workflows
- [Workflow Dashboard](./workflow-dashboard.md) - Per-workflow monitoring dashboard


