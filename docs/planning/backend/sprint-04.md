# Sprint 04: Action Registry & Trigger Registry

## Goal
Implement Action Registry and Trigger Registry systems, ensuring compliance with registry specifications.

## Phase
Foundation

## Complexity
Medium

## Dependencies
Sprint 03

## Compliance Check

### Before Starting
1. ✅ Read `@import(features/action-registry.md)` - Understand Action Registry system
2. ✅ Read `@import(features/trigger-registry.md)` - Understand Trigger Registry system
3. ✅ Read `@import(features/node-types.md)` - Understand node types
4. ✅ Verify Sprint 03 is completed

## Tasks

### Default Actions Migration
- [ ] Create `V10__seed_default_actions.sql`:
  - API Call Action, Publish Event Action, Function Action
  - Custom Actions: send-email, send-sms, send-push, send-in-app, send-slack, send-discord, send-teams, send-webhook, wait-events
  - See `@import(features/action-registry.md)` for action definitions

### Action Registry Service
- [ ] Create `ActionRegistryService` interface:
  - `getAllActions()`, `getActionById()`, `getActionsByType()`, `getCustomActions()`
  - `registerAction()`, `updateAction()`, `enableAction()`, `disableAction()`
- [ ] Implement `ActionRegistryServiceImpl`:
  - Use `ActionRepository` for data access
  - Implement all service methods

### Trigger Registry Service
- [ ] Create `TriggerRegistryService` interface:
  - `getAllTriggers()`, `getTriggerById()`, `getTriggerByType()`, `getConfigTemplate()`
- [ ] Create `TriggerDefinition.java` DTO class
- [ ] Implement `TriggerRegistryServiceImpl`:
  - Hardcode trigger definitions in `@PostConstruct`:
    - API Call Trigger (`api-trigger-standard`)
    - Scheduler Trigger (`scheduler-trigger-standard`)
    - Kafka Event Trigger (`kafka-event-trigger-standard`)
  - See `@import(features/trigger-registry.md)` for trigger definitions

### Exception Classes
- [ ] Create `ResourceNotFoundException.java`

## Deliverables

- ✅ Action Registry fully implemented
- ✅ Trigger Registry fully implemented
- ✅ Default actions seeded in database
- ✅ Default triggers available in code
- ✅ Registry services working

## Technical Details

### Action Registry Structure
See `@import(features/action-registry.md)` for action definition structure.

### Trigger Registry Structure
See `@import(features/trigger-registry.md)` for trigger definition structure.

## Compliance Verification

- [ ] Verify Action Registry matches `@import(features/action-registry.md)`
- [ ] Verify Trigger Registry matches `@import(features/trigger-registry.md)`
- [ ] Verify all default actions are seeded
- [ ] Verify all default triggers are available

## Related Documentation

- `@import(features/action-registry.md)` ⚠️ **MUST MATCH**
- `@import(features/trigger-registry.md)` ⚠️ **MUST MATCH**
- `@import(features/node-types.md)`
