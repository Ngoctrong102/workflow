# Sprint 03: Core Repositories

## Goal
Implement Spring Data JPA repositories with custom queries, ensuring compliance with database schema and query requirements.

## Phase
Foundation

## Complexity
Simple

## Dependencies
Sprint 02

## Compliance Check

### Before Starting
1. ✅ Read `@import(database-schema/entities.md)` - Understand entity structures
2. ✅ Read `@import(features/execution-query-capabilities.md)` - Understand query requirements
3. ✅ Verify Sprint 02 is completed

## Tasks

### Repository Interfaces
- [ ] Create `WorkflowRepository.java`:
  - `findByStatusAndDeletedAtIsNull()`, `findByIdAndDeletedAtIsNull()`
  - `findAllActive()`, `searchByNameOrDescription()`, `findByTag()`
- [ ] Create `TriggerRepository.java`:
  - `findByWorkflowId()`, `findByWorkflowIdAndNodeId()`, `findByTriggerType()`
- [ ] Create `ActionRepository.java`:
  - `findByType()`, `findByActionType()`, `findAllEnabled()`
- [ ] Create `ExecutionRepository.java`:
  - `findByWorkflowId()`, `findByStatus()`, `findByStartedAtBetween()`
  - JSONB queries: `findByContextField()`, `findByTriggerDataField()` - See `@import(features/execution-query-capabilities.md)`
- [ ] Create `NodeExecutionRepository.java`:
  - `findByExecutionId()`, `findByExecutionIdOrderByStartedAtAsc()`, `findByNodeType()`
  - JSONB queries: `findByInputDataField()`, `findByOutputDataField()`
- [ ] Create `RetryScheduleRepository.java`:
  - `findByStatusIn()`, `findByScheduledAtLessThanEqualAndStatus()`
- [ ] Create `ExecutionWaitStateRepository.java`:
  - `findByCorrelationId()`, `findByExecutionIdAndNodeId()`, `findByExpiresAtLessThanEqualAndStatus()`
- [ ] Create `AnalyticsDailyRepository.java`:
  - `findByDateBetween()`, `findByWorkflowIdAndDateBetween()`
- [ ] Create `WorkflowReportRepository.java`:
  - `findByWorkflowId()`, `findByNextGenerationAtLessThanEqualAndStatus()`
- [ ] Create `WorkflowReportHistoryRepository.java`:
  - `findByWorkflowReportId()`, `findByGeneratedAtBetween()`

### Custom Queries
- [ ] Implement soft delete queries (WHERE deleted_at IS NULL)
- [ ] Implement JSONB field queries - See `@import(features/execution-query-capabilities.md)`
- [ ] Implement aggregation queries for analytics

## Deliverables

- ✅ All repository interfaces created
- ✅ Custom query methods implemented
- ✅ Soft delete queries working
- ✅ JSONB queries working
- ✅ Repository layer ready for service layer

## Technical Details

### Query Methods
See `@import(features/execution-query-capabilities.md)` for JSONB query examples.

## Compliance Verification

- [ ] Verify all repositories extend JpaRepository correctly
- [ ] Verify all custom queries work correctly
- [ ] Test all repository methods

## Related Documentation

- `@import(database-schema/entities.md)`
- `@import(features/execution-query-capabilities.md)`
- `@import(technical/backend/project-structure.md)`
