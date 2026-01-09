# Retry Mechanism

## Overview

Retry mechanism được thiết kế để hỗ trợ retry trong môi trường phân tán (multi-instance), với khả năng retry sau thời gian dài (có thể đến 14 ngày hoặc hơn). Không sử dụng Resilience4j vì nó không đảm bảo tính phân tán và không thể retry sau thời gian dài.

## Design Principles

1. **Database-Based**: Tất cả retry state được lưu trong database, đảm bảo multi-instance safe
2. **Long-Term Support**: Hỗ trợ retry sau thời gian dài (days, weeks)
3. **Flexible Strategy**: Nhiều retry strategies (immediate, exponential backoff, fixed delay, custom schedule)
4. **Idempotent**: Retry operations là idempotent, an toàn khi nhiều instances cùng xử lý
5. **Observable**: Track retry attempts, failures, và success rates

## Database Schema

### Table: `retry_schedules`

Lưu trữ các retry tasks cần được thực thi trong tương lai.

```sql
CREATE TABLE retry_schedules (
    id VARCHAR(255) PRIMARY KEY,
    
    -- Retry target information
    retry_type VARCHAR(50) NOT NULL,  -- 'node_execution', 'execution'
    target_id VARCHAR(255) NOT NULL,  -- node_execution_id or execution_id
    execution_id VARCHAR(255) NOT NULL REFERENCES executions(id) ON DELETE CASCADE,
    node_id VARCHAR(255),  -- Node ID (for node_execution retry)
    
    -- Retry configuration
    retry_strategy VARCHAR(50) NOT NULL,  -- 'immediate', 'exponential_backoff', 'fixed_delay', 'custom'
    max_attempts INTEGER NOT NULL DEFAULT 3,
    current_attempt INTEGER NOT NULL DEFAULT 0,
    
    -- Timing configuration
    initial_delay_seconds INTEGER DEFAULT 0,  -- Delay before first retry
    delay_seconds INTEGER,  -- Fixed delay (for fixed_delay strategy)
    multiplier DECIMAL(5,2) DEFAULT 2.0,  -- For exponential backoff
    max_delay_seconds INTEGER,  -- Max delay cap (for exponential backoff)
    custom_schedule JSONB,  -- Custom schedule configuration
    
    -- Retry timing
    scheduled_at TIMESTAMP NOT NULL,  -- When to retry next
    last_retried_at TIMESTAMP,  -- Last retry attempt timestamp
    expires_at TIMESTAMP,  -- When to stop retrying (optional)
    
    -- Retry state
    status VARCHAR(50) NOT NULL DEFAULT 'pending',  -- pending, scheduled, retrying, completed, failed, cancelled
    locked_by VARCHAR(255),  -- Instance ID that locked this retry (for multi-instance safety)
    locked_at TIMESTAMP,  -- When locked
    
    -- Retry context
    retry_context JSONB,  -- Context data for retry (input data, config, etc.)
    error_history JSONB,  -- History of errors from retry attempts
    
    -- Metadata
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    version INTEGER NOT NULL DEFAULT 1  -- Optimistic locking
);

-- Indexes
CREATE INDEX idx_retry_schedules_status ON retry_schedules(status) WHERE status IN ('pending', 'scheduled');
CREATE INDEX idx_retry_schedules_scheduled_at ON retry_schedules(scheduled_at) WHERE status = 'scheduled';
CREATE INDEX idx_retry_schedules_execution_id ON retry_schedules(execution_id);
CREATE INDEX idx_retry_schedules_target_id ON retry_schedules(target_id);
CREATE INDEX idx_retry_schedules_expires_at ON retry_schedules(expires_at) WHERE expires_at IS NOT NULL;
CREATE INDEX idx_retry_schedules_locked_by ON retry_schedules(locked_by) WHERE locked_by IS NOT NULL;
```

### Fields Description

- **retry_type**: Loại retry (`node_execution` hoặc `execution`)
- **target_id**: ID của target cần retry (node_execution_id hoặc execution_id)
- **retry_strategy**: Strategy cho retry timing
  - `immediate`: Retry ngay lập tức
  - `exponential_backoff`: Exponential backoff với multiplier
  - `fixed_delay`: Fixed delay giữa các attempts
  - `custom`: Custom schedule (có thể retry sau 14 ngày, etc.)
- **scheduled_at**: Thời điểm retry tiếp theo (có thể là tương lai xa)
- **locked_by**: Instance ID đang xử lý retry này (để tránh duplicate processing)
- **retry_context**: Context data cần thiết để retry (input data, node config, etc.)
- **error_history**: Lịch sử errors từ các retry attempts

## Retry Strategies

### 1. Immediate Retry

Retry ngay lập tức khi node/execution failed.

```json
{
  "retry_strategy": "immediate",
  "max_attempts": 3,
  "initial_delay_seconds": 0
}
```

**Use Case**: Transient errors, network hiccups

### 2. Exponential Backoff

Retry với exponential backoff: `delay = initial_delay * multiplier^attempt`

```json
{
  "retry_strategy": "exponential_backoff",
  "max_attempts": 5,
  "initial_delay_seconds": 60,
  "multiplier": 2.0,
  "max_delay_seconds": 3600
}
```

**Example**:
- Attempt 1: 60 seconds
- Attempt 2: 120 seconds
- Attempt 3: 240 seconds
- Attempt 4: 480 seconds
- Attempt 5: 960 seconds (capped at 3600)

**Use Case**: Rate limiting, temporary service unavailability

### 3. Fixed Delay

Retry với fixed delay giữa các attempts.

```json
{
  "retry_strategy": "fixed_delay",
  "max_attempts": 3,
  "delay_seconds": 300
}
```

**Use Case**: Scheduled maintenance, known downtime windows

### 4. Custom Schedule

Retry theo custom schedule (có thể retry sau 14 ngày).

```json
{
  "retry_strategy": "custom",
  "max_attempts": 2,
  "custom_schedule": {
    "attempts": [
      {
        "attempt": 1,
        "delay_days": 7
      },
      {
        "attempt": 2,
        "delay_days": 14
      }
    ]
  }
}
```

**Use Case**: Long-term retry, waiting for external system recovery

## Retry Flow

### Node Execution Retry Flow

```
1. Node execution fails
   ↓
2. Check if retry is enabled in node config
   ↓
3. Create retry_schedule record
   - retry_type = 'node_execution'
   - target_id = node_execution_id
   - scheduled_at = calculate based on strategy
   - retry_context = save input data, node config
   ↓
4. Scheduled job picks up retry_schedule
   ↓
5. Lock retry_schedule (optimistic locking)
   ↓
6. Execute node again with saved context
   ↓
7. If success: Mark retry_schedule as completed
   If failure: Update scheduled_at for next retry
   If max_attempts reached: Mark as failed
```

### Execution Retry Flow

```
1. Execution fails
   ↓
2. Check if retry is enabled in execution config
   ↓
3. Create retry_schedule record
   - retry_type = 'execution'
   - target_id = execution_id
   - scheduled_at = calculate based on strategy
   - retry_context = save trigger data, workflow context
   ↓
4. Scheduled job picks up retry_schedule
   ↓
5. Lock retry_schedule (optimistic locking)
   ↓
6. Create new execution with saved context
   ↓
7. If success: Mark retry_schedule as completed
   If failure: Update scheduled_at for next retry
   If max_attempts reached: Mark as failed
```

## Implementation

### Retry Service

```java
@Service
public class RetryService {
    
    /**
     * Schedule a retry for failed node execution
     */
    public RetrySchedule scheduleNodeRetry(
        String nodeExecutionId,
        String executionId,
        String nodeId,
        NodeRetryConfig retryConfig,
        Map<String, Object> retryContext
    ) {
        // Calculate next retry time based on strategy
        LocalDateTime scheduledAt = calculateNextRetryTime(
            retryConfig.getStrategy(),
            retryConfig.getCurrentAttempt(),
            retryConfig
        );
        
        RetrySchedule retrySchedule = new RetrySchedule();
        retrySchedule.setId(UUID.randomUUID().toString());
        retrySchedule.setRetryType(RetryType.NODE_EXECUTION);
        retrySchedule.setTargetId(nodeExecutionId);
        retrySchedule.setExecutionId(executionId);
        retrySchedule.setNodeId(nodeId);
        retrySchedule.setRetryStrategy(retryConfig.getStrategy());
        retrySchedule.setMaxAttempts(retryConfig.getMaxAttempts());
        retrySchedule.setCurrentAttempt(0);
        retrySchedule.setScheduledAt(scheduledAt);
        retrySchedule.setRetryContext(retryContext);
        retrySchedule.setStatus(RetryStatus.SCHEDULED);
        
        return retryScheduleRepository.save(retrySchedule);
    }
    
    /**
     * Calculate next retry time based on strategy
     */
    private LocalDateTime calculateNextRetryTime(
        RetryStrategy strategy,
        int currentAttempt,
        NodeRetryConfig config
    ) {
        LocalDateTime now = LocalDateTime.now();
        
        switch (strategy) {
            case IMMEDIATE:
                return now;
                
            case EXPONENTIAL_BACKOFF:
                long delaySeconds = (long) (
                    config.getInitialDelaySeconds() * 
                    Math.pow(config.getMultiplier(), currentAttempt)
                );
                if (config.getMaxDelaySeconds() != null) {
                    delaySeconds = Math.min(delaySeconds, config.getMaxDelaySeconds());
                }
                return now.plusSeconds(delaySeconds);
                
            case FIXED_DELAY:
                return now.plusSeconds(config.getDelaySeconds());
                
            case CUSTOM:
                // Parse custom schedule
                Map<String, Object> customSchedule = config.getCustomSchedule();
                List<Map<String, Object>> attempts = 
                    (List<Map<String, Object>>) customSchedule.get("attempts");
                
                if (currentAttempt < attempts.size()) {
                    Map<String, Object> attemptConfig = attempts.get(currentAttempt);
                    Integer delayDays = (Integer) attemptConfig.get("delay_days");
                    return now.plusDays(delayDays);
                }
                // Fallback to last attempt delay
                Map<String, Object> lastAttempt = attempts.get(attempts.size() - 1);
                Integer delayDays = (Integer) lastAttempt.get("delay_days");
                return now.plusDays(delayDays);
                
            default:
                return now;
        }
    }
}
```

### Retry Scheduler Job

```java
@Component
@Slf4j
public class RetrySchedulerJob {
    
    private final RetryScheduleRepository retryScheduleRepository;
    private final RetryService retryService;
    private final WorkflowExecutor workflowExecutor;
    private final NodeExecutionRepository nodeExecutionRepository;
    
    /**
     * Process scheduled retries
     * Runs every minute to check for retries that are due
     */
    @Scheduled(fixedRate = 60000) // Every minute
    @Transactional
    public void processScheduledRetries() {
        LocalDateTime now = LocalDateTime.now();
        
        // Find retries that are due (scheduled_at <= now)
        List<RetrySchedule> dueRetries = retryScheduleRepository
            .findByStatusAndScheduledAtLessThanEqual(
                RetryStatus.SCHEDULED,
                now
            );
        
        log.info("Found {} retries due for processing", dueRetries.size());
        
        for (RetrySchedule retrySchedule : dueRetries) {
            try {
                processRetry(retrySchedule);
            } catch (Exception e) {
                log.error("Error processing retry: retryScheduleId={}", 
                          retrySchedule.getId(), e);
            }
        }
    }
    
    /**
     * Process a single retry
     * Uses optimistic locking to ensure only one instance processes each retry
     */
    private void processRetry(RetrySchedule retrySchedule) {
        String instanceId = getInstanceId();
        
        // Try to acquire lock (optimistic locking)
        int currentVersion = retrySchedule.getVersion();
        retrySchedule.setStatus(RetryStatus.RETRYING);
        retrySchedule.setLockedBy(instanceId);
        retrySchedule.setLockedAt(LocalDateTime.now());
        retrySchedule.setVersion(currentVersion + 1);
        
        try {
            retrySchedule = retryScheduleRepository.save(retrySchedule);
        } catch (OptimisticLockingFailureException e) {
            // Another instance already processing this retry
            log.debug("Retry already being processed by another instance: retryScheduleId={}", 
                     retrySchedule.getId());
            return;
        }
        
        try {
            // Execute retry based on type
            if (retrySchedule.getRetryType() == RetryType.NODE_EXECUTION) {
                retryNodeExecution(retrySchedule);
            } else if (retrySchedule.getRetryType() == RetryType.EXECUTION) {
                retryExecution(retrySchedule);
            }
        } catch (Exception e) {
            log.error("Error executing retry: retryScheduleId={}", 
                     retrySchedule.getId(), e);
            handleRetryFailure(retrySchedule, e);
        }
    }
    
    /**
     * Retry a node execution
     */
    private void retryNodeExecution(RetrySchedule retrySchedule) {
        // Load node execution
        NodeExecution nodeExecution = nodeExecutionRepository
            .findById(retrySchedule.getTargetId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Node execution not found: " + retrySchedule.getTargetId()));
        
        // Load execution context
        Execution execution = nodeExecution.getExecution();
        Map<String, Object> retryContext = retrySchedule.getRetryContext();
        
        // Extract saved data
        Map<String, Object> inputData = (Map<String, Object>) retryContext.get("inputData");
        Map<String, Object> nodeConfig = (Map<String, Object>) retryContext.get("nodeConfig");
        ExecutionContext executionContext = recoverExecutionContext(execution);
        
        // Execute node again
        NodeExecutionResult result = workflowExecutor.executeNode(
            retrySchedule.getNodeId(),
            nodeConfig,
            executionContext
        );
        
        if (result.isSuccess()) {
            // Retry successful
            retrySchedule.setStatus(RetryStatus.COMPLETED);
            retrySchedule.setLockedBy(null);
            retrySchedule.setLockedAt(null);
            retryScheduleRepository.save(retrySchedule);
        } else {
            // Retry failed, schedule next attempt
            scheduleNextRetry(retrySchedule, result.getError());
        }
    }
    
    /**
     * Schedule next retry attempt
     */
    private void scheduleNextRetry(RetrySchedule retrySchedule, String error) {
        int nextAttempt = retrySchedule.getCurrentAttempt() + 1;
        
        if (nextAttempt >= retrySchedule.getMaxAttempts()) {
            // Max attempts reached
            retrySchedule.setStatus(RetryStatus.FAILED);
            retrySchedule.setLockedBy(null);
            retrySchedule.setLockedAt(null);
            retryScheduleRepository.save(retrySchedule);
            return;
        }
        
        // Add error to history
        List<Map<String, Object>> errorHistory = retrySchedule.getErrorHistory() != null ?
            new ArrayList<>(retrySchedule.getErrorHistory()) : new ArrayList<>();
        Map<String, Object> errorEntry = new HashMap<>();
        errorEntry.put("attempt", nextAttempt);
        errorEntry.put("timestamp", LocalDateTime.now());
        errorEntry.put("error", error);
        errorHistory.add(errorEntry);
        retrySchedule.setErrorHistory(errorHistory);
        
        // Calculate next retry time
        LocalDateTime nextRetryTime = calculateNextRetryTime(
            retrySchedule.getRetryStrategy(),
            nextAttempt,
            retrySchedule
        );
        
        retrySchedule.setCurrentAttempt(nextAttempt);
        retrySchedule.setScheduledAt(nextRetryTime);
        retrySchedule.setStatus(RetryStatus.SCHEDULED);
        retrySchedule.setLastRetriedAt(LocalDateTime.now());
        retrySchedule.setLockedBy(null);
        retrySchedule.setLockedAt(null);
        retryScheduleRepository.save(retrySchedule);
    }
    
    private String getInstanceId() {
        // Return unique instance identifier
        return System.getenv("INSTANCE_ID") != null ? 
               System.getenv("INSTANCE_ID") : 
               InetAddress.getLocalHost().getHostName();
    }
}
```

## Node Configuration

### Retry Configuration in Node

```json
{
  "nodeId": "api-call-1",
  "type": "action",
  "subType": "api-call",
  "config": {
    "url": "https://api.example.com/users",
    "method": "POST",
    "retry": {
      "enabled": true,
      "strategy": "exponential_backoff",
      "maxAttempts": 5,
      "initialDelaySeconds": 60,
      "multiplier": 2.0,
      "maxDelaySeconds": 3600,
      "retryOn": ["5xx", "408", "429"],
      "ignoreExceptions": ["4xx"]
    }
  }
}
```

### Custom Schedule Example (14 days retry)

```json
{
  "retry": {
    "enabled": true,
    "strategy": "custom",
    "maxAttempts": 2,
    "customSchedule": {
      "attempts": [
        {
          "attempt": 1,
          "delayDays": 7
        },
        {
          "attempt": 2,
          "delayDays": 14
        }
      ]
    }
  }
}
```

## Multi-Instance Safety

### Optimistic Locking

Sử dụng `version` field để đảm bảo chỉ một instance xử lý mỗi retry:

```java
// Try to acquire lock
int currentVersion = retrySchedule.getVersion();
retrySchedule.setStatus(RetryStatus.RETRYING);
retrySchedule.setVersion(currentVersion + 1);

try {
    retrySchedule = retryScheduleRepository.save(retrySchedule);
} catch (OptimisticLockingFailureException e) {
    // Another instance already processing
    return;
}
```

### Lock Expiration

Nếu instance crash khi đang retry, lock sẽ expire sau một thời gian:

```java
@Scheduled(fixedRate = 300000) // Every 5 minutes
public void releaseExpiredLocks() {
    LocalDateTime expireTime = LocalDateTime.now().minusMinutes(10);
    
    List<RetrySchedule> expiredLocks = retryScheduleRepository
        .findByStatusAndLockedAtLessThan(RetryStatus.RETRYING, expireTime);
    
    for (RetrySchedule retrySchedule : expiredLocks) {
        retrySchedule.setStatus(RetryStatus.SCHEDULED);
        retrySchedule.setLockedBy(null);
        retrySchedule.setLockedAt(null);
        retryScheduleRepository.save(retrySchedule);
    }
}
```

## Monitoring and Observability

### Retry Metrics

- Number of pending retries
- Number of scheduled retries
- Retry success rate
- Average retry delay
- Retries by strategy
- Failed retries (max attempts reached)

### Retry Queries

```sql
-- Find all pending retries
SELECT * FROM retry_schedules 
WHERE status = 'scheduled' 
ORDER BY scheduled_at;

-- Find retries scheduled for next 24 hours
SELECT * FROM retry_schedules 
WHERE status = 'scheduled' 
  AND scheduled_at <= NOW() + INTERVAL '24 hours'
ORDER BY scheduled_at;

-- Find failed retries (max attempts reached)
SELECT * FROM retry_schedules 
WHERE status = 'failed'
ORDER BY updated_at DESC;

-- Retry success rate
SELECT 
  COUNT(*) FILTER (WHERE status = 'completed') as successful,
  COUNT(*) FILTER (WHERE status = 'failed') as failed,
  COUNT(*) as total,
  ROUND(100.0 * COUNT(*) FILTER (WHERE status = 'completed') / COUNT(*), 2) as success_rate
FROM retry_schedules;
```

## Migration from Resilience4j

### Changes Required

1. **Remove Resilience4j dependency** (if exists)
2. **Add `retry_schedules` table** (migration script)
3. **Update NodeExecution entity** (remove retry_count, retry_details if not needed)
4. **Implement RetryService** (schedule retries)
5. **Implement RetrySchedulerJob** (process retries)
6. **Update NodeExecutors** (create retry schedules on failure)
7. **Update ExecutionService** (create retry schedules for failed executions)

### Backward Compatibility

- Existing retry configurations in node configs vẫn hoạt động
- Chỉ thay đổi implementation (từ Resilience4j → Database-based)
- API endpoints không thay đổi

## Related Documentation

- [Workflow Execution State](./workflow-execution-state.md) - Execution state management
- [Database Schema](../database-schema/entities.md) - Database entities
- [Node Types](./node-types.md) - Node configuration

