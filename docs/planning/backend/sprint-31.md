# Backend Sprint 31: WorkflowService Lifecycle Management

## 📋 Overview

Add trigger instance lifecycle management to WorkflowService. Trigger instances are managed through workflow activation/deactivation, not separate endpoints.

## 🎯 Objectives

- Add methods to get workflow trigger instances
- Implement trigger instance lifecycle in workflow activation/deactivation
- Store runtime state in workflow definition
- Remove TriggerInstanceService dependency

## 📚 Design References

- `@import(../design-questions.md)` - Design decisions
- `@import(../clarification-questions.md#7-trigger-instance-lifecycle)` - Lifecycle clarifications
- `@import(../implementation-refactor-plan.md#phase-4-service-layer-refactoring)` - Detailed plan
- `@import(../../api/endpoints.md#get-workflow-triggers)` - API endpoint
- `@import(../../features/triggers.md#trigger-instance-management)` - Lifecycle design

## ✅ Tasks

### 1. Add WorkflowService Methods
- [ ] `getWorkflowTriggers(String workflowId)` - Get trigger instances for workflow
  - Read workflow definition
  - Extract trigger nodes
  - Load trigger configs from database
  - Merge configs with instance overrides
  - Return trigger instances with configs and runtime states
- [ ] Update `activateWorkflow(String workflowId)`:
  - Read trigger instances from workflow definition
  - Load trigger configs from database
  - Merge configs with instance-specific overrides
  - Create consumers/schedulers based on trigger type
  - Store runtime state (ACTIVE) in workflow definition
- [ ] Update `deactivateWorkflow(String workflowId)`:
  - Stop all trigger instances
  - Store runtime state (STOPPED) in workflow definition
- [ ] Add `pauseWorkflow(String workflowId)`:
  - Pause trigger instances
  - Store runtime state (PAUSED) in workflow definition
- [ ] Add `resumeWorkflow(String workflowId)`:
  - Resume trigger instances
  - Store runtime state (ACTIVE) in workflow definition

### 2. Create WorkflowTriggerResponse DTO
- [ ] `WorkflowTriggerResponse.java`:
  - `nodeId` - Node ID in workflow definition
  - `triggerConfigId` - Reference to trigger config
  - `triggerType` - Trigger type
  - `triggerConfig` - Full trigger config from database
  - `instanceConfig` - Instance-specific overrides
  - `runtimeState` - ACTIVE, PAUSED, STOPPED, ERROR

### 3. Update WorkflowController
- [ ] Add `GET /workflows/{workflowId}/triggers` endpoint
  - Returns list of trigger instances with configs and runtime states

### 4. Remove TriggerInstanceService Dependency
- [ ] Remove TriggerInstanceService usage
- [ ] Move logic to WorkflowService
- [ ] Update any references

### 5. Runtime State Management
- [ ] Implement runtime state storage in workflow definition
- [ ] State values: INITIALIZED, ACTIVE, PAUSED, STOPPED, ERROR
- [ ] Update state when workflow lifecycle changes

### 6. Testing
- [ ] Unit tests for lifecycle methods
- [ ] Integration tests for workflow activation/deactivation
- [ ] Test trigger instance creation and management

## 🔗 Related Sprints

- **Sprint 30**: TriggerController Refactoring (related)
- **Sprint 33**: WorkflowExecutor Node Structure Support (depends on this)

## ⚠️ Breaking Changes

- Trigger instance lifecycle now managed through workflow lifecycle
- No separate lifecycle endpoints

## 📝 Notes

- Runtime state stored in workflow definition node data
- Each trigger instance has independent runtime state
- Support for multiple trigger instances per workflow
