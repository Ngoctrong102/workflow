# Sprint 15: API Controllers - Workflow & Execution

## Goal
Implement REST API controllers for workflow and execution management, ensuring compliance with API specifications.

## Phase
API Layer

## Complexity
Medium

## Dependencies
Sprint 05, Sprint 06

## Compliance Check

### Before Starting
1. ✅ Read `@import(api/endpoints.md#workflows)` - Understand workflow API
2. ✅ Read `@import(api/endpoints.md#executions)` - Understand execution API
3. ✅ Read `@import(api/schemas.md)` - Understand request/response schemas
4. ✅ Read `@import(api/error-handling.md)` - Understand error handling
5. ✅ Verify Sprint 05 and 06 are completed

## Tasks

### Workflow Controller
- [ ] Create `WorkflowController.java`:
  - `POST /workflows` - Create workflow
  - `GET /workflows` - List workflows
  - `GET /workflows/{id}` - Get workflow
  - `PUT /workflows/{id}` - Update workflow
  - `DELETE /workflows/{id}` - Delete workflow
  - `POST /workflows/{id}/activate` - Activate workflow
  - `POST /workflows/{id}/deactivate` - Deactivate workflow
  - `POST /workflows/{id}/pause` - Pause workflow
  - `POST /workflows/{id}/resume` - Resume workflow
  - `GET /workflows/{id}/versions` - Get versions
  - `POST /workflows/{id}/rollback` - Rollback to version
  - **MUST MATCH**: `@import(api/endpoints.md#workflows)`

### Execution Controller
- [ ] Create `ExecutionController.java`:
  - `GET /executions/{id}` - Get execution details
  - `GET /executions` - List executions
  - `GET /executions/{id}/visualize` - Get execution for visualization
  - `POST /executions/{id}/visualize/step` - Execute next step
  - `GET /executions/{id}/visualize/step/{stepNumber}` - Get execution state at step
  - `POST /executions/{id}/visualize/reset` - Reset visualization
  - `GET /executions/{id}/visualize/context` - Get current context
  - **MUST MATCH**: `@import(api/endpoints.md#executions)` and `@import(api/endpoints.md#execution-visualization)`

### Request/Response DTOs
- [ ] Create request DTOs: `CreateWorkflowRequestDTO`, `UpdateWorkflowRequestDTO`
- [ ] Create response DTOs: `WorkflowDTO`, `ExecutionDTO`, `ExecutionVisualizationDTO`
- [ ] Use MapStruct for DTO mapping

### Validation
- [ ] Add `@Valid` annotations to request DTOs
- [ ] Create custom validators if needed

## Deliverables

- ✅ Workflow API fully implemented
- ✅ Execution API fully implemented
- ✅ API endpoints match specifications
- ✅ Request/response validation working

## Technical Details

### API Endpoints
- **Workflows**: `@import(api/endpoints.md#workflows)` ⚠️ **MUST MATCH**
- **Executions**: `@import(api/endpoints.md#executions)` ⚠️ **MUST MATCH**
- **Execution Visualization**: `@import(api/endpoints.md#execution-visualization)` ⚠️ **MUST MATCH**

### Request/Response Formats
- **Schemas**: `@import(api/schemas.md)` ⚠️ **MUST MATCH**

## Compliance Verification

- [ ] Verify API endpoints match `@import(api/endpoints.md)` exactly
- [ ] Test all CRUD operations
- [ ] Test execution visualization endpoints

## Related Documentation

- `@import(api/endpoints.md#workflows)` ⚠️ **MUST MATCH**
- `@import(api/endpoints.md#executions)` ⚠️ **MUST MATCH**
- `@import(api/endpoints.md#execution-visualization)` ⚠️ **MUST MATCH**
- `@import(api/schemas.md)` ⚠️ **MUST MATCH**
- `@import(api/error-handling.md)` ⚠️ **MUST MATCH**

