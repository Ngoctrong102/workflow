# Sprint 23: Async Event Aggregation - Service & Node Executor

## Goal
Complete async event aggregation implementation with service integration and node executor.

## Phase
Extended Features

## Complexity
Medium

## Dependencies
Sprint 22

## Compliance Check

### Before Starting
1. ✅ Read `@import(technical/integration/async-event-aggregation.md)` - Understand async event aggregation
2. ✅ Verify Sprint 22 is completed

## Tasks

### Event Aggregation Service
- [ ] Create `EventAggregationService.java`:
  - `waitForEvents()` - Wait for multiple async events
  - `aggregateEvents()` - Aggregate events when all received
  - `handleTimeout()` - Handle timeout scenarios
  - `handleEventReceived()` - Handle individual event received

### Wait for Events Node Executor Enhancement
- [ ] Enhance `WaitForEventsNodeExecutor`:
  - Create correlation ID
  - Create wait state with multiple event types
  - Register callback endpoints
  - Wait for all events
  - Aggregate results when all received
  - Handle timeout

### Event Matching
- [ ] Implement event matching by correlation ID
- [ ] Support multiple event types per wait state
- [ ] Track received events

### Timeout Handling
- [ ] Implement timeout handling for wait states
- [ ] Mark wait state as timeout if not completed
- [ ] Continue execution with partial data or error

## Deliverables

- ✅ Event aggregation service fully implemented
- ✅ Wait for Events node executor enhanced
- ✅ Event matching working
- ✅ Timeout handling working

## Technical Details

### Async Event Aggregation
See `@import(technical/integration/async-event-aggregation.md)`.

## Compliance Verification

- [ ] Verify event aggregation matches specifications
- [ ] Test wait for events with multiple event types
- [ ] Test timeout handling

## Related Documentation

- `@import(technical/integration/async-event-aggregation.md)` ⚠️ **MUST MATCH**

