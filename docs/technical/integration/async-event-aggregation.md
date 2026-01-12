# Async Event Aggregation Integration Pattern

## Overview

This document defines the integration pattern for external services that require waiting for multiple asynchronous events (API response and Kafka event) where the order of arrival is not guaranteed. This pattern solves the race condition problem when integrating with external services that respond via both synchronous API calls and asynchronous Kafka events.

## Problem Statement

When integrating with an external service, the workflow needs flexible event handling:

1. **Optional API Call**: May or may not need to call external API
2. **Optional Kafka Event**: May or may not need to wait for Kafka event
3. **Flexible Waiting Strategy**: 
   - Wait for **all** events (both API response and Kafka event)
   - Wait for **any** event (whichever comes first)
   - Wait for **specific** events (custom combination)
   - **No waiting** (fire and forget)
4. **Event Correlation**: Kafka events must be correctly matched to the right workflow execution (avoid triggering wrong workflows)
5. **Multi-Instance Deployment**: System must work correctly when deployed across multiple instances

**Challenges**:
- API response and Kafka event can arrive in any order
- Events must be correctly correlated to avoid cross-execution contamination
- Multiple service instances must coordinate without conflicts
- Need flexible strategies for different integration scenarios

## Solution Architecture

### Pattern: Event Aggregation with State Management

The solution uses a **Wait for Multiple Events** node type that:
- Initiates the API call
- Registers listeners for both API response and Kafka event
- Maintains execution state while waiting
- Aggregates both events when received
- Resumes workflow execution only after both events are received

## Correlation Mechanism

### Problem: Event Routing to Correct Execution

Kafka events must be correctly matched to the right workflow execution to avoid:
- **Cross-execution contamination**: Event from execution A triggering execution B
- **Wrong workflow trigger**: Event intended for one workflow triggering another
- **Race conditions**: Multiple executions waiting for events with same correlation pattern

### Solution: Dual-Identifier Correlation

Use **both** `execution_id` and `correlation_id` for precise event matching:

1. **Execution ID**: Unique per workflow execution
   - Ensures event belongs to correct execution
   - Prevents cross-execution contamination
   - Stored in wait state record

2. **Correlation ID**: Unique per wait node within execution
   - Allows multiple wait nodes in same execution
   - Enables external service to include in response
   - Stored in wait state record

3. **Combined Matching**: Both IDs must match
   - Database query: `WHERE execution_id = ? AND correlation_id = ?`
   - Additional validation in service layer
   - Prevents false matches

### Correlation ID Generation

```java
// Per execution-node pair
String correlationId = UUID.randomUUID().toString();
String executionId = context.getExecutionId();

// Store in wait state
waitState.setExecutionId(executionId);
waitState.setCorrelationId(correlationId);

// Include in API call
apiCallBody.put("execution_id", executionId);
apiCallBody.put("correlation_id", correlationId);

// External service must include both in response
// Kafka event must include both in payload
```

### Event Validation Flow

```
1. Event arrives (API response or Kafka event)
   ↓
2. Extract execution_id and correlation_id
   ↓
3. Query database: execution_wait_states
   WHERE execution_id = ? AND correlation_id = ? AND status = 'waiting'
   ↓
4. If found: Validate execution_id matches wait state's execution_id
   ↓
5. If valid: Process event
   ↓
6. If not found or invalid: Log warning, ignore event
```

## Architecture Components

### 1. Wait for Multiple Events Node

A new workflow node type that handles async event aggregation.

#### Node Configuration

```json
{
  "type": "wait_events",
  "data": {
    "apiCall": {
      "enabled": true,  // Optional: false to skip API call
      "url": "https://external-service.com/api/action",
      "method": "POST",
      "headers": {
        "Authorization": "Bearer @{_vars.token}",
        "Content-Type": "application/json"
      },
      "body": {
        "action": "@{_trigger.action}",
        "data": "@{_trigger.data}",
        "execution_id": "@{_execution.id}",  // Include execution ID for correlation
        "correlation_id": "@{_execution.correlationId}"  // Auto-injected correlation ID
      },
      "correlationIdField": "correlation_id",  // Field name in request body
      "correlationIdHeader": "X-Correlation-Id",  // Optional: header name
      "timeout": 300,
      "required": true  // true: must receive, false: optional
    },
    "kafkaEvent": {
      "enabled": true,  // Optional: false to skip Kafka event
      "topic": "external-service.events",
      "correlationIdField": "correlation_id",  // Field name in event payload
      "executionIdField": "execution_id",  // Field name for execution ID (for safety)
      "filter": {
        "event_type": "action.completed"
      },
      "timeout": 300,
      "required": true  // true: must receive, false: optional
    },
    "aggregationStrategy": "all",  // all, any, required_only, custom
    "requiredEvents": ["api_response", "kafka_event"],  // For custom strategy
    "timeout": 300,  // Overall timeout
    "onTimeout": "fail",  // fail, continue, continue_with_partial
    "outputMapping": {
      "apiResponse": "api_response",
      "kafkaEvent": "kafka_event"
    }
  }
}
```

#### Aggregation Strategies

1. **`all`**: Wait for all enabled events (both API response and Kafka event if both enabled)
2. **`any`**: Continue when any enabled event arrives (whichever comes first)
3. **`required_only`**: Wait only for events marked as `required: true`
4. **`custom`**: Wait for specific events listed in `requiredEvents` array

#### Node Behavior

1. **Initialization Phase**:
   - Generate unique correlation ID (UUID) per execution
   - Generate execution-scoped correlation ID: `{execution_id}:{correlation_id}`
   - Register event listeners based on enabled events
   - Store execution state in database (with execution_id and correlation_id)
   - Make API call (if enabled) with both correlation_id and execution_id
   - Subscribe to Kafka topic (if enabled) with correlation filter

2. **Waiting Phase**:
   - Execution status: `waiting`
   - Node execution status: `waiting_for_events`
   - Listen for:
     - API response callback/webhook (if apiCall.enabled = true)
     - Kafka event with matching correlation_id AND execution_id (if kafkaEvent.enabled = true)
   - Use database-based state management (shared across instances)

3. **Event Reception** (Multi-Instance Safe):
   - When API response arrives:
     - Extract correlation_id and execution_id
     - **Database Lock**: Acquire row-level lock on wait state record
     - Validate execution_id matches (prevent cross-execution contamination)
     - Store response data atomically
     - Update execution state
     - Check completion condition based on strategy
     - Release lock
   - When Kafka event arrives:
     - Extract correlation_id and execution_id from event
     - **Database Lock**: Acquire row-level lock on wait state record
     - Validate execution_id matches (prevent cross-execution contamination)
     - Validate correlation_id matches
     - Store event data atomically
     - Update execution state
     - Check completion condition based on strategy
     - Release lock

4. **Completion Phase** (Idempotent):
   - When completion condition met:
     - **Database Lock**: Acquire lock to prevent duplicate processing
     - Check if already resumed (idempotency check)
     - Aggregate data from received events
     - Update node execution status: `completed`
     - Mark wait state as `completed`
     - Release lock
     - Resume workflow execution (only one instance will resume)

5. **Timeout Handling**:
   - Scheduled job checks expired wait states (runs on all instances, but only one processes each)
   - If timeout reached:
     - Based on `onTimeout` config:
       - `fail`: Mark node execution as `failed`, stop workflow
       - `continue`: Continue with available events
       - `continue_with_partial`: Continue with partial data, mark as warning

## Implementation Details

### Database Schema Changes

#### New Table: `execution_wait_states`

Stores execution states for nodes waiting for events. Designed for multi-instance deployment.

```sql
CREATE TABLE execution_wait_states (
    id VARCHAR(255) PRIMARY KEY,
    execution_id VARCHAR(255) NOT NULL REFERENCES executions(id) ON DELETE CASCADE,
    node_id VARCHAR(255) NOT NULL,
    correlation_id VARCHAR(255) NOT NULL,
    aggregation_strategy VARCHAR(50) NOT NULL,  -- 'all', 'any', 'required_only', 'custom'
    required_events JSONB,  -- Array of required event types
    enabled_events JSONB,  -- Array of enabled event types
    api_call_enabled BOOLEAN NOT NULL DEFAULT false,
    kafka_event_enabled BOOLEAN NOT NULL DEFAULT false,
    api_response_data JSONB,
    kafka_event_data JSONB,
    received_events JSONB,  -- Array of received event types: ['api_response', 'kafka_event']
    status VARCHAR(50) NOT NULL DEFAULT 'waiting',  -- waiting, completed, timeout, failed, resuming
    resumed_at TIMESTAMP,  -- Timestamp when execution was resumed (for idempotency)
    resumed_by VARCHAR(255),  -- Instance ID that resumed (for debugging)
    version INTEGER NOT NULL DEFAULT 1,  -- Optimistic locking version
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMP NOT NULL,
    UNIQUE(execution_id, node_id)
);

CREATE INDEX idx_execution_wait_states_correlation_id ON execution_wait_states(correlation_id);
CREATE INDEX idx_execution_wait_states_execution_id ON execution_wait_states(execution_id);
CREATE INDEX idx_execution_wait_states_status ON execution_wait_states(status) WHERE status = 'waiting';
CREATE INDEX idx_execution_wait_states_expires_at ON execution_wait_states(expires_at) WHERE status = 'waiting';
CREATE INDEX idx_execution_wait_states_exec_corr ON execution_wait_states(execution_id, correlation_id);
```

**Multi-Instance Safety Features**:
- `version` field for optimistic locking (prevent concurrent updates)
- `resumed_at` and `resumed_by` for idempotency checking
- Indexes optimized for waiting state queries
- Composite index on (execution_id, correlation_id) for fast event matching

### Service Layer

#### EventAggregationService

Service to manage event aggregation for waiting nodes.

**Responsibilities**:
- Register wait states
- Handle API response callbacks
- Handle Kafka event reception
- Check completion conditions
- Resume workflow execution
- Handle timeouts

**Key Methods**:

```java
public interface EventAggregationService {
    /**
     * Register a wait state for a node waiting for multiple events
     * Returns correlation ID for use in API calls and Kafka events
     */
    ExecutionWaitState registerWaitState(
        String executionId, 
        String nodeId, 
        WaitForEventsConfig config
    );
    
    /**
     * Handle API response callback
     * Validates execution_id and correlation_id to prevent cross-execution contamination
     */
    void handleApiResponse(
        String executionId,  // Required for validation
        String correlationId, 
        Map<String, Object> responseData
    );
    
    /**
     * Handle Kafka event
     * Validates execution_id and correlation_id to prevent cross-execution contamination
     */
    void handleKafkaEvent(
        String topic, 
        Map<String, Object> eventData  // Must contain execution_id and correlation_id
    );
    
    /**
     * Check if completion condition met and resume execution if ready
     * Uses optimistic locking to ensure only one instance resumes
     */
    boolean checkAndResumeExecution(String executionId, String nodeId);
    
    /**
     * Handle timeout for waiting execution
     * Scheduled job calls this, but only one instance processes each expired state
     */
    void handleTimeout(String executionId, String nodeId);
    
    /**
     * Get correlation ID for an execution/node pair
     * Used to inject into API calls
     */
    String getCorrelationId(String executionId, String nodeId);
}
```

### Node Executor

#### WaitForEventsNodeExecutor

Executor for the "wait_events" node type.

**Execution Flow**:

1. **Initialize**:
   - Generate correlation ID
   - Create wait state record
   - Register listeners
   - Make API call

2. **Wait**:
   - Mark node execution as `waiting_for_events`
   - Store wait state
   - Return control (execution pauses)

3. **Resume** (when events received):
   - Load wait state
   - Aggregate events
   - Update node execution
   - Continue workflow

**Implementation**:

```java
@Component
public class WaitForEventsNodeExecutor implements NodeExecutor {
    
    @Override
    public NodeExecutionResult execute(
        String nodeId, 
        Map<String, Object> nodeData, 
        ExecutionContext context
    ) {
        // Parse configuration
        WaitForEventsConfig config = parseConfig(nodeData);
        
        // Check if any events are enabled
        if (!config.isApiCallEnabled() && !config.isKafkaEventEnabled()) {
            // No events to wait for, continue immediately
            return new NodeExecutionResult(true, new HashMap<>(), false);
        }
        
        // Register wait state (generates correlation ID internally)
        ExecutionWaitState waitState = eventAggregationService.registerWaitState(
            context.getExecutionId(),
            nodeId,
            config
        );
        
        String correlationId = waitState.getCorrelationId();
        String executionId = context.getExecutionId();
        
        // Make API call if enabled
        if (config.isApiCallEnabled()) {
            ApiCallConfig apiCall = config.getApiCall();
            // Inject execution_id and correlation_id into request
            Map<String, Object> apiCallData = injectCorrelationData(
                apiCall.getBody(),
                executionId,
                correlationId,
                context
            );
            makeApiCall(apiCall, apiCallData, context);
        }
        
        // Kafka subscription is handled by CorrelationAwareKafkaConsumer
        // No explicit subscription needed - consumer filters by correlation_id
        
        // Return waiting result
        Map<String, Object> output = new HashMap<>();
        output.put("correlationId", correlationId);
        output.put("executionId", executionId);
        output.put("status", "waiting");
        output.put("waitStateId", waitState.getId());
        output.put("enabledEvents", config.getEnabledEvents());
        
        return new NodeExecutionResult(true, output, true); // isWaiting = true
    }
    
    private Map<String, Object> injectCorrelationData(
        Object body,
        String executionId,
        String correlationId,
        ExecutionContext context
    ) {
        // Inject execution_id and correlation_id into API call body
        // Supports both object and string (template) body types
        // ... implementation details
    }
}
```

### Kafka Event Handler

Enhanced Kafka consumer to handle correlation ID and execution ID matching. **Multi-instance safe**.

**Implementation**:

```java
@Component
public class CorrelationAwareKafkaConsumer {
    
    @KafkaListener(topics = "${kafka.topics:events}", 
                   groupId = "${kafka.consumer.group-id:notification-platform-consumer}")
    public void consumeEvent(
        ConsumerRecord<String, String> record, 
        Acknowledgment acknowledgment
    ) {
        try {
            Map<String, Object> eventData = parseEventData(record.value());
            
            // Extract correlation ID and execution ID (both required for safety)
            String correlationId = extractField(eventData, "correlation_id");
            String executionId = extractField(eventData, "execution_id");
            
            // CRITICAL: Both correlation_id and execution_id must be present
            // to prevent cross-execution contamination
            if (correlationId != null && executionId != null) {
                // Find waiting execution with BOTH correlation_id AND execution_id
                // This ensures event belongs to the correct execution
                ExecutionWaitState waitState = 
                    waitStateRepository.findByExecutionIdAndCorrelationIdAndStatus(
                        executionId,
                        correlationId, 
                        "waiting"
                    );
                
                if (waitState != null) {
                    // Validate that Kafka event is enabled for this wait state
                    if (waitState.isKafkaEventEnabled()) {
                        // Handle event for waiting execution
                        // This method validates execution_id again internally
                        eventAggregationService.handleKafkaEvent(
                            record.topic(),
                            eventData
                        );
                    } else {
                        logger.warn("Kafka event received but not enabled for wait state: executionId={}, nodeId={}", 
                                   executionId, waitState.getNodeId());
                    }
                } else {
                    logger.debug("No waiting execution found for correlationId={}, executionId={}", 
                               correlationId, executionId);
                }
            } else {
                logger.debug("Event missing correlation_id or execution_id, skipping wait state matching");
            }
            
            // Also process as regular event trigger (existing logic)
            // This allows events without correlation to still trigger workflows
            processAsEventTrigger(record);
            
            acknowledgment.acknowledge();
        } catch (Exception e) {
            logger.error("Error processing Kafka event", e);
            // In production, implement retry logic or dead letter queue
        }
    }
    
    private String extractField(Map<String, Object> data, String fieldName) {
        // Support nested field paths like "data.correlation_id"
        // ... implementation
    }
}
```

**Multi-Instance Safety**:
- Kafka consumer group ensures each event is processed by only one instance
- Database query uses both `execution_id` and `correlation_id` for precise matching
- Additional validation in `EventAggregationService` prevents cross-execution contamination

### API Response Handler

Endpoint to receive API response callbacks from external service. **Multi-instance safe**.

**Implementation**:

```java
@RestController
@RequestMapping("/api/internal/callbacks")
public class ApiCallbackController {
    
    @PostMapping("/external-service")
    public ResponseEntity<Void> handleApiCallback(
        @RequestBody Map<String, Object> responseData,
        @RequestHeader(value = "X-Correlation-Id", required = false) String correlationIdHeader,
        @RequestHeader(value = "X-Execution-Id", required = false) String executionIdHeader
    ) {
        // Extract correlation ID and execution ID from headers or body
        String correlationId = correlationIdHeader != null ? correlationIdHeader : 
                              (String) responseData.get("correlation_id");
        String executionId = executionIdHeader != null ? executionIdHeader : 
                             (String) responseData.get("execution_id");
        
        // CRITICAL: Both correlation_id and execution_id required for safety
        if (correlationId == null || executionId == null) {
            logger.warn("API callback missing correlation_id or execution_id: correlationId={}, executionId={}", 
                       correlationId, executionId);
            return ResponseEntity.badRequest().build();
        }
        
        // Handle API response with validation
        // This method validates execution_id matches to prevent cross-execution contamination
        eventAggregationService.handleApiResponse(executionId, correlationId, responseData);
        
        return ResponseEntity.ok().build();
    }
}
```

**Multi-Instance Safety**:
- Any instance can receive the callback (load balanced)
- Database-based state management ensures correct handling
- Execution ID validation prevents cross-execution contamination

### Workflow Executor Enhancement

Modify `WorkflowExecutor` to handle waiting nodes:

1. **Check for waiting nodes** before marking execution as completed
2. **Resume execution** when events are received
3. **Handle timeouts** via scheduled job

**Changes**:

```java
// In WorkflowExecutor.executeNodeAndDependencies()
if (result.isWaiting()) {
    // Node is waiting for events, don't continue execution
    // Execution will be resumed when events are received
    return count;
}
```

## Data Flow

### Sequence Diagram

```
Workflow Execution:
1. Workflow Executor → WaitForEventsNodeExecutor.execute()
2. WaitForEventsNodeExecutor → EventAggregationService.registerWaitState()
3. WaitForEventsNodeExecutor → External API (with correlation ID)
4. WaitForEventsNodeExecutor → Kafka Consumer (subscribe with correlation ID)
5. Node Execution Status: "waiting_for_events"
6. Execution Status: "waiting"

Event Reception (Order 1: API Response First):
7. External Service → ApiCallbackController.handleApiCallback()
8. ApiCallbackController → EventAggregationService.handleApiResponse()
9. EventAggregationService → Check if Kafka event also received
10. If not, store API response, wait for Kafka event

Event Reception (Order 2: Kafka Event First):
11. Kafka → CorrelationAwareKafkaConsumer.consumeEvent()
12. CorrelationAwareKafkaConsumer → EventAggregationService.handleKafkaEvent()
13. EventAggregationService → Check if API response also received
14. If not, store Kafka event, wait for API response

Completion:
15. When both received → EventAggregationService.checkAndResumeExecution()
16. EventAggregationService → WorkflowExecutor.resumeExecution()
17. WorkflowExecutor → Continue with next node
```

## Configuration

### Node Configuration Schema

```json
{
  "type": "wait_events",
  "data": {
    "apiCall": {
      "enabled": "boolean (default: true, set false to skip API call)",
      "url": "string (required if enabled, supports template variables)",
      "method": "GET|POST|PUT|DELETE (default: POST)",
      "headers": {
        "key": "value (supports template variables)"
      },
      "body": "object|string (supports template variables, execution_id and correlation_id auto-injected)",
      "correlationIdField": "string (field name in request body, default: correlation_id)",
      "correlationIdHeader": "string (header name, optional, default: X-Correlation-Id)",
      "executionIdField": "string (field name in request body, default: execution_id)",
      "executionIdHeader": "string (header name, optional, default: X-Execution-Id)",
      "timeout": "number (seconds, default: 300)",
      "required": "boolean (default: true, must receive to continue)"
    },
    "kafkaEvent": {
      "enabled": "boolean (default: true, set false to skip Kafka event)",
      "topic": "string (required if enabled)",
      "correlationIdField": "string (field name in event, default: correlation_id)",
      "executionIdField": "string (field name in event, default: execution_id)",
      "filter": {
        "field": "value (optional, for event filtering)"
      },
      "timeout": "number (seconds, default: 300)",
      "required": "boolean (default: true, must receive to continue)"
    },
    "aggregationStrategy": "all|any|required_only|custom (default: all)",
    "requiredEvents": "array (for custom strategy, e.g. ['api_response', 'kafka_event'])",
    "timeout": "number (overall timeout in seconds, default: 300)",
    "onTimeout": "fail|continue|continue_with_partial (default: fail)",
    "outputMapping": {
      "apiResponse": "path to API response data (default: api_response)",
      "kafkaEvent": "path to Kafka event data (default: kafka_event)"
    }
  }
}
```

### Configuration Examples

#### Example 1: Wait for Both Events (Default)
```json
{
  "type": "wait_events",
  "data": {
    "apiCall": {
      "enabled": true,
      "url": "https://api.example.com/process",
      "method": "POST",
      "required": true
    },
    "kafkaEvent": {
      "enabled": true,
      "topic": "process.completed",
      "required": true
    },
    "aggregationStrategy": "all",
    "timeout": 300
  }
}
```

#### Example 2: Wait for Any Event (Whichever Comes First)
```json
{
  "type": "wait_events",
  "data": {
    "apiCall": {
      "enabled": true,
      "url": "https://api.example.com/process",
      "required": false
    },
    "kafkaEvent": {
      "enabled": true,
      "topic": "process.completed",
      "required": false
    },
    "aggregationStrategy": "any",
    "timeout": 300
  }
}
```

#### Example 3: Only API Call (No Kafka Event)
```json
{
  "type": "wait_events",
  "data": {
    "apiCall": {
      "enabled": true,
      "url": "https://api.example.com/process",
      "required": true
    },
    "kafkaEvent": {
      "enabled": false
    },
    "aggregationStrategy": "all",
    "timeout": 300
  }
}
```

#### Example 4: Only Kafka Event (No API Call)
```json
{
  "type": "wait_events",
  "data": {
    "apiCall": {
      "enabled": false
    },
    "kafkaEvent": {
      "enabled": true,
      "topic": "process.completed",
      "required": true
    },
    "aggregationStrategy": "all",
    "timeout": 300
  }
}
```

#### Example 5: Custom Strategy (Wait for Specific Events)
```json
{
  "type": "wait_events",
  "data": {
    "apiCall": {
      "enabled": true,
      "url": "https://api.example.com/process",
      "required": true
    },
    "kafkaEvent": {
      "enabled": true,
      "topic": "process.completed",
      "required": false
    },
    "aggregationStrategy": "custom",
    "requiredEvents": ["api_response"],
    "timeout": 300
  }
}
```

## Error Handling

### Timeout Scenarios

Handling depends on `aggregationStrategy` and `onTimeout` configuration:

1. **Strategy: `all`** (Wait for all enabled events):
   - If any required event times out:
     - `onTimeout: fail`: Mark node execution as failed, stop workflow
     - `onTimeout: continue`: Continue with available events (if all required events received)
     - `onTimeout: continue_with_partial`: Continue with partial data, log warning

2. **Strategy: `any`** (Wait for any event):
   - If no events received within timeout:
     - `onTimeout: fail`: Mark node execution as failed, stop workflow
     - `onTimeout: continue`: Continue with empty data (if allowed)
   - If at least one event received: Continue immediately

3. **Strategy: `required_only`** (Wait only for required events):
   - If any required event times out:
     - `onTimeout: fail`: Mark node execution as failed, stop workflow
     - `onTimeout: continue`: Continue with available required events
   - Optional events timeout is ignored

4. **Strategy: `custom`** (Wait for specific events):
   - If any event in `requiredEvents` times out:
     - `onTimeout: fail`: Mark node execution as failed, stop workflow
     - `onTimeout: continue`: Continue only if all required events received

### Event Validation Errors

1. **Missing Correlation ID**:
   - API response or Kafka event missing correlation_id
   - Log warning, ignore event (don't match to any wait state)

2. **Missing Execution ID**:
   - API response or Kafka event missing execution_id
   - Log warning, ignore event (safety measure)

3. **Execution ID Mismatch**:
   - Event's execution_id doesn't match wait state's execution_id
   - Log error, ignore event (prevent cross-execution contamination)

4. **Correlation ID Mismatch**:
   - Event's correlation_id doesn't match wait state's correlation_id
   - Log warning, ignore event

### Multi-Instance Errors

1. **Optimistic Locking Conflicts**:
   - Multiple instances try to resume same execution
   - Only one succeeds, others log debug message
   - Normal behavior, not an error

2. **Instance Failure During Wait**:
   - Instance crashes while waiting for events
   - Other instances can still process events (database-based state)
   - Timeout job on any instance will handle expired states

3. **Database Connection Loss**:
   - Retry with exponential backoff
   - Alert on persistent failures
   - Events may be lost if database unavailable (consider dead letter queue)

### Error Recovery

1. **Retry Logic** (optional):
   - Retry API call if timeout
   - Re-subscribe to Kafka topic if connection lost

2. **Dead Letter Queue**:
   - Store failed wait states for manual review
   - Alert on persistent failures

3. **Monitoring**:
   - Track timeout rates
   - Monitor average wait times
   - Alert on high timeout rates

## Frontend Integration

### Workflow Builder UI

Add "Wait for Events" node to node palette:

- **Node Type**: `wait_events`
- **Category**: Logic/Integration
- **Icon**: Clock/Wait icon
- **Properties Panel**:
  - API Call Configuration
    - URL input
    - Method dropdown
    - Headers editor
    - Body editor
    - Correlation ID field name
  - Kafka Event Configuration
    - Topic input
    - Correlation ID field name
    - Filter editor
  - Timeout Settings
    - Overall timeout
    - On timeout behavior
  - Output Mapping
    - API response path
    - Kafka event path

### Node Visualization

- Show waiting state with spinner
- Display received events status
- Show timeout countdown (if applicable)

## Testing Requirements

### Unit Tests

1. **WaitForEventsNodeExecutor**:
   - Test node execution
   - Test correlation ID generation
   - Test API call execution
   - Test wait state registration

2. **EventAggregationService**:
   - Test event registration
   - Test API response handling
   - Test Kafka event handling
   - Test completion checking
   - Test timeout handling

### Integration Tests

1. **End-to-End Flow**:
   - API response arrives first
   - Kafka event arrives first
   - Both arrive simultaneously
   - Timeout scenarios

2. **Kafka Integration**:
   - Event correlation matching
   - Multiple executions with different correlation IDs
   - Event filtering

3. **API Integration**:
   - API call execution
   - Callback handling
   - Error scenarios

## Multi-Instance Deployment Considerations

### Architecture for Multi-Instance

The system is designed to work correctly when deployed across multiple instances:

1. **Database-Based State Management**:
   - All wait states stored in shared database (PostgreSQL)
   - Any instance can read/write wait states
   - No in-memory state that would be lost on instance restart

2. **Optimistic Locking**:
   - `version` field in `execution_wait_states` table
   - Prevents concurrent updates from multiple instances
   - Only one instance successfully updates when resuming execution

3. **Kafka Consumer Groups**:
   - All instances in same consumer group
   - Kafka automatically distributes partitions across instances
   - Each event processed by exactly one instance
   - No duplicate processing

4. **Idempotency**:
   - `resumed_at` and `resumed_by` fields track resume status
   - Check before resuming to prevent duplicate resumes
   - Safe to retry operations

5. **Distributed Locking** (Optional for timeout handling):
   - Use database row locks (SELECT FOR UPDATE)
   - Or use distributed lock (Redis, etc.) for timeout job coordination
   - Ensures only one instance processes each expired wait state

### Implementation Example: Optimistic Locking

```java
@Service
public class EventAggregationServiceImpl implements EventAggregationService {
    
    @Transactional
    public boolean checkAndResumeExecution(String executionId, String nodeId) {
        // Load wait state with current version
        ExecutionWaitState waitState = waitStateRepository
            .findByExecutionIdAndNodeId(executionId, nodeId)
            .orElseThrow();
        
        // Check if already resumed (idempotency)
        if ("completed".equals(waitState.getStatus()) || 
            "resuming".equals(waitState.getStatus())) {
            logger.debug("Execution already resumed: executionId={}, nodeId={}", 
                        executionId, nodeId);
            return false;
        }
        
        // Check completion condition based on strategy
        if (!isCompletionConditionMet(waitState)) {
            return false;
        }
        
        // Optimistic locking: update with version check
        int currentVersion = waitState.getVersion();
        waitState.setStatus("resuming");
        waitState.setResumedAt(LocalDateTime.now());
        waitState.setResumedBy(getInstanceId()); // e.g., hostname or instance ID
        waitState.setVersion(currentVersion + 1);
        
        try {
            // This will fail if version changed (another instance updated it)
            waitState = waitStateRepository.save(waitState);
            
            // Successfully acquired lock, proceed with resume
            resumeWorkflowExecution(executionId, nodeId, waitState);
            
            waitState.setStatus("completed");
            waitStateRepository.save(waitState);
            
            return true;
        } catch (OptimisticLockingFailureException e) {
            // Another instance already resumed
            logger.debug("Another instance already resumed: executionId={}, nodeId={}", 
                        executionId, nodeId);
            return false;
        }
    }
    
    private String getInstanceId() {
        // Return unique instance identifier
        // e.g., hostname, container ID, or configured instance ID
        return System.getenv("INSTANCE_ID") != null ? 
               System.getenv("INSTANCE_ID") : 
               InetAddress.getLocalHost().getHostName();
    }
}
```

### Timeout Job Coordination

```java
@Scheduled(fixedDelay = 60000) // Run every minute
public void processExpiredWaitStates() {
    LocalDateTime now = LocalDateTime.now();
    
    // Find expired wait states
    List<ExecutionWaitState> expiredStates = waitStateRepository
        .findByStatusAndExpiresAtBefore("waiting", now);
    
    for (ExecutionWaitState state : expiredStates) {
        // Try to acquire lock (only one instance will succeed)
        try {
            // Use database row lock
            ExecutionWaitState lockedState = waitStateRepository
                .findByIdWithLock(state.getId())
                .orElse(null);
            
            if (lockedState != null && "waiting".equals(lockedState.getStatus())) {
                // This instance acquired the lock
                handleTimeout(lockedState.getExecutionId(), lockedState.getNodeId());
            }
        } catch (Exception e) {
            logger.warn("Error processing expired wait state: {}", state.getId(), e);
        }
    }
}
```

## Performance Considerations

### Scalability

1. **Database Indexing**:
   - Index on `correlation_id` for fast lookups
   - Index on `(execution_id, correlation_id)` for event matching
   - Index on `expires_at` for timeout cleanup
   - Partial index on `status = 'waiting'` for active queries

2. **Kafka Consumer**:
   - Efficient correlation ID matching with composite index
   - Consumer group distributes load across instances
   - Avoid full table scans

3. **Cleanup Job**:
   - Scheduled job to clean up expired wait states
   - Run every 5 minutes
   - Only one instance processes each expired state (via locking)

4. **Connection Pooling**:
   - Database connection pooling for concurrent access
   - Kafka consumer connection pooling

### Monitoring

1. **Metrics**:
   - Number of active wait states (per instance and total)
   - Average wait time
   - Timeout rate
   - Event arrival order distribution
   - Resume conflicts (optimistic locking failures)
   - Instance distribution of processing

2. **Alerts**:
   - High timeout rate (> 5%)
   - Stale wait states (> 1 hour)
   - Database connection issues
   - High resume conflict rate (indicates load imbalance)
   - Instance failures

3. **Distributed Tracing**:
   - Track execution across instances
   - Correlate API calls and Kafka events
   - Monitor instance processing distribution

## Related Documentation

- [Integration Requirements](./integration-requirements.md) - General integration patterns
- [API Contract](./api-contract.md) - API specifications
- [Workflow Builder](../features/workflow-builder.md) - Workflow builder feature
- [Database Schema](../../database-schema/entities.md) - Database entities

## Implementation Checklist

### Backend Implementation

- [ ] Create `execution_wait_states` table
- [ ] Implement `EventAggregationService`
- [ ] Implement `WaitForEventsNodeExecutor`
- [ ] Enhance `KafkaEventConsumer` with correlation ID matching
- [ ] Create `ApiCallbackController` for API response callbacks
- [ ] Modify `WorkflowExecutor` to handle waiting nodes
- [ ] Implement timeout handling
- [ ] Create cleanup job for expired wait states
- [ ] Add monitoring and metrics

### Frontend Implementation

- [ ] Add "Wait for Events" node to palette
- [ ] Create node configuration UI
- [ ] Add waiting state visualization
- [ ] Display event reception status
- [ ] Show timeout countdown

### Testing

- [ ] Unit tests for all services
- [ ] Integration tests for event aggregation
- [ ] End-to-end tests for complete flow
- [ ] Performance tests for scalability

### Documentation

- [ ] Update API documentation
- [ ] Update workflow builder documentation
- [ ] Create user guide for this node type

