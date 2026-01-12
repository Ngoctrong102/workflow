# Workflow Creation User Flow

## Overview

This document describes the user flow for creating a notification workflow. The flow follows a **trigger-first** and **action-first** approach: users create trigger configs and action definitions before building workflows.

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

### 3. Create Trigger Config (If Not Exists)

**Note**: This step can be done before or during workflow creation. Users can also reuse existing trigger configs.

- **Screen**: Trigger Management page (or modal from workflow builder)
- **User Actions**:
  1. Click "Create Trigger Config" or "Manage Triggers"
  2. Select trigger type:
     - API Call Trigger
     - Scheduler Trigger
     - Event Trigger (Kafka)
  3. Configure trigger-specific settings:
     - **API Trigger**: Name, endpoint path, HTTP method, authentication
     - **Scheduler Trigger**: Name, cron expression, timezone, data
     - **Event Trigger**: Name, Kafka brokers, topic, offset, filter
  4. Set status (active/inactive)
  5. Click "Save"
- **System Actions**:
  - Validate configuration
  - Save trigger config to `triggers` table
  - Return to workflow builder (if opened from there)

### 4. Add Trigger Node to Workflow

- **Screen**: Workflow Builder with empty canvas
- **User Actions**:
  1. Open node palette (left sidebar)
  2. Drag "Trigger" node to canvas
  3. Click on trigger node to open Properties Panel
- **System Actions**:
  - Node appears on canvas
  - Properties panel opens (right sidebar)
  - Shows list of available trigger configs from registry

### 5. Link Trigger Config to Node

- **Screen**: Properties panel for trigger node
- **User Actions**:
  1. Select trigger config from dropdown (shows active trigger configs)
  2. System loads trigger config details
  3. Configure instance-specific settings:
     - **Event Trigger**: Consumer Group (required, unique per workflow)
     - Other instance-specific fields (if any)
  4. Click "Save" or "Apply"
- **System Actions**:
  - Validate configuration
  - Create trigger instance in workflow definition:
    ```json
    {
      "id": "node-1",
      "nodeType": "trigger",
      "nodeConfig": {
        "triggerConfigId": "trigger-config-123",
        "triggerType": "event",
        "instanceConfig": {
          "consumerGroup": "workflow-456-consumer"
        }
      }
    }
    ```
  - Update node display with trigger config name

### 6. Create Action Definition (If Not Exists)

**Note**: This step can be done before or during workflow creation. Users can also reuse existing action definitions.

- **Screen**: Action Management page (or modal from workflow builder)
- **User Actions**:
  1. Click "Create Action" or "Manage Actions"
  2. Select action type:
     - API Call Action
     - Publish Event Action
     - Function Action
     - Custom Actions (Send Email, Send SMS, etc.)
  3. Configure action definition (if creating new)
  4. Click "Save"
- **System Actions**:
  - Validate action definition
  - Save to `actions` table (registry)
  - Return to workflow builder (if opened from there)

### 7. Add Action Node to Workflow

- **Screen**: Workflow Builder with trigger node
- **User Actions**:
  1. Open node palette
  2. Drag "Action" node to canvas
  3. Connect trigger node to action node (drag connection)
  4. Click on action node to open Properties Panel
- **System Actions**:
  - Create connection between nodes
  - Validate connection
  - Properties panel opens

### 8. Configure Action Node

- **Screen**: Properties panel for action node
- **User Actions**:
  1. Select action from registry dropdown
  2. System loads action definition template
  3. Configure action-specific settings:
     - **API Call Action**: URL, method, headers, body
     - **Send Email**: Recipients, subject, body, template
     - **Send SMS**: Phone numbers, message
     - Other action-specific fields
  4. Click "Save"
- **System Actions**:
  - Validate configuration
  - Save action configuration to workflow definition node data
  - Update node display

### 9. Add Logic Nodes (Optional)

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

### 10. Preview Workflow

- **Screen**: Workflow Builder with preview option
- **User Actions**:
  - Click "Preview" button
  - Review workflow structure
  - Check node connections
  - Validate workflow visually
- **System Actions**:
  - Display workflow preview
  - Highlight any issues

### 11. Test Workflow

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

### 12. Save Workflow

- **Screen**: Workflow Builder
- **User Actions**:
  - Click "Save" button
  - Workflow saved as draft
  - Or click "Save & Activate" to activate immediately
- **System Actions**:
  - Validate workflow
  - Save workflow definition (including trigger instances and action configs)
  - Create workflow version
  - **Do NOT activate triggers yet** (triggers activate when workflow is activated)

### 13. Activate Workflow

- **Screen**: Workflow details or list
- **User Actions**:
  - Click "Activate" button
  - Confirm activation
- **System Actions**:
  - Activate workflow
  - For each trigger instance in workflow:
    - Load trigger config from database
    - Merge with instance-specific overrides
    - Create runtime consumer/scheduler
    - Start processing
  - Store runtime state (ACTIVE) in workflow definition
  - Start listening for trigger events

## Alternative Flows

### Reuse Existing Trigger Config

1. User opens workflow builder
2. Adds trigger node to canvas
3. Selects existing trigger config from dropdown
4. Configures instance-specific settings
5. Continues with workflow creation

### Reuse Existing Action Definition

1. User opens workflow builder
2. Adds action node to canvas
3. Selects existing action from registry dropdown
4. Configures action settings
5. Continues with workflow creation

### Create from Template

1. Browse template library
2. Select template
3. Click "Create from Template"
4. Workflow opens with template structure
5. Review and update trigger configs/actions as needed
6. Customize as needed
7. Save workflow

### Import Workflow

1. Click "Import" button
2. Upload workflow JSON file
3. System validates and imports
4. Workflow appears in builder
5. Review trigger configs and actions
6. Update if needed (trigger configs may need to be recreated if not exist)
7. Save workflow

## Key Concepts

### Trigger Config vs Trigger Instance

- **Trigger Config**: Created in Trigger Management, stored in `triggers` table, can be reused
- **Trigger Instance**: Created when trigger config is linked to workflow node, stored in workflow definition

### Action Definition vs Action Config

- **Action Definition**: Created in Action Management, stored in `actions` table (registry), can be reused
- **Action Config**: Stored in workflow definition node data, specific to each workflow

### Sharing Resources

- **Trigger Configs**: Can be shared across multiple workflows
- **Action Definitions**: Can be shared across multiple workflows
- **Instance Overrides**: Each workflow can have instance-specific settings (e.g., consumerGroup)

## Error Handling

### Validation Errors

- **Invalid Trigger Config**: Show error message, prevent save
- **Missing Trigger Config**: Prompt user to create or select trigger config
- **Invalid Instance Config**: Show error message, highlight field
- **Missing Connections**: Show warning, prevent save
- **Invalid Logic**: Show error, suggest fix

### Save Errors

- **Network Error**: Show error, allow retry
- **Validation Error**: Show validation errors, prevent save
- **Trigger Config Not Found**: Show error, allow user to select different config
- **Conflict Error**: Show conflict message, suggest resolution

## Success Criteria

- Trigger config created (or existing one selected)
- Trigger node added and linked to trigger config
- Action definition selected (or created)
- Action node added and configured
- All nodes configured correctly
- Workflow validated
- Workflow saved successfully
- Workflow activated (triggers start processing)

## Related Documentation

- [Workflow Builder Feature](../features/workflow-builder.md)
- [Trigger Registry](../features/trigger-registry.md)
- [Action Registry](../features/action-registry.md)
- [Notification Delivery Flow](./notification-delivery.md)
- [Analytics Viewing Flow](./analytics-viewing.md)
