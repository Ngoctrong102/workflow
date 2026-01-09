# Sprint 02: Database Schema & JPA Entities

## Goal
Create database schema via Flyway migrations and JPA entity classes, ensuring 100% compliance with database schema documentation.

## Phase
Foundation

## Complexity
Medium

## Dependencies
Sprint 01

## Compliance Check

### Before Starting
1. ✅ Read `@import(database-schema/entities.md)` - Understand all entity structures
2. ✅ Read `@import(database-schema/relationships.md)` - Understand entity relationships
3. ✅ Read `@import(database-schema/indexes.md)` - Understand index requirements
4. ✅ Verify Sprint 01 is completed

## Tasks

### Enums
- [ ] Create enums package: `com.notificationplatform.enums`
- [ ] Create `WorkflowStatus.java` (DRAFT, ACTIVE, INACTIVE, PAUSED, ARCHIVED)
- [ ] Create `ExecutionStatus.java` (RUNNING, WAITING, PAUSED, COMPLETED, FAILED, CANCELLED)
- [ ] Create `NodeExecutionStatus.java` (RUNNING, WAITING, PAUSED, COMPLETED, FAILED)
- [ ] Create `TriggerType.java` (API_CALL, SCHEDULER, EVENT)
- [ ] Create `ActionType.java` (API_CALL, PUBLISH_EVENT, FUNCTION, CUSTOM_ACTION)
- [ ] Create `RetryStrategy.java` (IMMEDIATE, EXPONENTIAL_BACKOFF, FIXED_DELAY, CUSTOM)
- [ ] Create `RetryStatus.java` (PENDING, SCHEDULED, RETRYING, COMPLETED, FAILED, CANCELLED)

### Flyway Migrations
- [ ] Create `V1__create_workflows_table.sql` - See `@import(database-schema/entities.md#workflows)`
- [ ] Create `V2__create_triggers_table.sql` - See `@import(database-schema/entities.md#triggers)`
- [ ] Create `V3__create_actions_table.sql` - See `@import(database-schema/entities.md#actions)`
- [ ] Create `V4__create_executions_table.sql` - See `@import(database-schema/entities.md#executions)`
- [ ] Create `V5__create_node_executions_table.sql` - See `@import(database-schema/entities.md#node-executions)`
- [ ] Create `V6__create_retry_schedules_table.sql` - See `@import(database-schema/entities.md#retry-schedules)`
- [ ] Create `V7__create_execution_wait_states_table.sql` - See `@import(database-schema/entities.md#execution-wait-states)`
- [ ] Create `V8__create_analytics_daily_table.sql` - See `@import(database-schema/entities.md#analytics-aggregates)`
- [ ] Create `V9__create_workflow_reports_tables.sql` - See `@import(database-schema/entities.md#workflow-reports)`

### JPA Entities
- [ ] Create `Workflow.java` - See `@import(database-schema/entities.md#workflows)`
- [ ] Create `Trigger.java` - See `@import(database-schema/entities.md#triggers)`
- [ ] Create `Action.java` - See `@import(database-schema/entities.md#actions)`
- [ ] Create `Execution.java` - See `@import(database-schema/entities.md#executions)`
- [ ] Create `NodeExecution.java` - See `@import(database-schema/entities.md#node-executions)`
- [ ] Create `RetrySchedule.java` - See `@import(database-schema/entities.md#retry-schedules)`
- [ ] Create `ExecutionWaitState.java` - See `@import(database-schema/entities.md#execution-wait-states)`
- [ ] Create `AnalyticsDaily.java` - See `@import(database-schema/entities.md#analytics-aggregates)`
- [ ] Create `WorkflowReport.java` - See `@import(database-schema/entities.md#workflow-reports)`
- [ ] Create `WorkflowReportHistory.java` - See `@import(database-schema/entities.md#workflow-reports)`

### Entity Relationships
- [ ] Add relationships matching `@import(database-schema/relationships.md)`:
  - Workflow → Triggers (OneToMany)
  - Workflow → Executions (OneToMany)
  - Workflow → WorkflowReport (OneToOne)
  - Execution → NodeExecutions (OneToMany)
  - Execution → RetrySchedules (OneToMany)
  - Execution → ExecutionWaitStates (OneToMany)
  - Trigger → Executions (OneToMany)

## Deliverables

- ✅ All database tables created via Flyway migrations
- ✅ All JPA entities matching database schema exactly
- ✅ Entity relationships properly defined
- ✅ All indexes created
- ✅ Migrations run successfully

## Technical Details

### Entity Structure
See `@import(database-schema/entities.md)` for exact field definitions.

### Relationships
See `@import(database-schema/relationships.md)` for relationship details.

## Compliance Verification

- [ ] Verify all migrations run successfully
- [ ] Verify database schema matches `@import(database-schema/entities.md)` exactly
- [ ] Verify entity relationships match `@import(database-schema/relationships.md)` exactly
- [ ] Test entity persistence and retrieval

## Related Documentation

- `@import(database-schema/entities.md)` ⚠️ **MUST MATCH EXACTLY**
- `@import(database-schema/relationships.md)` ⚠️ **MUST MATCH EXACTLY**
- `@import(database-schema/indexes.md)` ⚠️ **MUST MATCH EXACTLY**
