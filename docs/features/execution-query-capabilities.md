# Execution Query Capabilities

## Overview

PostgreSQL JSONB functions enable powerful querying capabilities for execution data. This document describes how to query individual fields within JSONB structures for troubleshooting, reporting, and analytics.

## PostgreSQL JSONB Functions

### Basic JSONB Operators

#### Access JSONB Fields

```sql
-- Access top-level field
SELECT context->'workflowId' FROM executions;

-- Access nested field
SELECT context->'nodeOutputs'->'trigger-node-abc'->'userId' FROM executions;

-- Get text value (removes quotes)
SELECT context->>'workflowId' FROM executions;

-- Get JSONB value (keeps JSON structure)
SELECT context->'nodeOutputs' FROM executions;
```

#### Path Access

```sql
-- Access using path operator
SELECT context#>'{nodeOutputs,trigger-node-abc,userId}' FROM executions;

-- Get text value using path
SELECT context#>>'{nodeOutputs,trigger-node-abc,userId}' FROM executions;
```

### Query Examples

#### 1. Query Executions by Trigger Data

```sql
-- Find executions triggered by specific user
SELECT * FROM executions
WHERE trigger_data->>'userId' = 'user-123';

-- Find executions with specific event type
SELECT * FROM executions
WHERE trigger_data->'request'->'body'->>'eventType' = 'user.created';

-- Find executions with API key in headers
SELECT * FROM executions
WHERE trigger_data->'request'->'headers'->>'X-API-Key' IS NOT NULL;
```

#### 2. Query Executions by Context Data

```sql
-- Find executions for specific workflow
SELECT * FROM executions
WHERE context->>'workflowId' = 'workflow-456';

-- Find executions with specific variable value
SELECT * FROM executions
WHERE context->'variables'->>'globalVar1' = 'value1';

-- Find executions with specific node output
SELECT * FROM executions
WHERE context->'nodeOutputs'->'send-email'->>'status' = 'sent';
```

#### 3. Query Node Executions by Input/Output

```sql
-- Find node executions with specific input
SELECT * FROM node_executions
WHERE input_data->'resolvedInput'->>'url' LIKE '%api.example.com%';

-- Find node executions with specific output
SELECT * FROM node_executions
WHERE output_data->'result'->>'email' = 'user@example.com';

-- Find node executions with error in output
SELECT * FROM node_executions
WHERE output_data->'errors' IS NOT NULL;
```

#### 4. Query by Node Configuration

```sql
-- Find API call nodes with specific URL
SELECT * FROM node_executions
WHERE node_config->'config'->>'url' = 'https://api.example.com/users';

-- Find nodes with retry enabled
SELECT * FROM node_executions
WHERE node_config->'config'->'retry'->>'enabled' = 'true';

-- Find nodes of specific type
SELECT * FROM node_executions
WHERE node_type = 'action' AND node_sub_type = 'api-call';
```

#### 5. Complex Queries

```sql
-- Find failed executions with error details
SELECT 
    e.id,
    e.workflow_id,
    e.started_at,
    e.error_details->>'errorType' as error_type,
    e.error_details->>'nodeId' as failed_node_id,
    e.error_details->'context'->>'url' as failed_url
FROM executions e
WHERE e.status = 'FAILED'
  AND e.error_details IS NOT NULL;

-- Find executions with specific node output value
SELECT 
    e.id,
    e.workflow_id,
    e.context->'nodeOutputs'->'send-email'->>'messageId' as message_id
FROM executions e
WHERE e.context->'nodeOutputs'->'send-email'->>'messageId' IS NOT NULL;

-- Find node executions with retry attempts
SELECT 
    ne.id,
    ne.node_id,
    ne.execution_id,
    ne.retry_details->>'retryCount' as retry_count,
    ne.retry_details->'attempts' as retry_attempts
FROM node_executions ne
WHERE ne.retry_details IS NOT NULL
  AND (ne.retry_details->>'retryCount')::int > 0;
```

#### 6. Aggregation Queries

```sql
-- Count executions by workflow
SELECT 
    context->>'workflowId' as workflow_id,
    context->>'workflowName' as workflow_name,
    COUNT(*) as execution_count
FROM executions
GROUP BY context->>'workflowId', context->>'workflowName';

-- Average duration by node type
SELECT 
    node_type,
    node_sub_type,
    AVG(duration) as avg_duration,
    COUNT(*) as execution_count
FROM node_executions
WHERE status = 'COMPLETED'
GROUP BY node_type, node_sub_type;

-- Error rate by node
SELECT 
    node_id,
    node_label,
    COUNT(*) FILTER (WHERE status = 'FAILED') as failed_count,
    COUNT(*) as total_count,
    ROUND(100.0 * COUNT(*) FILTER (WHERE status = 'FAILED') / COUNT(*), 2) as error_rate
FROM node_executions
GROUP BY node_id, node_label;
```

#### 7. Time-Based Queries

```sql
-- Find executions in date range with specific trigger data
SELECT * FROM executions
WHERE started_at BETWEEN '2024-01-01' AND '2024-01-31'
  AND trigger_data->'request'->'body'->>'userId' = 'user-123';

-- Find slow node executions
SELECT 
    ne.id,
    ne.node_id,
    ne.node_label,
    ne.duration,
    ne.input_data->'resolvedInput'->>'url' as url
FROM node_executions ne
WHERE ne.duration > 5000
  AND ne.status = 'COMPLETED'
ORDER BY ne.duration DESC;
```

#### 8. Nested JSONB Queries

```sql
-- Query nested arrays
SELECT 
    ne.id,
    ne.node_id,
    jsonb_array_elements(ne.retry_details->'attempts') as retry_attempt
FROM node_executions ne
WHERE ne.retry_details->'attempts' IS NOT NULL;

-- Query nested objects
SELECT 
    e.id,
    jsonb_each(e.context->'nodeOutputs') as node_output
FROM executions e
WHERE e.context->'nodeOutputs' IS NOT NULL;
```

## Indexing Strategies

### GIN Indexes for JSONB

```sql
-- Full JSONB index (supports all operators)
CREATE INDEX idx_executions_context_gin 
ON executions USING GIN (context);

CREATE INDEX idx_node_executions_input_data_gin 
ON node_executions USING GIN (input_data);

CREATE INDEX idx_node_executions_output_data_gin 
ON node_executions USING GIN (output_data);
```

### Expression Indexes for Specific Fields

```sql
-- Index on specific JSONB path
CREATE INDEX idx_executions_workflow_id 
ON executions ((context->>'workflowId'));

CREATE INDEX idx_executions_trigger_user_id 
ON executions ((trigger_data->>'userId'));

CREATE INDEX idx_node_executions_output_email 
ON node_executions ((output_data->'result'->>'email'));

-- Index on nested path
CREATE INDEX idx_node_executions_config_url 
ON node_executions ((node_config->'config'->>'url'));
```

### Composite Indexes

```sql
-- Composite index for common query patterns
CREATE INDEX idx_executions_workflow_status 
ON executions ((context->>'workflowId'), status, started_at);

CREATE INDEX idx_node_executions_type_status 
ON node_executions (node_type, node_sub_type, status, started_at);
```

## Query Performance Tips

### 1. Use Specific Paths

```sql
-- Good: Specific path
SELECT * FROM executions
WHERE context->>'workflowId' = 'workflow-456';

-- Bad: Full JSONB scan
SELECT * FROM executions
WHERE context @> '{"workflowId": "workflow-456"}';
```

### 2. Use Expression Indexes

```sql
-- Create index for frequently queried fields
CREATE INDEX idx_executions_user_id 
ON executions ((trigger_data->>'userId'));

-- Then query uses index
SELECT * FROM executions
WHERE trigger_data->>'userId' = 'user-123';
```

### 3. Use JSONB Containment

```sql
-- Use @> operator for complex queries
SELECT * FROM executions
WHERE context @> '{"nodeOutputs": {"send-email": {"status": "sent"}}}';
```

### 4. Combine with Regular Indexes

```sql
-- Combine JSONB queries with regular indexes
SELECT * FROM executions
WHERE status = 'COMPLETED'
  AND started_at > '2024-01-01'
  AND context->>'workflowId' = 'workflow-456';
```

## Reporting Queries

### Execution Summary Report

```sql
SELECT 
    e.id,
    e.workflow_id,
    e.context->>'workflowName' as workflow_name,
    e.status,
    e.started_at,
    e.completed_at,
    e.duration,
    e.nodes_executed,
    e.notifications_sent,
    e.trigger_data->>'triggerType' as trigger_type,
    e.error
FROM executions e
WHERE e.started_at BETWEEN :start_date AND :end_date
ORDER BY e.started_at DESC;
```

### Node Execution Report

```sql
SELECT 
    ne.id,
    ne.execution_id,
    ne.node_id,
    ne.node_label,
    ne.node_type,
    ne.node_sub_type,
    ne.status,
    ne.started_at,
    ne.completed_at,
    ne.duration,
    ne.input_data,
    ne.output_data,
    ne.error
FROM node_executions ne
WHERE ne.execution_id = :execution_id
ORDER BY ne.started_at;
```

### Error Analysis Report

```sql
SELECT 
    e.id,
    e.workflow_id,
    e.context->>'workflowName' as workflow_name,
    e.error_details->>'errorType' as error_type,
    e.error_details->>'nodeId' as failed_node_id,
    e.error_details->'context'->>'url' as failed_url,
    e.error_details->'context'->>'responseStatus' as response_status,
    e.started_at
FROM executions e
WHERE e.status = 'FAILED'
  AND e.started_at BETWEEN :start_date AND :end_date
ORDER BY e.started_at DESC;
```

### Performance Analysis Report

```sql
SELECT 
    ne.node_id,
    ne.node_label,
    ne.node_type,
    ne.node_sub_type,
    COUNT(*) as total_executions,
    AVG(ne.duration) as avg_duration,
    MIN(ne.duration) as min_duration,
    MAX(ne.duration) as max_duration,
    PERCENTILE_CONT(0.95) WITHIN GROUP (ORDER BY ne.duration) as p95_duration,
    COUNT(*) FILTER (WHERE ne.status = 'FAILED') as failed_count
FROM node_executions ne
WHERE ne.started_at BETWEEN :start_date AND :end_date
GROUP BY ne.node_id, ne.node_label, ne.node_type, ne.node_sub_type
ORDER BY avg_duration DESC;
```

## Troubleshooting Queries

### Find Execution by Correlation ID

```sql
SELECT * FROM executions
WHERE execution_metadata->>'correlationId' = :correlation_id;
```

### Find All Node Executions for Failed Execution

```sql
SELECT 
    ne.*,
    ne.input_data,
    ne.output_data,
    ne.error_details
FROM node_executions ne
WHERE ne.execution_id = :execution_id
ORDER BY ne.started_at;
```

### Find Executions with Specific Node Output

```sql
SELECT 
    e.*,
    e.context->'nodeOutputs'->:node_id as node_output
FROM executions e
WHERE e.context->'nodeOutputs'->:node_id IS NOT NULL
  AND e.context->'nodeOutputs'->:node_id->>:field_name = :field_value;
```

## Related Documentation

- [Execution Data Structure](./execution-data-structure.md) - Execution data structure details
- [Analytics](./analytics.md) - Analytics and reporting features
- [Workflow Report](./workflow-report.md) - Report generation

