# Backend Sprint 30: TriggerController Refactoring

## 📋 Overview

Refactor TriggerController để expose trigger config management APIs thay vì workflow-specific trigger creation endpoints.

## 🎯 Objectives

- Remove legacy trigger creation endpoints
- Add trigger config CRUD endpoints
- Remove trigger instance lifecycle endpoints
- Update API documentation

## 📚 Design References

- `@import(../design-questions.md)` - Design decisions
- `@import(../implementation-refactor-plan.md#phase-2-api-endpoints-refactoring)` - Detailed plan
- `@import(../../api/endpoints.md#trigger-configs)` - Target API design
- `@import(../../features/trigger-registry.md)` - Trigger registry design

## ✅ Tasks

### 1. Remove Legacy Endpoints
- [ ] Remove `POST /triggers/api`
- [ ] Remove `POST /triggers/schedule`
- [ ] Remove `POST /triggers/event`
- [ ] Remove `POST /triggers/file`
- [ ] Remove `POST /triggers/{id}/initialize`
- [ ] Remove `POST /triggers/{id}/start`
- [ ] Remove `POST /triggers/{id}/pause`
- [ ] Remove `POST /triggers/{id}/resume`
- [ ] Remove `POST /triggers/{id}/stop`
- [ ] Remove `POST /triggers/{id}/activate`
- [ ] Remove `POST /triggers/{id}/deactivate`

### 2. Add New Endpoints
- [ ] `POST /triggers` - Create trigger config
  - Request: `CreateTriggerConfigRequest`
  - Response: `TriggerResponse`
- [ ] `GET /triggers/{id}` - Get trigger config
  - Response: `TriggerResponse`
- [ ] `GET /triggers` - List trigger configs
  - Query params: `triggerType`, `status`, `search`, `limit`, `offset`
  - Response: `PagedResponse<TriggerResponse>`
- [ ] `PUT /triggers/{id}` - Update trigger config
  - Request: `UpdateTriggerConfigRequest`
  - Response: `TriggerResponse`
- [ ] `DELETE /triggers/{id}` - Delete trigger config
  - Response: `204 No Content`

### 3. Keep Existing Endpoints (if needed)
- [ ] `POST /trigger/{path}` - Trigger workflow execution (API trigger endpoint)
- [ ] Review and update if needed

### 4. Update API Documentation
- [ ] Update Swagger/OpenAPI annotations
- [ ] Update endpoint descriptions
- [ ] Add request/response examples

### 5. Testing
- [ ] Unit tests for new endpoints
- [ ] Integration tests
- [ ] API contract tests

## 🔗 Related Sprints

- **Sprint 29**: TriggerService Refactoring (prerequisite)
- **Sprint 31**: WorkflowService Lifecycle Management (related)
- **Sprint 32**: TriggerRegistryController Update (related)

## ⚠️ Breaking Changes

- 11 endpoints removed
- 5 new endpoints added
- Request/response structures changed

## 📝 Notes

- All trigger configs are independent (no workflow_id in request)
- Support filtering by triggerType, status, search
- Pagination support for list endpoint

