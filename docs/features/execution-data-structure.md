# Execution Data Structure

## Overview

Execution data structure is designed to store **comprehensive information** about workflow and node executions to support troubleshooting, reporting, and analytics. All execution data is stored in PostgreSQL JSONB columns to enable powerful querying capabilities using PostgreSQL JSON functions.

## Design Principles

1. **Comprehensive Data**: Store all execution information including inputs, outputs, errors, metadata
2. **JSONB Storage**: Use PostgreSQL JSONB for flexible querying
3. **Queryable Fields**: Enable querying individual fields within JSON structures
4. **Troubleshooting Support**: Store enough information to debug issues
5. **Analytics Ready**: Structure data for reporting and analytics
6. **Efficient Storage**: Context only persisted to database when necessary (completed or paused)
7. **Cache Strategy**: Use Redis cache for active executions to minimize database reads

**See**: [Distributed Execution Management](./distributed-execution-management.md) for context storage strategy.

## Execution Entity Structure

### Database Schema

```sql
CREATE TABLE executions (
    id VARCHAR(255) PRIMARY KEY,
    workflow_id VARCHAR(255) NOT NULL REFERENCES workflows(id),
    trigger_id VARCHAR(255) REFERENCES triggers(id),
    trigger_node_id VARCHAR(255),  -- Node ID of trigger node
    status VARCHAR(50) NOT NULL,  -- RUNNING, WAITING, PAUSED, COMPLETED, FAILED
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    duration INTEGER,  -- Duration in milliseconds
    nodes_executed INTEGER DEFAULT 0,
    notifications_sent INTEGER DEFAULT 0,
    
    -- Comprehensive execution data (JSONB)
    context JSONB NOT NULL,  -- Full execution context
    trigger_data JSONB,  -- Data from trigger
    workflow_metadata JSONB,  -- Workflow metadata at execution time
    execution_metadata JSONB,  -- Execution-specific metadata
    
    error TEXT,
    error_details JSONB,  -- Detailed error information
    
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Indexes for JSONB queries
CREATE INDEX idx_executions_context ON executions USING GIN (context);
CREATE INDEX idx_executions_trigger_data ON executions USING GIN (trigger_data);
CREATE INDEX idx_executions_workflow_metadata ON executions USING GIN (workflow_metadata);
CREATE INDEX idx_executions_status ON executions(status);
CREATE INDEX idx_executions_started_at ON executions(started_at);
```

### Context Structure (JSONB)

The `context` field stores comprehensive execution context:

```json
{
  "executionId": "exec-123",
  "workflowId": "workflow-456",
  "workflowName": "User Onboarding Workflow",
  "workflowVersion": 1,
  "triggerId": "trigger-789",
  "triggerNodeId": "trigger-node-abc",
  "triggerType": "api-call",
  "triggeredAt": "2024-01-01T10:00:00Z",
  "triggeredBy": "system",
  
  "nodeOutputs": {
    "trigger-node-abc": {
      "userId": "user-123",
      "email": "user@example.com",
      "eventType": "user.created",
      "timestamp": "2024-01-01T10:00:00Z"
    },
    "fetch-user-data": {
      "user": {
        "id": "user-123",
        "name": "John Doe",
        "email": "user@example.com",
        "status": "active"
      }
    },
    "send-email": {
      "messageId": "msg-456",
      "status": "sent",
      "sentAt": "2024-01-01T10:00:05Z"
    }
  },
  
  "variables": {
    "globalVar1": "value1",
    "globalVar2": "value2"
  },
  
  "executionPath": [
    "trigger-node-abc",
    "fetch-user-data",
    "send-email"
  ],
  
  "metadata": {
    "instanceId": "instance-1",
    "environment": "production",
    "correlationId": "corr-789"
  }
}
```

### Trigger Data Structure (JSONB)

The `trigger_data` field stores data received from the trigger:

```json
{
  "triggerType": "api-call",
  "triggerNodeId": "trigger-node-abc",
  "receivedAt": "2024-01-01T10:00:00Z",
  "request": {
    "method": "POST",
    "path": "/api/v1/trigger/workflow-456",
    "headers": {
      "Content-Type": "application/json",
      "X-API-Key": "***"
    },
    "body": {
      "userId": "user-123",
      "email": "user@example.com",
      "action": "signup"
    }
  },
  "response": {
    "statusCode": 200,
    "body": {
      "executionId": "exec-123",
      "status": "triggered"
    }
  }
}
```

### Workflow Metadata Structure (JSONB)

The `workflow_metadata` field stores workflow information at execution time:

```json
{
  "workflowId": "workflow-456",
  "workflowName": "User Onboarding Workflow",
  "workflowVersion": 1,
  "workflowStatus": "active",
  "definition": {
    "nodes": [...],
    "edges": [...]
  },
  "snapshotAt": "2024-01-01T10:00:00Z"
}
```

### Execution Metadata Structure (JSONB)

The `execution_metadata` field stores execution-specific metadata:

```json
{
  "instanceId": "instance-1",
  "environment": "production",
  "correlationId": "corr-789",
  "parentExecutionId": null,
  "retryAttempt": 0,
  "tags": ["onboarding", "user-signup"],
  "customFields": {
    "campaignId": "campaign-123",
    "source": "web"
  }
}
```

### Error Details Structure (JSONB)

The `error_details` field stores detailed error information:

```json
{
  "errorType": "NodeExecutionError",
  "errorCode": "API_CALL_FAILED",
  "message": "API call failed with status 500",
  "nodeId": "api-call-node-123",
  "nodeType": "action",
  "nodeSubType": "api-call",
  "stackTrace": "...",
  "context": {
    "url": "https://api.example.com/users",
    "method": "POST",
    "requestBody": {...},
    "responseStatus": 500,
    "responseBody": {...}
  },
  "timestamp": "2024-01-01T10:00:05Z"
}
```

## Node Execution Entity Structure

### Database Schema

```sql
CREATE TABLE node_executions (
    id VARCHAR(255) PRIMARY KEY,
    execution_id VARCHAR(255) NOT NULL REFERENCES executions(id) ON DELETE CASCADE,
    node_id VARCHAR(255) NOT NULL,
    node_label VARCHAR(255),  -- Human-readable node label
    node_type VARCHAR(50) NOT NULL,  -- trigger, logic, action
    node_sub_type VARCHAR(50),  -- api-call, condition, delay, etc.
    registry_id VARCHAR(255),  -- Registry ID for trigger/action
    
    status VARCHAR(50) NOT NULL,  -- RUNNING, WAITING, PAUSED, COMPLETED, FAILED
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    duration INTEGER,  -- Duration in milliseconds
    
    -- Comprehensive node execution data (JSONB)
    input_data JSONB NOT NULL,  -- Input data to the node
    output_data JSONB,  -- Output data from the node
    node_config JSONB,  -- Node configuration at execution time
    execution_context JSONB,  -- Execution context available to node
    
    error TEXT,
    error_details JSONB,  -- Detailed error information
    
    retry_count INTEGER DEFAULT 0,
    retry_details JSONB,  -- Retry attempts information
    
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Indexes for JSONB queries
CREATE INDEX idx_node_executions_input_data ON node_executions USING GIN (input_data);
CREATE INDEX idx_node_executions_output_data ON node_executions USING GIN (output_data);
CREATE INDEX idx_node_executions_node_config ON node_executions USING GIN (node_config);
CREATE INDEX idx_node_executions_execution_id ON node_executions(execution_id);
CREATE INDEX idx_node_executions_node_id ON node_executions(node_id);
CREATE INDEX idx_node_executions_node_type ON node_executions(node_type);
CREATE INDEX idx_node_executions_status ON node_executions(status);
CREATE INDEX idx_node_executions_started_at ON node_executions(started_at);
```

### Input Data Structure (JSONB)

The `input_data` field stores all input data to the node:

```json
{
  "fromPreviousNode": {
    "nodeId": "previous-node-123",
    "nodeLabel": "fetchUserData",
    "data": {
      "user": {
        "id": "user-123",
        "email": "user@example.com"
      }
    }
  },
  "fromContext": {
    "triggerData": {
      "userId": "user-123",
      "eventType": "user.created"
    },
    "variables": {
      "globalVar1": "value1"
    },
    "nodeOutputs": {
      "trigger-node-abc": {...},
      "previous-node-123": {...}
    }
  },
  "fromConfig": {
    "url": "https://api.example.com/users/${userId}",
    "method": "POST"
  },
  "resolvedInput": {
    "url": "https://api.example.com/users/user-123",
    "method": "POST",
    "body": {
      "userId": "user-123"
    }
  }
}
```

### Output Data Structure (JSONB)

The `output_data` field stores all output data from the node:

```json
{
  "result": {
    "id": "user-123",
    "name": "John Doe",
    "email": "user@example.com",
    "status": "active"
  },
  "metadata": {
    "executionTime": 150,
    "statusCode": 200,
    "messageId": "msg-456"
  },
  "errors": null,
  "warnings": []
}
```

### Node Config Structure (JSONB)

The `node_config` field stores node configuration at execution time:

```json
{
  "nodeId": "api-call-node-123",
  "nodeLabel": "fetchUserData",
  "nodeType": "action",
  "nodeSubType": "api-call",
  "registryId": "api-call-action-standard",
  "config": {
    "url": "https://api.example.com/users",
    "method": "POST",
    "headers": {
      "Authorization": "Bearer ***"
    },
    "timeout": 5000,
    "retry": {
      "enabled": true,
      "maxAttempts": 3
    }
  },
  "schema": {
    "inputSchema": {...},
    "outputSchema": {...}
  }
}
```

### Execution Context Structure (JSONB)

The `execution_context` field stores execution context available to the node:

```json
{
  "executionId": "exec-123",
  "workflowId": "workflow-456",
  "workflowName": "User Onboarding Workflow",
  "triggerId": "trigger-789",
  "triggerNodeId": "trigger-node-abc",
  "triggerType": "api-call",
  "allNodeOutputs": {
    "trigger-node-abc": {...},
    "previous-node-123": {...}
  },
  "variables": {
    "globalVar1": "value1"
  },
  "metadata": {
    "instanceId": "instance-1",
    "correlationId": "corr-789"
  }
}
```

### Retry Details Structure (JSONB)

The `retry_details` field stores retry attempts information:

```json
{
  "retryCount": 2,
  "maxAttempts": 3,
  "attempts": [
    {
      "attempt": 1,
      "timestamp": "2024-01-01T10:00:05Z",
      "error": "Connection timeout",
      "duration": 5000
    },
    {
      "attempt": 2,
      "timestamp": "2024-01-01T10:00:10Z",
      "error": "Connection timeout",
      "duration": 5000
    },
    {
      "attempt": 3,
      "timestamp": "2024-01-01T10:00:15Z",
      "success": true,
      "duration": 150
    }
  ],
  "totalRetryDuration": 10150
}
```

## Context Storage Strategy

### When to Persist Context

1. **During Execution**: Context is kept in Redis cache only (not persisted to DB)
2. **On Pause**: Context is persisted to database for recovery
3. **On Completion**: Context is persisted to database for analytics/reporting
4. **On Error**: Context is persisted to database for troubleshooting

### Storage Flow

```java
@Service
public class ExecutionContextManager {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final ExecutionRepository executionRepository;
    
    public void updateContext(String executionId, ExecutionContext context) {
        // Always update in cache during execution
        cacheContext(executionId, context);
        
        // Don't persist to DB yet (only on pause/completion)
    }
    
    public void persistContext(String executionId, ExecutionContext context) {
        // Persist to database
        Execution execution = executionRepository.findById(executionId)
            .orElseThrow(() -> new ResourceNotFoundException("Execution not found"));
        
        execution.setContext(serializeContext(context));
        executionRepository.save(execution);
        
        // Keep in cache for quick resume if paused
        if (execution.getStatus() == ExecutionStatus.PAUSED) {
            cacheContext(executionId, context);
        }
    }
}
```

**See**: [Distributed Execution Management](./distributed-execution-management.md) for detailed implementation.

## Data Storage Best Practices

### 1. Store Complete Data

Always store complete input/output data, not just references:

```java
// Good: Store complete data
nodeExecution.setInputData(completeInputData);
nodeExecution.setOutputData(completeOutputData);

// Bad: Store only references
nodeExecution.setInputData(Map.of("reference", "node-123"));
```

### 2. Include Metadata

Always include metadata for context:

```java
Map<String, Object> outputData = new HashMap<>();
outputData.put("result", actualResult);
outputData.put("metadata", Map.of(
    "executionTime", duration,
    "statusCode", statusCode,
    "timestamp", LocalDateTime.now()
));
```

### 3. Structure for Querying

Structure data to enable efficient querying:

```java
// Good: Flat structure for common queries
{
  "userId": "user-123",
  "email": "user@example.com",
  "status": "active"
}

// Also good: Nested structure when needed
{
  "user": {
    "id": "user-123",
    "email": "user@example.com",
    "status": "active"
  }
}
```

### 4. Index Frequently Queried Fields

Create indexes on frequently queried JSONB fields:

```sql
-- Index on specific JSONB path
CREATE INDEX idx_node_executions_user_id 
ON node_executions ((output_data->>'userId'));

-- Index on nested JSONB path
CREATE INDEX idx_node_executions_user_email 
ON node_executions ((output_data->'user'->>'email'));
```

## Related Documentation

- [Distributed Execution Management](./distributed-execution-management.md) - Distributed pause/resume with Redis cache
- [Execution Query Capabilities](./execution-query-capabilities.md) - Querying execution data using PostgreSQL JSON functions
- [Workflow Execution State](./workflow-execution-state.md) - Execution state management
- [Analytics](./analytics.md) - Analytics and reporting
- [Workflow Report](./workflow-report.md) - Report generation

