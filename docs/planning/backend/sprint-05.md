# Sprint 05: Workflow Service - CRUD Operations

## Goal
Implement workflow management service with CRUD operations, ensuring compliance with workflow builder and API specifications.

## Phase
Core Services

## Complexity
Medium

## Dependencies
Sprint 03, Sprint 04

## Compliance Check

### Before Starting
1. ✅ Read `@import(features/workflow-builder.md)` - Understand workflow structure
2. ✅ Read `@import(api/endpoints.md#workflows)` - Understand API contracts
3. ✅ Read `@import(database-schema/entities.md#workflows)` - Understand workflow entity
4. ✅ Read `@import(features/node-types.md)` - Understand node types
5. ✅ Verify Sprint 03 and 04 are completed

## Tasks

### DTOs and Mappers
- [ ] Create `WorkflowDTO.java` (response)
- [ ] Create `WorkflowRequestDTO.java` (create/update request)
- [ ] Create `WorkflowListDTO.java` (for list responses)
- [ ] Create `WorkflowMapper.java` using MapStruct

### Service Layer
- [ ] Create `WorkflowService` interface - See `@import(technical/backend/service-interfaces.md#workflow-service)`
- [ ] Implement `WorkflowServiceImpl`:
  - `createWorkflow()`, `getWorkflowById()`, `getAllWorkflows()`, `updateWorkflow()`, `deleteWorkflow()`
  - `activateWorkflow()`, `deactivateWorkflow()`, `pauseWorkflow()`, `resumeWorkflow()`
  - `getWorkflowVersions()`, `rollbackWorkflow()`

### Workflow Validation
- [ ] Create `WorkflowValidator.java`:
  - Verify workflow has exactly one trigger node
  - Verify all nodes have valid types - See `@import(features/node-types.md)`
  - Verify all edges connect valid nodes
  - Verify no circular dependencies
  - Verify trigger node references valid trigger config
  - Verify action nodes reference valid action registry entries

### Workflow Versioning
- [ ] Implement version increment on update
- [ ] Implement version comparison
- [ ] Implement rollback functionality

### Workflow Status Management
- [ ] Implement status transitions - See `@import(features/workflow-builder.md#workflow-status)`
- [ ] Validate status transitions
- [ ] Handle trigger activation/deactivation on status change

## Deliverables

- ✅ Complete workflow CRUD API
- ✅ Workflow validation working
- ✅ Workflow versioning working
- ✅ Workflow status management working

## Technical Details

### Workflow Definition Structure
See `@import(features/workflow-builder.md)` for workflow definition structure.

### API Contracts
See `@import(api/endpoints.md#workflows)` for API request/response formats.

## Compliance Verification

- [ ] Verify API endpoints match `@import(api/endpoints.md#workflows)` exactly
- [ ] Verify workflow definition structure matches `@import(features/workflow-builder.md)`
- [ ] Verify validation rules match specifications
- [ ] Test all CRUD operations

## Related Documentation

- `@import(features/workflow-builder.md)` ⚠️ **MUST MATCH**
- `@import(api/endpoints.md#workflows)` ⚠️ **MUST MATCH**
- `@import(features/node-types.md)`
- `@import(technical/backend/service-interfaces.md#workflow-service)`
