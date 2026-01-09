# Async Event Aggregation - Solution Summary

## Problem

Khi tích hợp với external service, workflow cần:
1. Gọi API external service
2. Nhận API response (có thể đến trước hoặc sau)
3. Chờ Kafka Event (có thể đến trước hoặc sau)
4. Chạy action tiếp theo chỉ khi **cả 2 events đã nhận được**

**Vấn đề**: API response và Kafka event có thể đến theo thứ tự bất kỳ, tạo ra race condition.

## Giải Pháp

### Pattern: Event Aggregation với State Management (Multi-Instance Safe)

Sử dụng node type mới **"Wait for Events"** với các tính năng:

1. **Linh hoạt về Events**:
   - Có thể bật/tắt API call hoặc Kafka event
   - Có thể chỉ cần 1 trong 2, hoặc cả 2, hoặc không cần chờ

2. **Correlation Mechanism**:
   - Mỗi execution có correlation ID riêng
   - Kafka event phải có cả `correlation_id` và `execution_id`
   - Đảm bảo event đúng flow, tránh trigger flow khác

3. **Multi-Instance Safe**:
   - Database-based state management (shared state)
   - Optimistic locking để tránh conflict
   - Kafka consumer groups phân phối load
   - Idempotency để tránh duplicate processing

4. **Aggregation Strategies**:
   - `all`: Chờ tất cả enabled events
   - `any`: Chờ bất kỳ event nào (whichever comes first)
   - `required_only`: Chỉ chờ events marked as required
   - `custom`: Chờ specific events theo config

## Kiến Trúc

```
┌─────────────────────────────────────────────────────────┐
│              Wait for Events Node                        │
├─────────────────────────────────────────────────────────┤
│ 1. Generate Correlation ID                              │
│ 2. Register Wait State                                  │
│ 3. Call External API (with correlation ID)               │
│ 4. Subscribe to Kafka Topic (with correlation ID)       │
│ 5. Mark Node as "waiting_for_events"                    │
└─────────────────────────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────┐
│              Event Reception (Any Order)                │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  ┌──────────────────┐      ┌──────────────────┐         │
│  │ API Response     │      │ Kafka Event     │         │
│  │ (via callback)   │      │ (via consumer)  │         │
│  └────────┬─────────┘      └────────┬─────────┘        │
│           │                          │                  │
│           └──────────┬───────────────┘                  │
│                      ▼                                  │
│         EventAggregationService                         │
│         - Store received event                           │
│         - Check if both received                        │
│         - Resume workflow if ready                      │
└─────────────────────────────────────────────────────────┘
```

## Components

### 1. Database Table: `execution_wait_states`
- Lưu trạng thái đợi của execution
- Track correlation ID
- Lưu API response và Kafka event data
- Hỗ trợ timeout handling

### 2. Service: `EventAggregationService`
- Quản lý wait states
- Xử lý API response callbacks
- Xử lý Kafka events
- Kiểm tra và resume execution

### 3. Node Executor: `WaitForEventsNodeExecutor`
- Execute node "wait_events"
- Generate correlation ID
- Make API call
- Register wait state
- Return waiting status

### 4. Enhanced Kafka Consumer
- Match events với correlation ID
- Forward events đến EventAggregationService

### 5. API Callback Controller
- Nhận API response callbacks
- Extract correlation ID
- Forward đến EventAggregationService

## Data Flow

### Scenario 1: API Response đến trước

```
1. WaitForEventsNodeExecutor → Call API (correlation_id: "abc123")
2. WaitForEventsNodeExecutor → Register wait state
3. Node status: "waiting_for_events"
4. [API Response arrives first]
5. ApiCallbackController → EventAggregationService.handleApiResponse()
6. EventAggregationService → Store API response, status: "waiting_for_kafka"
7. [Kafka Event arrives later]
8. KafkaConsumer → EventAggregationService.handleKafkaEvent()
9. EventAggregationService → Both received! Resume execution
10. WorkflowExecutor → Continue with next node
```

### Scenario 2: Kafka Event đến trước

```
1. WaitForEventsNodeExecutor → Call API (correlation_id: "abc123")
2. WaitForEventsNodeExecutor → Register wait state
3. Node status: "waiting_for_events"
4. [Kafka Event arrives first]
5. KafkaConsumer → EventAggregationService.handleKafkaEvent()
6. EventAggregationService → Store Kafka event, status: "waiting_for_api"
7. [API Response arrives later]
8. ApiCallbackController → EventAggregationService.handleApiResponse()
9. EventAggregationService → Both received! Resume execution
10. WorkflowExecutor → Continue with next node
```

## Configuration Examples

### Example 1: Chờ cả 2 events (default)
```json
{
  "type": "wait_events",
  "data": {
    "apiCall": {
      "enabled": true,
      "url": "https://external-service.com/api/action",
      "method": "POST",
      "body": {
        "action": "process",
        "data": "{{input_data}}"
        // execution_id và correlation_id tự động inject
      },
      "required": true
    },
    "kafkaEvent": {
      "enabled": true,
      "topic": "external-service.events",
      "correlationIdField": "correlation_id",
      "executionIdField": "execution_id",  // Required for safety
      "filter": {
        "event_type": "action.completed"
      },
      "required": true
    },
    "aggregationStrategy": "all",
    "timeout": 300
  }
}
```

### Example 2: Chờ bất kỳ event nào
```json
{
  "type": "wait_events",
  "data": {
    "apiCall": {
      "enabled": true,
      "url": "https://external-service.com/api/action",
      "required": false
    },
    "kafkaEvent": {
      "enabled": true,
      "topic": "external-service.events",
      "required": false
    },
    "aggregationStrategy": "any",
    "timeout": 300
  }
}
```

### Example 3: Chỉ API call, không cần Kafka event
```json
{
  "type": "wait_events",
  "data": {
    "apiCall": {
      "enabled": true,
      "url": "https://external-service.com/api/action",
      "required": true
    },
    "kafkaEvent": {
      "enabled": false
    },
    "aggregationStrategy": "all"
  }
}
```

## Benefits

1. **Race Condition Safe**: Không quan trọng thứ tự events
2. **Execution Isolation**: Execution ID validation đảm bảo event đúng flow
3. **Multi-Instance Safe**: Hoạt động đúng khi deploy nhiều instances
4. **Flexible**: Nhiều strategies, có thể bật/tắt từng event
5. **Scalable**: Database-based state, Kafka consumer groups
6. **Reliable**: Timeout handling, error recovery, idempotency
7. **Observable**: Track wait states, metrics, distributed tracing

## Implementation Priority

**High Priority** - Cần thiết cho tích hợp external services với async events.

## Related Documentation

- [Full Technical Specification](./async-event-aggregation.md) - Chi tiết implementation
- [Integration Requirements](./integration-requirements.md) - General integration patterns
- [Workflow Builder](../../features/workflow-builder.md) - Workflow builder feature

