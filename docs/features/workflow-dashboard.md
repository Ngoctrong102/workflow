# Workflow Dashboard Feature

## Overview

Each workflow has its own dedicated monitoring dashboard that provides real-time insights, performance metrics, and execution details specific to that workflow. This enables users to monitor, analyze, and optimize individual workflows independently.

## Requirements

### Dashboard Access

#### Entry Points
- **Workflow List**: "View Dashboard" button/link for each workflow
- **Workflow Details**: "Dashboard" tab in workflow detail view
- **Direct URL**: `/workflows/{workflow_id}/dashboard`
- **Navigation**: Breadcrumb navigation from workflow details

#### Access Control
- Dashboard is accessible for any workflow (active, inactive, paused, archived)
- Dashboard shows data based on workflow's execution history
- Empty state when workflow has no executions

### Dashboard Layout

#### Header Section
- **Workflow Name**: Display workflow name
- **Workflow Status**: Status badge (active, inactive, paused, archived)
- **Last Execution**: Timestamp of last execution
- **Quick Actions**:
  - View Workflow Details
  - Edit Workflow
  - View Execution History
  - Export Dashboard Data
  - Configure Report (if report not configured)
  - View Report Settings (if report configured)

#### Metrics Overview Section
- **Key Performance Indicators (KPIs)**:
  - Total Executions: Total number of workflow executions
  - Success Rate: Percentage of successful executions
  - Average Execution Time: Average time to complete workflow
  - Total Notifications Sent: Total notifications sent by this workflow
  - Delivery Rate: Percentage of successfully delivered notifications
  - Error Rate: Percentage of failed executions
- **Trend Indicators**: Up/down arrows showing change from previous period
- **Comparison Period**: Compare with previous period (day, week, month)

#### Charts Section

##### Execution Trends Chart
- **Type**: Line chart
- **Metrics**: 
  - Total executions over time
  - Successful executions
  - Failed executions
- **Time Range**: Configurable (last 24 hours, 7 days, 30 days, custom)
- **Granularity**: Auto-adjusted based on time range (hourly, daily, weekly)

##### Execution Status Distribution
- **Type**: Pie/Donut chart
- **Metrics**: 
  - Successful executions
  - Failed executions
  - In-progress executions
- **Percentage**: Show percentages for each status

##### Execution Time Analysis
- **Type**: Bar chart or histogram
- **Metrics**:
  - Average execution time
  - Min/Max execution time
  - Execution time distribution
- **Time Range**: Configurable

##### Notification Delivery by Channel
- **Type**: Stacked bar chart or grouped bar chart
- **Metrics**:
  - Notifications sent per channel
  - Delivery success per channel
  - Delivery failure per channel
- **Channels**: All channels used in workflow

##### Node Performance
- **Type**: Bar chart or table
- **Metrics**:
  - Execution count per node
  - Average execution time per node
  - Error count per node
  - Success rate per node
- **Nodes**: All nodes in workflow definition

#### Execution History Section
- **Table View**: List of recent executions
- **Columns**:
  - Execution ID
  - Start Time
  - End Time
  - Duration
  - Status (Success, Failed, In Progress)
  - Notifications Sent
  - Actions (View Details)
- **Pagination**: Paginated list (default 20 per page)
- **Filtering**: 
  - By status
  - By date range
  - By trigger type
- **Sorting**: By start time (newest first, oldest first)

#### Error Analysis Section
- **Error Summary**: 
  - Total errors
  - Error rate
  - Most common error types
- **Error Timeline**: Line chart showing errors over time
- **Error Details Table**:
  - Error ID
  - Timestamp
  - Error Type
  - Error Message
  - Node (where error occurred)
  - Execution ID
  - Actions (View Details)

#### Performance Insights Section
- **Performance Alerts**: 
  - Slow execution warnings
  - High error rate warnings
  - Low delivery rate warnings
- **Recommendations**: 
  - Optimization suggestions
  - Best practices
  - Performance tips

#### Report Configuration Section (Optional)
- **Report Status**: Display if report is configured and active
- **Next Report**: Display next scheduled report generation time
- **Last Report**: Display last generated report with link to download
- **Quick Actions**:
  - Configure Report
  - View Report History
  - Test Report Generation
  - Download Recent Reports

### Filtering and Time Range

#### Time Range Selector
- **Presets**:
  - Last 24 hours
  - Last 7 days
  - Last 30 days
  - Last 90 days
  - Custom range
- **Custom Range**: Date picker for start and end dates
- **Timezone**: Timezone selection (default: user's timezone)

#### Additional Filters
- **Status Filter**: Filter executions by status
- **Trigger Filter**: Filter by trigger type
- **Node Filter**: Filter by specific node (if applicable)

### Real-time Updates

#### Auto-refresh
- **Interval**: Configurable auto-refresh (30 seconds, 1 minute, 5 minutes, disabled)
- **Indicator**: Visual indicator when data is refreshing
- **Manual Refresh**: Refresh button to manually update data

#### Live Metrics (Optional for MVP)
- Real-time execution count
- Live status updates
- Real-time error alerts

### Data Export

#### Export Options
- **Export Dashboard**: Export all dashboard data as PDF
- **Export Metrics**: Export metrics as CSV/Excel
- **Export Execution History**: Export execution history as CSV/Excel
- **Export Charts**: Export individual charts as images (PNG, SVG)

#### Export Configuration
- **Date Range**: Select date range for export
- **Format**: CSV, Excel, PDF, PNG, SVG
- **Include**: Select which sections to include

### Empty States

#### No Executions
- **Message**: "This workflow hasn't been executed yet"
- **Actions**: 
  - View Workflow Details
  - Test Workflow
  - View Documentation

#### No Data in Time Range
- **Message**: "No data available for selected time range"
- **Actions**: 
  - Change time range
  - View all time data

### Responsive Design

#### Desktop View
- Full dashboard with all sections visible
- Multi-column layout
- Side-by-side charts

#### Tablet View
- Stacked layout
- Responsive charts
- Collapsible sections

#### Mobile View
- Single column layout
- Simplified metrics
- Tabbed navigation for sections
- Mobile-optimized charts

## Data Model

### Analytics Data
- Uses existing analytics tables (workflow_executions, notifications, etc.)
- Aggregated data for performance
- Real-time queries for current metrics

### Caching
- Cache aggregated metrics for performance
- Cache invalidation on new executions
- Cache TTL: 1 minute for real-time data, 5 minutes for historical data

## API Endpoints

See [API - Workflow Dashboard](../api/endpoints.md#workflow-dashboard)

## User Experience

### For Non-Technical Users (Marketing Teams)
- **Simple Metrics**: Clear, easy-to-understand metrics
- **Visual Charts**: Intuitive charts and graphs
- **Guided Insights**: Helpful recommendations and alerts
- **Export Reports**: Easy export for reporting

### For Technical Users (Developers)
- **Detailed Metrics**: Comprehensive performance data
- **Node-level Analysis**: Deep dive into node performance
- **Error Details**: Detailed error information
- **API Access**: Programmatic access to dashboard data

## Technical Requirements

### Frontend
- **Component**: Dedicated WorkflowDashboard component
- **State Management**: Manage dashboard state, filters, time range
- **Charts**: Use chart library (Recharts, Chart.js, or similar)
- **Real-time**: WebSocket or polling for updates (optional for MVP)
- **Export**: PDF generation library, CSV/Excel export

### Backend
- **API Endpoints**: REST API for dashboard data
- **Aggregation**: Efficient data aggregation queries
- **Caching**: Cache aggregated metrics
- **Performance**: Optimized queries for dashboard data

### Performance
- **Load Time**: Dashboard should load within 2 seconds
- **Query Optimization**: Efficient database queries
- **Caching**: Cache frequently accessed data
- **Pagination**: Paginate large datasets

## Related Features

- [Workflow Builder](./workflow-builder.md) - Workflow creation and management
- [Analytics](./analytics.md) - General analytics system
- [Execution Management](./workflow-builder.md#workflow-execution) - Execution viewing and management

## User Flows

See [User Flows - Workflow Dashboard Viewing](../user-flows/workflow-dashboard-viewing.md)

