# Sprint 22: Async Event Aggregation - Database & Entities

## Goal
Implement database schema and entities for async event aggregation (Wait for Events node).

## Phase
Extended Features

## Complexity
Medium

## Dependencies
Sprint 07

## Compliance Check

### Before Starting
1. ✅ Read `@import(technical/integration/async-event-aggregation.md)` - Understand async event aggregation
2. ✅ Read `@import(api/endpoints.md#callback-endpoints)` - Understand callback API
3. ✅ Verify Sprint 07 is completed

## Tasks

### Database Schema
- [ ] Verify `execution_wait_states` table exists (from Sprint 02)
- [ ] Add indexes if needed for correlation_id queries

### Execution Wait State Service
- [ ] Create `ExecutionWaitStateService.java`:
  - `createWaitState()` - Create wait state for node
  - `updateWaitState()` - Update wait state when event received
  - `getWaitStateByCorrelationId()` - Get wait state by correlation ID
  - `checkTimeouts()` - Check for expired wait states

### Callback Endpoint
- [ ] Create `CallbackController.java`:
  - `POST /callback/api/{correlationId}` - Receive API callback
  - `POST /callback/kafka/{correlationId}` - Receive Kafka event callback
  - Update wait state when callback received
  - **MUST MATCH**: `@import(api/endpoints.md#callback-endpoints)`

### Kafka Consumer Enhancement
- [ ] Enhance Kafka consumer to handle correlation IDs
- [ ] Route events to appropriate wait states

## Deliverables

- ✅ Execution wait state service implemented
- ✅ Callback endpoints working
- ✅ Kafka consumer enhanced for correlation IDs

## Technical Details

### Async Event Aggregation
- **Features**: `@import(technical/integration/async-event-aggregation.md)` ⚠️ **MUST MATCH**

### Callback API
- **Endpoints**: `@import(api/endpoints.md#callback-endpoints)` ⚠️ **MUST MATCH**

## Compliance Verification

- [ ] Verify wait state management matches specifications
- [ ] Test callback endpoints
- [ ] Test Kafka event routing

## Related Documentation

- `@import(technical/integration/async-event-aggregation.md)` ⚠️ **MUST MATCH**
- `@import(api/endpoints.md#callback-endpoints)` ⚠️ **MUST MATCH**

