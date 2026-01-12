# Backend Sprint 29: TriggerService Refactoring

## 📋 Overview

Refactor TriggerService để support trigger config management (independent, shareable configs) thay vì workflow-specific triggers.

## 🎯 Objectives

- Remove workflow-specific trigger creation methods
- Add trigger config CRUD methods
- Update service interface and implementation
- Remove trigger instance lifecycle methods (moved to WorkflowService)

## 📚 Design References

- `@import(../design-questions.md)` - Design decisions
- `@import(../clarification-questions.md)` - Clarifications
- `@import(../implementation-refactor-plan.md#phase-4-service-layer-refactoring)` - Detailed plan
- `@import(../../api/endpoints.md#trigger-configs)` - API endpoints
- `@import(../../features/trigger-registry.md)` - Trigger registry design

## ✅ Tasks

### 1. Update TriggerService Interface
- [ ] Remove methods:
  - `createApiTrigger(CreateApiTriggerRequest)`
  - `createScheduleTrigger(CreateScheduleTriggerRequest)`
  - `createEventTrigger(CreateEventTriggerRequest)`
  - `createFileTrigger(CreateFileTriggerRequest)`
  - `listTriggers(String workflowId)` - workflow-specific
  - `activateTrigger(String id)`
  - `deactivateTrigger(String id)`
- [ ] Add methods:
  - `createTriggerConfig(CreateTriggerConfigRequest)` - independent config creation
  - `getTriggerConfigById(String id)`
  - `listTriggerConfigs(String triggerType, String status, String search, int limit, int offset)`
  - `updateTriggerConfig(String id, UpdateTriggerConfigRequest)`
  - `deleteTriggerConfig(String id)`
- [ ] Keep methods:
  - `activateApiTrigger()` - for triggering workflow execution
  - `getTriggerByPath()` - for API trigger endpoint

### 2. Update TriggerServiceImpl
- [ ] Implement new trigger config CRUD methods
- [ ] Remove workflow-specific logic
- [ ] Update validation logic (no workflow_id required)
- [ ] Update error handling

### 3. Create New DTOs
- [ ] `CreateTriggerConfigRequest.java` - unified request for all trigger types
- [ ] `UpdateTriggerConfigRequest.java` - update request
- [ ] Update `TriggerResponse.java` - remove `workflowId`, `nodeId`, add `name`

### 4. Remove Legacy DTOs (or refactor)
- [ ] `CreateApiTriggerRequest.java` - remove or refactor
- [ ] `CreateScheduleTriggerRequest.java` - remove or refactor
- [ ] `CreateEventTriggerRequest.java` - remove or refactor
- [ ] `CreateFileTriggerRequest.java` - remove or refactor

### 5. Testing
- [ ] Unit tests for new methods
- [ ] Integration tests
- [ ] Verify backward compatibility for `activateApiTrigger()` and `getTriggerByPath()`

## 🔗 Related Sprints

- **Sprint 28**: Database Migration (prerequisite)
- **Sprint 30**: TriggerController Refactoring (depends on this)
- **Sprint 31**: WorkflowService Lifecycle Management (related)

## ⚠️ Breaking Changes

- Service interface changes: methods removed/renamed
- DTOs changes: new request/response structures

## 📝 Notes

- Keep `activateApiTrigger()` for backward compatibility (workflow execution)
- Consider validation for trigger config (type-specific validation)
- Support all 3 trigger types: api-call, scheduler, event

