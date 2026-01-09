# Analytics Viewing User Flow

## Overview

This document describes the user flow for viewing analytics and reports.

## User Journey

### 1. Access Analytics
- **Entry Points**:
  - Dashboard → "Analytics" link
  - Workflow details → "View Analytics" button
  - Navigation menu → "Analytics"
- **User Action**: Click "Analytics"

### 2. Select Analytics View
- **Screen**: Analytics dashboard
- **Options**:
  - **Overview**: High-level metrics
  - **Workflow Analytics**: Per-workflow analytics
  - **Channel Analytics**: Per-channel analytics
  - **Delivery Analytics**: Delivery metrics
  - **Error Analytics**: Error metrics
- **User Action**: Select view

### 3. Configure Filters
- **Screen**: Analytics view with filters
- **User Actions**:
  - Select date range:
    - Today
    - Last 7 days
    - Last 30 days
    - Custom range
  - Select workflow (if applicable)
  - Select channel (if applicable)
  - Select metric type
  - Click "Apply Filters"
- **System Actions**:
  - Validate filters
  - Load filtered data
  - Display results

### 4. View Metrics
- **Screen**: Analytics view with metrics
- **Displayed Metrics**:
  - **Overview**:
    - Total executions
    - Total notifications sent
    - Delivery rate
    - Error rate
  - **Workflow Analytics**:
    - Executions per workflow
    - Success rate per workflow
    - Average execution time
  - **Channel Analytics**:
    - Sent per channel
    - Delivery rate per channel
    - Error rate per channel
  - **Delivery Analytics**:
    - Total sent
    - Total delivered
    - Delivery rate
    - Time-based breakdown
- **User Actions**:
  - View charts and graphs
  - Hover for details
  - Click for drill-down

### 5. View Charts
- **Screen**: Analytics view with charts
- **Chart Types**:
  - **Line Charts**: Trends over time
  - **Bar Charts**: Comparisons
  - **Pie Charts**: Distributions
  - **Tables**: Detailed data
- **User Actions**:
  - View different chart types
  - Zoom in/out
  - Export chart

### 6. Drill Down
- **Screen**: Detailed view
- **User Actions**:
  - Click on metric or chart element
  - View detailed breakdown
  - Navigate to related data
- **System Actions**:
  - Load detailed data
  - Display breakdown
  - Show related metrics

### 7. Export Data
- **Screen**: Analytics view
- **User Actions**:
  - Click "Export" button
  - Select format:
    - CSV
    - PDF
    - Excel
  - Select data range
  - Click "Download"
- **System Actions**:
  - Generate export file
  - Download file

### 8. Schedule Report (Optional)
- **Screen**: Analytics view
- **User Actions**:
  - Click "Schedule Report"
  - Configure schedule:
    - Frequency (daily, weekly, monthly)
    - Recipients
    - Format
  - Click "Save Schedule"
- **System Actions**:
  - Save report schedule
  - Schedule report generation
  - Send reports via email

## Analytics Views

### Overview Dashboard
- **Key Metrics**: High-level KPIs
- **Recent Activity**: Recent executions
- **System Health**: Overall system status
- **Quick Actions**: Common actions

### Workflow Analytics
- **Workflow List**: List of workflows with metrics
- **Workflow Details**: Detailed workflow analytics
- **Execution History**: Historical executions
- **Performance Metrics**: Workflow performance

### Channel Analytics
- **Channel Status**: Status of all channels
- **Channel Performance**: Performance metrics per channel
- **Channel Health**: Health monitoring
- **Provider Status**: Third-party provider status

### Delivery Analytics
- **Delivery Metrics**: Overall delivery metrics
- **Time-based Breakdown**: Delivery by time period
- **Channel Breakdown**: Delivery by channel
- **Recipient Breakdown**: Delivery by recipient (optional)

### Error Analytics
- **Error Types**: Errors by type
- **Error Trends**: Error trends over time
- **Error Distribution**: Error distribution
- **Recovery Time**: Time to recover from errors

## Filtering and Search

### Date Range
- **Presets**: Today, Last 7 days, Last 30 days, Custom
- **Custom Range**: Start date and end date
- **Timezone**: Timezone selection

### Workflow Filter
- **All Workflows**: All workflows
- **Specific Workflow**: Single workflow
- **Multiple Workflows**: Multiple workflows
- **Search**: Search by name

### Channel Filter
- **All Channels**: All channels
- **Specific Channel**: Single channel
- **Multiple Channels**: Multiple channels

### Status Filter
- **All Statuses**: All statuses
- **Specific Status**: Single status
- **Multiple Statuses**: Multiple statuses

## Data Refresh

### Manual Refresh
- **User Action**: Click "Refresh" button
- **System Action**: Reload data

### Auto Refresh (Optional)
- **Interval**: Configurable refresh interval
- **System Action**: Auto-reload data

## Related Documentation

- [Workflow Creation Flow](./workflow-creation.md)
- [Notification Delivery Flow](./notification-delivery.md)
- [Analytics Feature](../features/analytics.md)


