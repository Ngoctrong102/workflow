# Sprint 14: Trigger Registry & Instance Management

## Goal
Implement trigger registry service and instance management, ensuring triggers are properly initialized and managed.

## Phase
Triggers & Integration

## Complexity
Medium

## Dependencies
Sprint 11, Sprint 12, Sprint 13

## Compliance Check

### Before Starting
1. ✅ Read `@import(features/trigger-registry.md)` - Understand trigger registry
2. ✅ Verify Sprint 11, 12, 13 are completed

## Tasks

### Trigger Service
- [ ] Create `TriggerService` interface - See `@import(technical/backend/service-interfaces.md#trigger-service)`
- [ ] Implement `TriggerServiceImpl`:
  - `createTrigger()`, `getTrigger()`, `updateTrigger()`, `deleteTrigger()`
  - `activateTrigger()`, `deactivateTrigger()`
  - `listTriggers()`

### Trigger Instance Manager
- [ ] Create `TriggerInstanceManager.java`:
  - Manage trigger instances lifecycle
  - Initialize instances when workflow is activated
  - Destroy instances when workflow is deactivated
  - Handle instance state (ACTIVE, PAUSED, STOPPED)

### Trigger Activation Service
- [ ] Create `TriggerActivationService.java`:
  - `activateTriggersForWorkflow()` - Activate all triggers for workflow
  - `deactivateTriggersForWorkflow()` - Deactivate all triggers for workflow
  - `syncTriggersWithWorkflow()` - Sync trigger instances with workflow definition

### Trigger Status Management
- [ ] Track trigger instance status separately from trigger config
- [ ] Store runtime state (in memory or separate table)
- [ ] Handle trigger errors and update status

## Deliverables

- ✅ Trigger service fully implemented
- ✅ Trigger instance management working
- ✅ Trigger activation/deactivation working
- ✅ Triggers sync with workflow status

## Technical Details

### Trigger Service Interface
See `@import(technical/backend/service-interfaces.md#trigger-service)`.

### Trigger Instance Lifecycle
See `@import(features/trigger-registry.md#trigger-instance-lifecycle)`.

## Compliance Verification

- [ ] Verify trigger service matches `@import(technical/backend/service-interfaces.md#trigger-service)`
- [ ] Test trigger activation/deactivation
- [ ] Test trigger instance lifecycle

## Related Documentation

- `@import(features/trigger-registry.md)` ⚠️ **MUST MATCH**
- `@import(technical/backend/service-interfaces.md#trigger-service)`

