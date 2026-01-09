# Workflow Creation User Flow

## Overview

This document describes the user flow for creating a notification workflow using the drag-and-drop workflow builder.

## User Journey

### 1. Access Workflow Builder
- **Entry Point**: Dashboard → "Create Workflow" button
- **Alternative**: Templates → "Create from Template"
- **User Action**: Click "Create Workflow"

### 2. Create New Workflow
- **Screen**: Workflow Builder (empty canvas)
- **User Actions**:
  - Enter workflow name
  - Enter description (optional)
  - Click "Create" or "Start Building"

### 3. Add Trigger Node
- **Screen**: Workflow Builder with empty canvas
- **User Actions**:
  - Open node palette (left sidebar)
  - Drag trigger node to canvas
  - Select trigger type:
    - API Trigger
    - Schedule Trigger
    - Event Trigger (Kafka)
- **System Actions**:
  - Node appears on canvas
  - Properties panel opens (right sidebar)

### 4. Configure Trigger
- **Screen**: Properties panel for trigger node
- **User Actions**:
  - Configure trigger-specific settings:
    - **API Trigger**: Path, method, authentication
    - **Schedule Trigger**: Cron expression, timezone, data
    - **File Trigger**: File formats, data mapping
    - **Event Trigger**: Queue type, topic, filter
  - Click "Save" or "Apply"
- **System Actions**:
  - Validate configuration
  - Save trigger settings
  - Update node display

### 5. Add Action Node
- **Screen**: Workflow Builder with trigger node
- **User Actions**:
  - Open node palette
  - Drag action node to canvas:
    - Send Email
    - Send SMS
    - Send Push
    - Send Slack
    - Send Discord
    - Send Teams
    - Send Webhook
  - Connect trigger node to action node (drag connection)
- **System Actions**:
  - Create connection between nodes
  - Validate connection

### 6. Configure Action Node
- **Screen**: Properties panel for action node
- **User Actions**:
  - Select template (if using template)
  - Or configure message directly:
    - Subject/Title
    - Body/Message
    - Recipients
    - Variables
  - Configure channel-specific settings
  - Click "Save"
- **System Actions**:
  - Validate configuration
  - Save action settings
  - Update node display

### 7. Add Logic Nodes (Optional)
- **Screen**: Workflow Builder
- **User Actions**:
  - Add condition node for branching
  - Configure condition:
    - Field to check
    - Comparison operator
    - Value
  - Connect nodes to condition branches
  - Add other logic nodes as needed:
    - Switch (multi-case)
    - Loop (iterate)
    - Delay (wait)
    - Merge (combine branches)
- **System Actions**:
  - Validate logic
  - Update workflow structure

### 8. Preview Workflow
- **Screen**: Workflow Builder with preview option
- **User Actions**:
  - Click "Preview" button
  - Review workflow structure
  - Check node connections
  - Validate workflow visually
- **System Actions**:
  - Display workflow preview
  - Highlight any issues

### 9. Test Workflow
- **Screen**: Test execution dialog
- **User Actions**:
  - Click "Test" button
  - Enter test data
  - Click "Run Test"
  - Review test results
- **System Actions**:
  - Execute workflow with test data
  - Display execution results
  - Show node execution status
  - Display any errors

### 10. Save Workflow
- **Screen**: Workflow Builder
- **User Actions**:
  - Click "Save" button
  - Workflow saved as draft
  - Or click "Save & Activate" to activate immediately
- **System Actions**:
  - Validate workflow
  - Save workflow definition
  - Create workflow version
  - Activate triggers (if activated)

### 11. Activate Workflow
- **Screen**: Workflow details or list
- **User Actions**:
  - Click "Activate" button
  - Confirm activation
- **System Actions**:
  - Activate workflow
  - Activate associated triggers
  - Start listening for trigger events

## Alternative Flows

### Create from Template
1. Browse template library
2. Select template
3. Click "Create from Template"
4. Workflow opens with template structure
5. Customize as needed
6. Save workflow

### Import Workflow
1. Click "Import" button
2. Upload workflow JSON file
3. System validates and imports
4. Workflow appears in builder
5. Review and customize
6. Save workflow

## Error Handling

### Validation Errors
- **Invalid Configuration**: Show error message, highlight node
- **Missing Connections**: Show warning, prevent save
- **Invalid Logic**: Show error, suggest fix

### Save Errors
- **Network Error**: Show error, allow retry
- **Validation Error**: Show validation errors, prevent save
- **Conflict Error**: Show conflict message, suggest resolution

## Success Criteria

- Workflow created successfully
- All nodes configured correctly
- Workflow validated
- Workflow saved and activated
- Triggers active and listening

## Related Documentation

- [Workflow Builder Feature](../features/workflow-builder.md)
- [Notification Delivery Flow](./notification-delivery.md)
- [Analytics Viewing Flow](./analytics-viewing.md)


