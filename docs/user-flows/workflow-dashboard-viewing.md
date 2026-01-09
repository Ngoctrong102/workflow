# Workflow Dashboard Viewing User Flow

## Overview

This document describes the user flow for viewing and interacting with a workflow-specific monitoring dashboard.

## User Journey

### 1. Access Workflow Dashboard
- **Entry Points**:
  - Workflow List → "View Dashboard" button/link
  - Workflow Details → "Dashboard" tab
  - Direct URL: `/workflows/{workflow_id}/dashboard`
  - Navigation breadcrumb from workflow details
- **User Action**: Click "View Dashboard" or navigate to dashboard

### 2. Dashboard Loads
- **Screen**: Workflow Dashboard page
- **System Actions**:
  - Load workflow information
  - Load dashboard metrics (default: last 7 days)
  - Load execution history
  - Load charts data
- **Loading State**: Show loading skeleton/spinner
- **User Sees**: Dashboard with metrics, charts, and execution history

### 3. View Metrics Overview
- **Screen**: Dashboard with metrics section
- **Displayed Metrics**:
  - Total Executions
  - Success Rate
  - Average Execution Time
  - Total Notifications Sent
  - Delivery Rate
  - Error Rate
- **Trend Indicators**: Up/down arrows showing change from previous period
- **User Actions**:
  - View metric details
  - Click metric to drill down
  - Hover for tooltip with additional info

### 4. Configure Time Range
- **Screen**: Dashboard with time range selector
- **User Actions**:
  - Click time range selector
  - Select preset:
    - Last 24 hours
    - Last 7 days
    - Last 30 days
    - Last 90 days
    - Custom range
  - Or select custom range (date picker)
  - Click "Apply"
- **System Actions**:
  - Reload dashboard data for selected range
  - Update all charts and metrics
  - Show loading state during refresh

### 5. View Charts
- **Screen**: Dashboard with charts section
- **Available Charts**:
  - **Execution Trends**: Line chart showing executions over time
  - **Execution Status**: Pie chart showing success/failure distribution
  - **Execution Time**: Bar chart showing execution time analysis
  - **Delivery by Channel**: Stacked bar chart showing notifications per channel
  - **Node Performance**: Bar chart or table showing node-level metrics
- **User Actions**:
  - View different charts
  - Hover over chart elements for details
  - Click chart elements to drill down
  - Switch chart type (if applicable)
  - Export chart as image

### 6. View Execution History
- **Screen**: Dashboard with execution history table
- **Displayed Information**:
  - Execution ID
  - Start Time
  - End Time
  - Duration
  - Status
  - Notifications Sent
  - Actions
- **User Actions**:
  - Scroll through execution list
  - Click "View Details" for specific execution
  - Filter by status
  - Filter by date range
  - Sort by column
  - Navigate pages (if paginated)

### 7. Filter Executions
- **Screen**: Dashboard with filter options
- **User Actions**:
  - Click "Filter" button
  - Select status filter (All, Success, Failed, In Progress)
  - Select trigger type filter
  - Select node filter (if applicable)
  - Select channel filter
  - Click "Apply Filters"
- **System Actions**:
  - Filter execution history
  - Update metrics based on filters
  - Update charts based on filters

### 8. View Error Analysis
- **Screen**: Dashboard with error analysis section
- **Displayed Information**:
  - Error summary (total errors, error rate)
  - Error timeline chart
  - Error details table
- **User Actions**:
  - View error summary
  - View error timeline
  - Click on error in table to view details
  - Filter errors by type
  - Export error data

### 9. View Performance Insights
- **Screen**: Dashboard with performance insights section
- **Displayed Information**:
  - Performance alerts (if any)
  - Recommendations
  - Best practices
- **User Actions**:
  - View alerts
  - View recommendations
  - Click recommendation to learn more
  - Dismiss alerts (if applicable)

### 10. Export Dashboard Data
- **Screen**: Dashboard with export options
- **User Actions**:
  - Click "Export" button
  - Select export type:
    - Export Dashboard (PDF)
    - Export Metrics (CSV/Excel)
    - Export Execution History (CSV/Excel)
    - Export Chart (PNG/SVG)
  - Select date range (if applicable)
  - Select format
  - Click "Download"
- **System Actions**:
  - Generate export file
  - Download file to user's device

### 11. Refresh Dashboard
- **Screen**: Dashboard with refresh option
- **User Actions**:
  - Click "Refresh" button (manual refresh)
  - Or wait for auto-refresh (if enabled)
- **System Actions**:
  - Reload all dashboard data
  - Update metrics, charts, and tables
  - Show refresh indicator

### 12. Navigate to Related Views
- **Screen**: Dashboard with navigation options
- **User Actions**:
  - Click "View Workflow Details" → Navigate to workflow details
  - Click "Edit Workflow" → Navigate to workflow editor
  - Click "View Execution History" → Navigate to full execution history
  - Click on execution → Navigate to execution details
  - Click on node → Navigate to node details (if applicable)

## Dashboard States

### Normal State
- All data loaded
- Metrics displayed
- Charts rendered
- Execution history shown

### Loading State
- Loading skeleton/spinner
- Placeholder content
- Disabled interactions

### Empty State (No Executions)
- Message: "This workflow hasn't been executed yet"
- Actions:
  - View Workflow Details
  - Test Workflow
  - View Documentation

### Empty State (No Data in Range)
- Message: "No data available for selected time range"
- Actions:
  - Change time range
  - View all time data

### Error State
- Error message displayed
- Retry button
- Support contact information

## Filtering and Search

### Time Range
- **Presets**: Last 24 hours, 7 days, 30 days, 90 days, Custom
- **Custom Range**: Date picker with start and end dates
- **Timezone**: Timezone selection (default: user's timezone)

### Status Filter
- All statuses
- Success only
- Failed only
- In Progress only

### Trigger Filter
- All triggers
- API trigger
- Schedule trigger
- File trigger
- Event trigger

### Node Filter
- All nodes
- Specific node selection
- Multiple node selection

### Channel Filter
- All channels
- Specific channel selection
- Multiple channel selection

## Real-time Updates

### Auto-refresh
- **Interval Options**: 30 seconds, 1 minute, 5 minutes, Disabled
- **Indicator**: Visual indicator when refreshing
- **User Control**: Enable/disable auto-refresh

### Manual Refresh
- **Button**: Refresh button in header
- **Action**: Immediately reload all data
- **Feedback**: Loading indicator during refresh

## Responsive Behavior

### Desktop
- Full dashboard layout
- Multi-column charts
- Side-by-side sections
- Full table view

### Tablet
- Stacked layout
- Responsive charts
- Collapsible sections
- Scrollable table

### Mobile
- Single column layout
- Simplified metrics
- Tabbed navigation
- Mobile-optimized charts
- Swipeable sections

## Related Documentation

- [Workflow Creation Flow](./workflow-creation.md)
- [Analytics Viewing Flow](./analytics-viewing.md)
- [Workflow Dashboard Feature](../features/workflow-dashboard.md)





