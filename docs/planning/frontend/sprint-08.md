# Sprint 08: Workflow List & Details

## Goal
Implement workflow list page and workflow details page, ensuring compliance with API specifications.

## Phase
Core Features

## Complexity
Medium

## Dependencies
Sprint 02, Sprint 03

## Compliance Check

### Before Starting
1. ✅ Read `@import(api/endpoints.md#workflows)` - Understand workflow API
2. ✅ Verify Sprint 02 and 03 are completed

## Tasks

### Workflow List Page
- [ ] Create `WorkflowList.tsx` page
- [ ] Implement workflow table:
  - [ ] Display workflow name, status, version
  - [ ] Display created/updated dates
  - [ ] Display tags
- [ ] Implement filtering:
  - [ ] Filter by status
  - [ ] Filter by tags
  - [ ] Search by name/description
- [ ] Implement pagination
- [ ] Add actions:
  - [ ] View workflow
  - [ ] Edit workflow
  - [ ] Delete workflow
  - [ ] Activate/Deactivate workflow

### Workflow Details Page
- [ ] Create `WorkflowDetails.tsx` page
- [ ] Display workflow information:
  - [ ] Name, description, status
  - [ ] Version history
  - [ ] Tags
  - [ ] Created/updated dates
- [ ] Add action buttons:
  - [ ] Edit workflow
  - [ ] Open in builder
  - [ ] Activate/Deactivate
  - [ ] Delete workflow
- [ ] Display workflow definition preview
- [ ] Display recent executions

### Workflow Actions
- [ ] Implement workflow CRUD:
  - [ ] Create workflow
  - [ ] Update workflow
  - [ ] Delete workflow
- [ ] Implement status management:
  - [ ] Activate workflow
  - [ ] Deactivate workflow
  - [ ] Pause workflow
  - [ ] Resume workflow
- [ ] Implement version management:
  - [ ] View versions
  - [ ] Rollback to version

## Deliverables

- ✅ Workflow list page implemented
- ✅ Workflow details page implemented
- ✅ All workflow actions working

## Technical Details

### Workflow API
- **Endpoints**: `@import(api/endpoints.md#workflows)` ⚠️ **MUST MATCH**
- **Schemas**: `@import(api/schemas.md)`

## Compliance Verification

- [ ] Verify API calls match `@import(api/endpoints.md#workflows)`
- [ ] Test all CRUD operations
- [ ] Test status management

## Related Documentation

- `@import(api/endpoints.md#workflows)` ⚠️ **MUST MATCH**
- `@import(api/schemas.md)`

