# Frontend Sprint 29: Trigger Service Update

## 📋 Overview

Update trigger service để call new trigger config management APIs thay vì legacy workflow-specific endpoints.

## 🎯 Objectives

- Remove legacy trigger creation methods
- Add trigger config CRUD methods
- Update API calls to match new backend endpoints
- Remove trigger instance lifecycle methods

## 📚 Design References

- `@import(../implementation-refactor-plan.md#phase-8-service-layer-updates)` - Detailed plan
- `@import(../../api/endpoints.md#trigger-configs)` - API endpoints
- `@import(../../features/trigger-registry.md)` - Trigger registry design

## ✅ Tasks

### 1. Remove Legacy Methods
- [ ] Remove `createApiTrigger()`
- [ ] Remove `createScheduleTrigger()`
- [ ] Remove `createEventTrigger()`
- [ ] Remove `createFileTrigger()`
- [ ] Remove `initializeTrigger()`
- [ ] Remove `startTrigger()`
- [ ] Remove `pauseTrigger()`
- [ ] Remove `resumeTrigger()`
- [ ] Remove `stopTrigger()`

### 2. Add New Methods
- [ ] `createTriggerConfig(request: CreateTriggerConfigRequest)`:
  - Call `POST /triggers`
  - Return `TriggerResponse`
- [ ] `getTriggerConfig(id: string)`:
  - Call `GET /triggers/{id}`
  - Return `TriggerResponse`
- [ ] `listTriggerConfigs(params)`:
  - Call `GET /triggers` with query params
  - Return `PagedResponse<TriggerResponse>`
- [ ] `updateTriggerConfig(id, request)`:
  - Call `PUT /triggers/{id}`
  - Return `TriggerResponse`
- [ ] `deleteTriggerConfig(id)`:
  - Call `DELETE /triggers/{id}`
  - Return void
- [ ] `getWorkflowTriggers(workflowId)`:
  - Call `GET /workflows/{workflowId}/triggers`
  - Return `WorkflowTriggerResponse[]`

### 3. Create Type Definitions
- [ ] `CreateTriggerConfigRequest` type
- [ ] `UpdateTriggerConfigRequest` type
- [ ] `TriggerResponse` type (updated)
- [ ] `WorkflowTriggerResponse` type

### 4. Update Trigger Registry Service
- [ ] Update `getTriggerRegistry()` to call `GET /triggers/registry`
- [ ] Handle response format (trigger configs from database)
- [ ] Update type definitions

### 5. Update Hooks
- [ ] Update `useCreateTrigger` hook
- [ ] Update `useUpdateTrigger` hook
- [ ] Update `useTriggerRegistry` hook
- [ ] Add new hooks as needed

### 6. Testing
- [ ] Unit tests for service methods
- [ ] Integration tests
- [ ] Verify API calls match backend

## 🔗 Related Sprints

- **Sprint 28**: Type Definitions Update (prerequisite)
- **Sprint 30**: NodePalette Update (depends on this)
- **Sprint 31**: PropertiesPanel Update (depends on this)

## ⚠️ Breaking Changes

- All trigger service methods changed
- API endpoints changed
- Request/response structures changed

## 📝 Notes

- Keep `activateApiTrigger()` if needed for workflow execution
- Update error handling for new endpoints
- Support all trigger types in unified request
