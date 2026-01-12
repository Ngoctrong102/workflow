# Database Indexes

## Index Strategy

Indexes are created for:
- Foreign keys (for join performance)
- Frequently queried columns
- Time-based queries (for analytics and retention)
- Status filters (for active/inactive filtering)

## Primary Indexes

### Workflows
```sql
CREATE INDEX idx_workflows_status ON workflows(status);
CREATE INDEX idx_workflows_created_at ON workflows(created_at);
CREATE INDEX idx_workflows_deleted_at ON workflows(deleted_at) WHERE deleted_at IS NULL;
```

### Triggers (Trigger Configs)
```sql
CREATE INDEX idx_triggers_trigger_type ON triggers(trigger_type);
CREATE INDEX idx_triggers_status ON triggers(status);
CREATE INDEX idx_triggers_deleted_at ON triggers(deleted_at) WHERE deleted_at IS NULL;
CREATE INDEX idx_triggers_config ON triggers USING GIN (config);
```

### Actions (Action Registry)
```sql
CREATE INDEX idx_actions_type ON actions(type);
CREATE INDEX idx_actions_action_type ON actions(action_type);
CREATE INDEX idx_actions_enabled ON actions(enabled);
CREATE INDEX idx_actions_deleted_at ON actions(deleted_at) WHERE deleted_at IS NULL;
```

### Executions
```sql
CREATE INDEX idx_executions_workflow_id ON executions(workflow_id);
CREATE INDEX idx_executions_trigger_id ON executions(trigger_id);
CREATE INDEX idx_executions_status ON executions(status);
CREATE INDEX idx_executions_started_at ON executions(started_at);
CREATE INDEX idx_executions_created_at ON executions(created_at);

-- JSONB GIN indexes for querying context and metadata
CREATE INDEX idx_executions_context ON executions USING GIN (context);
CREATE INDEX idx_executions_trigger_data ON executions USING GIN (trigger_data);
CREATE INDEX idx_executions_workflow_metadata ON executions USING GIN (workflow_metadata);
```

### Node Executions
```sql
CREATE INDEX idx_node_executions_execution_id ON node_executions(execution_id);
CREATE INDEX idx_node_executions_node_id ON node_executions(node_id);
CREATE INDEX idx_node_executions_node_type ON node_executions(node_type);
CREATE INDEX idx_node_executions_status ON node_executions(status);
CREATE INDEX idx_node_executions_started_at ON node_executions(started_at);

-- JSONB GIN indexes for querying input/output data
CREATE INDEX idx_node_executions_input_data ON node_executions USING GIN (input_data);
CREATE INDEX idx_node_executions_output_data ON node_executions USING GIN (output_data);
CREATE INDEX idx_node_executions_node_config ON node_executions USING GIN (node_config);
```

### Analytics Daily
```sql
CREATE INDEX idx_analytics_daily_date ON analytics_daily(date);
CREATE INDEX idx_analytics_daily_workflow_id ON analytics_daily(workflow_id);
CREATE INDEX idx_analytics_daily_metric_type ON analytics_daily(metric_type);
```

## Composite Indexes

### For Common Query Patterns

#### Workflow Executions by Date Range
```sql
CREATE INDEX idx_executions_workflow_date ON executions(workflow_id, started_at);
```

#### Analytics by Date and Workflow
```sql
CREATE INDEX idx_analytics_daily_date_workflow ON analytics_daily(date, workflow_id);
```

## Partial Indexes

### Active Records Only
Partial indexes for soft-deleted records:
```sql
CREATE INDEX idx_workflows_active ON workflows(id) WHERE deleted_at IS NULL;
CREATE INDEX idx_triggers_active ON triggers(id) WHERE deleted_at IS NULL;
CREATE INDEX idx_actions_active ON actions(id) WHERE deleted_at IS NULL;
```

## Performance Considerations

### Index Maintenance
- Monitor index usage
- Remove unused indexes
- Rebuild indexes periodically
- Consider partitioning for large tables

### Query Optimization
- Use EXPLAIN ANALYZE for query plans
- Monitor slow queries
- Optimize based on actual usage patterns

### Data Retention Impact
- Indexes on time-based columns help with retention queries
- Consider partial indexes for recent data only
- Archive old data to reduce index size


