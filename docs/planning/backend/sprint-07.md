# Sprint 07: Workflow Engine - Logic Node Executors

## Goal
Implement logic node executors (Condition, Switch, Loop, Delay, Wait for Events, Merge), ensuring compliance with node type specifications.

## Phase
Core Services

## Complexity
Medium

## Dependencies
Sprint 06

## Compliance Check

### Before Starting
1. ✅ Read `@import(features/node-types.md)` - Understand logic node types
2. ✅ Read `@import(features/workflow-execution-state.md)` - Understand pause/resume
3. ✅ Verify Sprint 06 is completed

## Tasks

### Condition Node Executor
- [ ] Create `ConditionNodeExecutor.java`:
  - Implement `NodeExecutor` interface
  - Evaluate conditions (field comparison, AND/OR logic)
  - Return true/false branch based on condition
  - See `@import(features/node-types.md#condition)`

### Switch Node Executor
- [ ] Create `SwitchNodeExecutor.java`:
  - Evaluate switch field
  - Match against cases
  - Return appropriate branch or default
  - See `@import(features/node-types.md#switch)`

### Loop Node Executor
- [ ] Create `LoopNodeExecutor.java`:
  - Iterate over array field
- [ ] Process each item in sequence
- [ ] Accumulate results
- [ ] See `@import(features/node-types.md#loop)`

### Delay Node Executor
- [ ] Create `DelayNodeExecutor.java`:
  - Calculate delay duration
  - Create `ExecutionWaitState` for pause
  - Persist context to database
  - Schedule resume task
  - See `@import(features/workflow-execution-state.md#delay-node-with-persistent-state)`

### Wait for Events Node Executor
- [ ] Create `WaitForEventsNodeExecutor.java`:
  - Create `ExecutionWaitState` with correlation_id
  - Wait for multiple async events (API response + Kafka event)
  - Aggregate results when all events received
  - Handle timeout
  - See `@import(technical/integration/async-event-aggregation.md)`

### Merge Node Executor
- [ ] Create `MergeNodeExecutor.java`:
  - Combine multiple branches
  - Implement merge strategies (all, first, last, custom)
  - See `@import(features/node-types.md#merge)`

### Node Executor Registry
- [ ] Create `NodeExecutorRegistry.java`:
  - Register node executors by node type
  - Get executor by node type
  - Use in `WorkflowExecutor` to route to correct executor

## Deliverables

- ✅ All logic node executors implemented
- ✅ Condition, Switch, Loop, Delay, Wait for Events, Merge working
- ✅ Delay node supports pause/resume
- ✅ Wait for Events node supports async aggregation

## Technical Details

### Logic Node Specifications
See `@import(features/node-types.md)` for detailed node specifications.

### Delay Node Implementation
See `@import(features/workflow-execution-state.md#delay-node-with-persistent-state)`.

### Wait for Events Implementation
See `@import(technical/integration/async-event-aggregation.md)`.

## Compliance Verification

- [ ] Verify all logic nodes match `@import(features/node-types.md)` specifications
- [ ] Test condition node with various conditions
- [ ] Test delay node pause/resume
- [ ] Test wait for events async aggregation

## Related Documentation

- `@import(features/node-types.md)` ⚠️ **MUST MATCH**
- `@import(features/workflow-execution-state.md)`
- `@import(technical/integration/async-event-aggregation.md)`
