# Sprint 16: API Controllers - Trigger & Action Registry

## Goal
Implement REST API controllers for trigger management and action registry access.

## Phase
API Layer

## Complexity
Simple

## Dependencies
Sprint 14

## Compliance Check

### Before Starting
1. ✅ Read `@import(api/endpoints.md#triggers)` - Understand trigger API
2. ✅ Read `@import(api/endpoints.md#trigger-registry)` - Understand trigger registry API
3. ✅ Read `@import(api/endpoints.md#action-registry)` - Understand action registry API
4. ✅ Read `@import(api/schemas.md)` - Understand request/response schemas
5. ✅ Verify Sprint 14 is completed

## Tasks

### Trigger Controller
- [ ] Create `TriggerController.java`:
  - `POST /workflows/{workflowId}/triggers/api` - Create API trigger
  - `POST /workflows/{workflowId}/triggers/schedule` - Create schedule trigger
  - `POST /workflows/{workflowId}/triggers/event` - Create event trigger
  - `GET /workflows/{workflowId}/triggers` - List triggers
  - `GET /triggers/{id}` - Get trigger
  - `PUT /triggers/{id}` - Update trigger
  - `DELETE /triggers/{id}` - Delete trigger
  - `POST /triggers/{id}/initialize` - Initialize trigger instance
  - `POST /triggers/{id}/start` - Start trigger instance
  - `POST /triggers/{id}/pause` - Pause trigger instance
  - `POST /triggers/{id}/resume` - Resume trigger instance
  - `POST /triggers/{id}/stop` - Stop trigger instance
  - `POST /trigger/{triggerPath}` - Trigger workflow endpoint
  - **MUST MATCH**: `@import(api/endpoints.md#triggers)`

### Action Registry Controller
- [ ] Create `ActionRegistryController.java`:
  - `GET /actions/registry` - List all actions
  - `GET /actions/registry/{id}` - Get action by ID
  - `GET /actions/registry/type/{type}` - Get actions by type
  - `GET /actions/registry/custom` - Get custom actions
  - **MUST MATCH**: `@import(api/endpoints.md#action-registry)`

### Trigger Registry Controller
- [ ] Create `TriggerRegistryController.java`:
  - `GET /triggers/registry` - List all available triggers
  - `GET /triggers/registry/{id}` - Get trigger definition
  - `GET /triggers/registry/type/{type}` - Get trigger by type
  - **MUST MATCH**: `@import(api/endpoints.md#trigger-registry)`

### Request/Response DTOs
- [ ] Create trigger request DTOs
- [ ] Create trigger response DTOs
- [ ] Create action registry response DTOs

## Deliverables

- ✅ Trigger API fully implemented
- ✅ Action registry API implemented
- ✅ Trigger registry API implemented
- ✅ API endpoints match specifications

## Technical Details

### API Endpoints
- **Triggers**: `@import(api/endpoints.md#triggers)` ⚠️ **MUST MATCH**
- **Trigger Registry**: `@import(api/endpoints.md#trigger-registry)` ⚠️ **MUST MATCH**
- **Action Registry**: `@import(api/endpoints.md#action-registry)` ⚠️ **MUST MATCH**
- **Schemas**: `@import(api/schemas.md)` ⚠️ **MUST MATCH**

## Compliance Verification

- [ ] Verify API endpoints match `@import(api/endpoints.md#triggers)`
- [ ] Test trigger CRUD operations
- [ ] Test action registry endpoints

## Related Documentation

- `@import(api/endpoints.md#triggers)` ⚠️ **MUST MATCH**
- `@import(api/endpoints.md#trigger-registry)` ⚠️ **MUST MATCH**
- `@import(api/endpoints.md#action-registry)` ⚠️ **MUST MATCH**
- `@import(api/schemas.md)` ⚠️ **MUST MATCH**

