# Sprint 08: Workflow Engine - Action Node Executors

## Goal
Implement action node executors (API Call, Publish Event, Function, Custom Actions), ensuring compliance with action registry specifications.

## Phase
Core Services

## Complexity
Medium

## Dependencies
Sprint 06, Sprint 07

## Compliance Check

### Before Starting
1. ✅ Read `@import(features/node-types.md)` - Understand action node types
2. ✅ Read `@import(features/action-registry.md)` - Understand action registry
3. ✅ Verify Sprint 06 and 07 are completed

## Tasks

### API Call Action Executor
- [ ] Create `ApiCallNodeExecutor.java`:
  - Parse node config (URL, method, headers, body)
  - Make HTTP request using RestTemplate or WebClient
  - Handle response
  - Support retry configuration
  - See `@import(features/node-types.md#api-call-action)`

### Publish Event Action Executor
- [ ] Create `PublishEventNodeExecutor.java`:
  - Parse Kafka configuration
  - Publish message to Kafka topic
  - Use `KafkaTemplate` from configuration
  - See `@import(features/node-types.md#publish-event-action-kafka)`

### Function Action Executor
- [ ] Create `FunctionNodeExecutor.java`:
  - Parse expression from config
  - Evaluate expression using expression evaluator
  - Support arithmetic, string, date, array, object operations
  - See `@import(features/node-types.md#function-action)`

### Custom Action Executors
- [ ] Create `SendEmailActionExecutor.java`:
  - Parse recipients, subject, body from node config
  - Implement SMTP email sending logic
  - Handle email delivery status
  - See `@import(features/node-types.md#send-email)`
- [ ] Create `SendSmsActionExecutor.java`:
  - Parse recipients, message from node config
  - Implement SMS provider integration (Twilio, etc.)
  - Handle SMS delivery status
- [ ] Create `SendPushActionExecutor.java`:
  - Parse push payload, target devices from node config
  - Implement FCM/APNS integration
  - Handle push delivery status
- [ ] Create `SendInAppActionExecutor.java`:
  - Parse in-app message config from node config
  - Implement in-app notification logic
- [ ] Create `SendSlackActionExecutor.java`:
  - Parse Slack channel, message from node config
  - Implement Slack API integration
- [ ] Create `SendDiscordActionExecutor.java`:
  - Parse Discord channel, message from node config
  - Implement Discord API integration
- [ ] Create `SendTeamsActionExecutor.java`:
  - Parse Teams channel, message from node config
  - Implement Teams API integration
- [ ] Create `SendWebhookActionExecutor.java`:
  - Parse webhook URL, payload from node config
  - Implement HTTP webhook sending
- [ ] Create `WaitForEventsActionExecutor.java` (reuse from Sprint 07)

### Action Executor Factory
- [ ] Create `ActionExecutorFactory.java`:
  - Get executor by action registry ID
  - Load action from registry
  - Route to appropriate executor

### Retry Support
- [ ] Integrate Resilience4j retry mechanism
- [ ] Support retry configuration from node config
- [ ] See `@import(features/node-types.md#node-retry-configuration)`

## Deliverables

- ✅ All action node executors implemented
- ✅ API Call, Publish Event, Function actions working
- ✅ Custom actions fully implemented with notification sending logic
- ✅ Retry mechanism integrated

## Technical Details

### Action Node Specifications
See `@import(features/node-types.md)` for detailed action specifications.

### Retry Configuration
See `@import(features/node-types.md#node-retry-configuration)`.

## Compliance Verification

- [ ] Verify all action nodes match `@import(features/node-types.md)` specifications
- [ ] Test API call action with various HTTP methods
- [ ] Test publish event action with Kafka
- [ ] Test function action with expressions

## Related Documentation

- `@import(features/node-types.md)` ⚠️ **MUST MATCH**
- `@import(features/action-registry.md)`
- `@import(features/workflow-execution-state.md#node-retry-configuration)`
