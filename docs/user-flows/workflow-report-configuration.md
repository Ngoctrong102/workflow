# Workflow Report Configuration User Flow

## Overview

This document describes the user flow for configuring automated reports for workflows that will be sent to business teams at scheduled intervals.

## User Journey

### 1. Access Report Configuration
- **Entry Points**:
  - Workflow Dashboard → "Configure Report" button
  - Workflow Details → "Report Settings" tab
  - Workflow List → "Configure Report" action
- **User Action**: Click "Configure Report" or navigate to report settings

### 2. Report Configuration Form
- **Screen**: Report Configuration form
- **Form Fields**:
  - **Report Name**: Text input (default: workflow name + "Report")
  - **Recipients**: Multi-select email input
    - Add email addresses
    - Remove email addresses
    - Validate email format
  - **Schedule Type**: Radio buttons or dropdown
    - Daily
    - Weekly
    - Monthly
    - Custom (Cron)
  - **Schedule Time**: Time picker (for daily/weekly/monthly)
  - **Schedule Day**: Day selector
    - For weekly: Day of week (Monday-Sunday)
    - For monthly: Day of month (1-31)
  - **Cron Expression**: Text input (for custom schedule)
  - **Timezone**: Timezone selector
  - **Format**: Radio buttons (PDF, Excel, CSV)
  - **Sections**: Checkboxes
    - Execution Summary
    - Notification Summary
    - Performance Metrics
    - Error Analysis
    - Custom Metrics (optional)
  - **Status**: Toggle (Active/Inactive)
- **User Actions**:
  - Fill in form fields
  - Select schedule type
  - Configure schedule details
  - Select report sections
  - Click "Save" or "Cancel"

### 3. Schedule Configuration
- **Screen**: Report Configuration form with schedule section
- **For Daily Schedule**:
  - User selects "Daily"
  - User selects time (e.g., 9:00 AM)
  - User selects timezone
- **For Weekly Schedule**:
  - User selects "Weekly"
  - User selects day of week (e.g., Monday)
  - User selects time (e.g., 9:00 AM)
  - User selects timezone
- **For Monthly Schedule**:
  - User selects "Monthly"
  - User selects day of month (e.g., 1st)
  - User selects time (e.g., 9:00 AM)
  - User selects timezone
- **For Custom Schedule**:
  - User selects "Custom"
  - User enters cron expression
  - User selects timezone
  - System validates cron expression
- **User Actions**:
  - Select schedule type
  - Configure schedule details
  - View next generation time preview

### 4. Preview Report
- **Screen**: Report Preview
- **User Actions**:
  - Click "Preview Report" button
  - View preview of report content
  - Review report sections
  - Check report data
- **System Actions**:
  - Generate preview data
  - Display preview
  - Show report structure

### 5. Save Report Configuration
- **Screen**: Report Configuration form
- **User Actions**:
  - Click "Save" button
  - Confirm configuration
- **System Actions**:
  - Validate form data
  - Calculate next generation time
  - Save report configuration
  - Schedule report generation
  - Show success message
- **Success State**: 
  - Configuration saved
  - Next generation time displayed
  - Report status shown as "Active"

### 6. View Report Configuration
- **Screen**: Report Settings page
- **Displayed Information**:
  - Report name
  - Recipients list
  - Schedule details
  - Format
  - Selected sections
  - Status
  - Last generated report
  - Next generation time
- **User Actions**:
  - View configuration details
  - Edit configuration
  - Test report generation
  - View report history

### 7. Edit Report Configuration
- **Screen**: Report Configuration form (pre-filled)
- **User Actions**:
  - Modify form fields
  - Update schedule
  - Change recipients
  - Update sections
  - Click "Save"
- **System Actions**:
  - Validate changes
  - Update configuration
  - Recalculate next generation time
  - Update schedule

### 8. Test Report Generation
- **Screen**: Report Settings page
- **User Actions**:
  - Click "Test Report" button
  - Select test period (optional)
  - Click "Generate"
- **System Actions**:
  - Generate test report
  - Send test report to user's email
  - Show generation status
- **Result**: User receives test report via email

### 9. View Report History
- **Screen**: Report History page
- **Displayed Information**:
  - List of generated reports
  - Report period
  - Generation date
  - Format
  - File size
  - Delivery status
  - Recipients
- **User Actions**:
  - View report list
  - Filter by date range
  - Download report
  - View report details

### 10. Download Report
- **Screen**: Report History page
- **User Actions**:
  - Click "Download" button for a report
  - Select download location
- **System Actions**:
  - Retrieve report file
  - Download file to user's device

### 11. Manage Report Status
- **Screen**: Report Settings page
- **User Actions**:
  - Pause report (temporarily disable)
  - Resume report (re-enable)
  - Deactivate report (permanently disable)
- **System Actions**:
  - Update report status
  - Update schedule accordingly
  - Show status change confirmation

### 12. Delete Report Configuration
- **Screen**: Report Settings page
- **User Actions**:
  - Click "Delete" button
  - Confirm deletion
- **System Actions**:
  - Delete report configuration
  - Cancel scheduled reports
  - Show deletion confirmation
- **Result**: Report configuration removed, no more automated reports

## Report Generation Flow (Automatic)

### 1. Scheduled Trigger
- **System Action**: Scheduled job checks for due reports
- **Trigger**: Based on `next_generation_at` timestamp

### 2. Generate Report
- **System Actions**:
  - Calculate report period (previous day/week/month)
  - Query workflow execution data
  - Aggregate metrics
  - Generate report file (PDF/Excel/CSV)
  - Store report file

### 3. Send Report
- **System Actions**:
  - Send report via email to recipients
  - Track delivery status
  - Log delivery attempts
  - Retry on failure (up to 3 attempts)

### 4. Update Configuration
- **System Actions**:
  - Update `last_generated_at`
  - Calculate `next_generation_at`
  - Update `generation_count`
  - Update `last_generation_status`
  - Save report history

## Error Handling

### Invalid Schedule
- **Error**: Invalid cron expression or schedule configuration
- **User Action**: Fix schedule configuration
- **System Action**: Show validation error

### Generation Failure
- **Error**: Report generation failed
- **System Action**: 
  - Log error
  - Update `last_generation_status` to "failed"
  - Store error message
  - Retry next scheduled time

### Delivery Failure
- **Error**: Email delivery failed
- **System Action**:
  - Log delivery error
  - Retry delivery (up to 3 attempts)
  - Update delivery status
  - Notify admin if all retries fail

## Related Documentation

- [Workflow Report Feature](../features/workflow-report.md)
- [Workflow Dashboard Feature](../features/workflow-dashboard.md)
- [Analytics Feature](../features/analytics.md)





