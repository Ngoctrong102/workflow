# Database Entities

## Workflows

### Table: `workflows`

Stores workflow definitions created by users.

```sql
CREATE TABLE workflows (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    definition JSONB NOT NULL,  -- Workflow structure (nodes, edges)
    status VARCHAR(50) NOT NULL,  -- draft, active, inactive, paused, archived
    version INTEGER NOT NULL DEFAULT 1,
    tags TEXT[],
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP
);

CREATE INDEX idx_workflows_status ON workflows(status);
CREATE INDEX idx_workflows_created_at ON workflows(created_at);
CREATE INDEX idx_workflows_deleted_at ON workflows(deleted_at) WHERE deleted_at IS NULL;
```

### Fields
- `id`: Unique workflow identifier
- `name`: Workflow name
- `description`: Workflow description
- `definition`: JSONB containing workflow structure (nodes, edges, positions)
- `status`: Workflow status (draft, active, inactive, paused, archived)
- `version`: Version number for versioning
- `tags`: Array of tags for categorization
- `created_at`: Creation timestamp
- `updated_at`: Last update timestamp
- `deleted_at`: Soft delete timestamp

## Triggers

### Table: `triggers`

Stores trigger configurations for workflows. **Note**: Triggers in this table are only definitions/configurations and do not automatically run. They only become active when a trigger node is added to a workflow definition and the workflow is activated.

**Concept Hierarchy:**
1. **Trigger Registry** (hardcoded in code): Template definitions (api-call, scheduler, event)
2. **Trigger Config** (this table): User-created configuration for a workflow trigger node
3. **Trigger Node** (in `workflows.definition`): Node in workflow graph that references trigger config
4. **Runtime Execution**: When workflow is active, system creates consumers/schedulers based on trigger config

```sql
CREATE TABLE triggers (
    id VARCHAR(255) PRIMARY KEY,
    workflow_id VARCHAR(255) NOT NULL REFERENCES workflows(id) ON DELETE CASCADE,
    node_id VARCHAR(255) NOT NULL,  -- Node ID in workflow definition
    trigger_type VARCHAR(50) NOT NULL CHECK (trigger_type IN ('api-call', 'scheduler', 'event')),
    config JSONB NOT NULL,  -- Trigger configuration
    status VARCHAR(50) NOT NULL DEFAULT 'active',  -- active, inactive
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP,
    UNIQUE(workflow_id, node_id)
);

CREATE INDEX idx_triggers_workflow_id ON triggers(workflow_id);
CREATE INDEX idx_triggers_trigger_type ON triggers(trigger_type);
CREATE INDEX idx_triggers_status ON triggers(status);
CREATE INDEX idx_triggers_deleted_at ON triggers(deleted_at) WHERE deleted_at IS NULL;
CREATE INDEX idx_triggers_config ON triggers USING GIN (config);
```

### Fields
- `id`: Unique trigger identifier
- `workflow_id`: Reference to workflow
- `node_id`: Node ID in workflow definition (triggers reference this node)
- `trigger_type`: Trigger type (api-call, scheduler, event)
- `config`: JSONB containing trigger configuration
  - API Call: `{endpointPath, httpMethod, authentication, requestSchema}`
  - Scheduler: `{cronExpression, timezone, startDate, endDate, repeat, data}`
  - Event (Kafka): `{kafka: {brokers, topic, consumerGroup, offset}, schemas, kafkaConnect}`
- `status`: Trigger status (active, inactive) - metadata only, not runtime state
- `error_message`: Error message if trigger has configuration error
- `created_at`: Creation timestamp
- `updated_at`: Last update timestamp
- `deleted_at`: Soft delete timestamp

### Important Notes
- **Triggers are configuration only**: They do not automatically run. Runtime state (ACTIVE, PAUSED, etc.) is managed separately (in memory or separate table if needed).
- **Trigger nodes in workflow**: When a trigger node is added to `workflows.definition`, it references a trigger config via `node_id`.
- **Runtime activation**: When workflow is activated, system reads trigger node from workflow definition and creates consumer/scheduler based on trigger config.

## Actions

### Table: `actions`

Stores action definitions in the registry. Actions must be defined here before they can be used in workflows.

```sql
CREATE TABLE actions (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,  -- api-call, publish-event, function, custom-action
    action_type VARCHAR(50),  -- For custom actions (send-email, send-sms, etc.)
    description TEXT,
    config_template JSONB NOT NULL,  -- Configuration template
    metadata JSONB,  -- Icon, color, version, etc.
    version VARCHAR(50) NOT NULL DEFAULT '1.0.0',
    enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP
);

CREATE INDEX idx_actions_type ON actions(type);
CREATE INDEX idx_actions_action_type ON actions(action_type);
CREATE INDEX idx_actions_enabled ON actions(enabled);
CREATE INDEX idx_actions_deleted_at ON actions(deleted_at) WHERE deleted_at IS NULL;
```

### Fields
- `id`: Unique action identifier
- `name`: Action name (e.g., "API Call Action", "Send Email")
- `type`: Action type (api-call, publish-event, function, custom-action)
- `action_type`: For custom actions (send-email, send-sms, send-webhook, wait-events, etc.)
- `description`: Action description
- `config_template`: JSONB containing configuration template
- `metadata`: JSONB containing icon, color, version, etc.
- `version`: Version number
- `enabled`: Whether action is enabled
- `created_at`: Creation timestamp
- `updated_at`: Last update timestamp
- `deleted_at`: Soft delete timestamp

## Executions

### Table: `executions`

Stores workflow execution records with comprehensive data for troubleshooting, reporting, and analytics.

```sql
CREATE TABLE executions (
    id VARCHAR(255) PRIMARY KEY,
    workflow_id VARCHAR(255) NOT NULL REFERENCES workflows(id),
    trigger_id VARCHAR(255) REFERENCES triggers(id),
    trigger_node_id VARCHAR(255),  -- Node ID of trigger node
    status VARCHAR(50) NOT NULL,  -- RUNNING, WAITING, PAUSED, COMPLETED, FAILED, CANCELLED
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    duration INTEGER,  -- Duration in milliseconds
    nodes_executed INTEGER DEFAULT 0,
    notifications_sent INTEGER DEFAULT 0,
    
    -- Comprehensive execution data (JSONB)
    context JSONB NOT NULL,  -- Full execution context (stored when paused/completed)
    trigger_data JSONB,  -- Data from trigger
    workflow_metadata JSONB,  -- Workflow metadata at execution time
    execution_metadata JSONB,  -- Execution-specific metadata
    
    error TEXT,
    error_details JSONB,  -- Detailed error information
    
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Indexes for JSONB queries
CREATE INDEX idx_executions_workflow_id ON executions(workflow_id);
CREATE INDEX idx_executions_trigger_id ON executions(trigger_id);
CREATE INDEX idx_executions_status ON executions(status);
CREATE INDEX idx_executions_started_at ON executions(started_at);
CREATE INDEX idx_executions_created_at ON executions(created_at);
CREATE INDEX idx_executions_context ON executions USING GIN (context);
CREATE INDEX idx_executions_trigger_data ON executions USING GIN (trigger_data);
CREATE INDEX idx_executions_workflow_metadata ON executions USING GIN (workflow_metadata);
```

### Fields
- `id`: Unique execution identifier
- `workflow_id`: Reference to workflow
- `trigger_id`: Reference to trigger configuration that started execution
- `trigger_node_id`: Node ID of trigger node in workflow definition
- `status`: Execution status (RUNNING, WAITING, PAUSED, COMPLETED, FAILED, CANCELLED)
- `started_at`: Execution start timestamp
- `completed_at`: Execution completion timestamp
- `duration`: Execution duration in milliseconds
- `nodes_executed`: Number of nodes executed
- `notifications_sent`: Number of notifications sent
- `context`: JSONB containing full execution context (stored when paused/completed, cached in Redis during active execution)
- `trigger_data`: JSONB containing data from trigger
- `workflow_metadata`: JSONB containing workflow metadata at execution time
- `execution_metadata`: JSONB containing execution-specific metadata
- `error`: Error message if execution failed
- `error_details`: JSONB containing detailed error information
- `created_at`: Record creation timestamp
- `updated_at`: Last update timestamp

### Context Storage Strategy
- **Active Execution**: Context stored in Redis cache for performance
- **Paused Execution**: Context persisted to database for resume capability
- **Completed Execution**: Context persisted to database for analytics
- **Failed Execution**: Context persisted to database for troubleshooting

See [Distributed Execution Management](../features/distributed-execution-management.md) for details.

### Data Retention
- Keep records for 6 months
- Archive or delete older records

## Node Executions

### Table: `node_executions`

Stores individual node execution records with comprehensive data for troubleshooting, reporting, and analytics.

```sql
CREATE TABLE node_executions (
    id VARCHAR(255) PRIMARY KEY,
    execution_id VARCHAR(255) NOT NULL REFERENCES executions(id) ON DELETE CASCADE,
    node_id VARCHAR(255) NOT NULL,
    node_label VARCHAR(255),  -- Human-readable node label
    node_type VARCHAR(50) NOT NULL,  -- trigger, logic, action
    node_sub_type VARCHAR(50),  -- api-call, condition, delay, etc.
    registry_id VARCHAR(255),  -- Registry ID for trigger/action nodes
    
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
    retry_details JSONB,  -- Retry attempts information (Resilience4j)
    
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Indexes for JSONB queries
CREATE INDEX idx_node_executions_execution_id ON node_executions(execution_id);
CREATE INDEX idx_node_executions_node_id ON node_executions(node_id);
CREATE INDEX idx_node_executions_node_type ON node_executions(node_type);
CREATE INDEX idx_node_executions_status ON node_executions(status);
CREATE INDEX idx_node_executions_started_at ON node_executions(started_at);
CREATE INDEX idx_node_executions_input_data ON node_executions USING GIN (input_data);
CREATE INDEX idx_node_executions_output_data ON node_executions USING GIN (output_data);
CREATE INDEX idx_node_executions_node_config ON node_executions USING GIN (node_config);
```

### Fields
- `id`: Unique node execution identifier
- `execution_id`: Reference to execution
- `node_id`: Node identifier from workflow definition
- `node_label`: Human-readable node label
- `node_type`: Node type (trigger, logic, action)
- `node_sub_type`: Node sub-type (api-call, condition, delay, etc.)
- `registry_id`: Registry ID for action nodes (reference to actions table, or hardcoded trigger types)
- `status`: Node execution status (RUNNING, WAITING, PAUSED, COMPLETED, FAILED)
- `started_at`: Node execution start timestamp
- `completed_at`: Node execution completion timestamp
- `duration`: Node execution duration in milliseconds
- `input_data`: JSONB containing all input data to the node (from previous nodes, context, config)
- `output_data`: JSONB containing all output data from the node
- `node_config`: JSONB containing node configuration at execution time
- `execution_context`: JSONB containing execution context available to node
- `error`: Error message if node execution failed
- `error_details`: JSONB containing detailed error information
- `retry_count`: Number of retry attempts
- `retry_details`: JSONB containing retry attempts information (Resilience4j)
- `created_at`: Record creation timestamp
- `updated_at`: Last update timestamp

## Retry Schedules

### Table: `retry_schedules`

Stores retry tasks for failed node executions and executions. Supports long-term retries (days, weeks) and is designed for multi-instance deployment.

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

### Fields
- `id`: Unique retry schedule identifier
- `retry_type`: Type of retry (`node_execution` or `execution`)
- `target_id`: ID of target to retry (node_execution_id or execution_id)
- `execution_id`: Reference to execution
- `node_id`: Node ID (for node_execution retry)
- `retry_strategy`: Retry strategy (immediate, exponential_backoff, fixed_delay, custom)
- `max_attempts`: Maximum number of retry attempts
- `current_attempt`: Current attempt number
- `initial_delay_seconds`: Delay before first retry
- `delay_seconds`: Fixed delay between retries (for fixed_delay strategy)
- `multiplier`: Multiplier for exponential backoff
- `max_delay_seconds`: Maximum delay cap (for exponential backoff)
- `custom_schedule`: Custom schedule configuration (JSONB) - supports long-term retries (days, weeks)
- `scheduled_at`: When to retry next (can be far in the future)
- `last_retried_at`: Last retry attempt timestamp
- `expires_at`: When to stop retrying (optional)
- `status`: Retry status (pending, scheduled, retrying, completed, failed, cancelled)
- `locked_by`: Instance ID that locked this retry (for multi-instance safety)
- `locked_at`: When locked
- `retry_context`: Context data for retry (input data, node config, etc.)
- `error_history`: History of errors from retry attempts
- `created_at`: Record creation timestamp
- `updated_at`: Last update timestamp
- `version`: Optimistic locking version

### Retry Strategies

1. **immediate**: Retry immediately when failed
2. **exponential_backoff**: Retry with exponential backoff (delay = initial_delay * multiplier^attempt)
3. **fixed_delay**: Retry with fixed delay between attempts
4. **custom**: Custom schedule (supports retry after days/weeks, e.g., 14 days)

See [Retry Mechanism](../features/retry-mechanism.md) for detailed implementation.

## Execution Wait States

### Table: `execution_wait_states`

Stores execution states for nodes waiting for multiple asynchronous events (API response + Kafka event).

```sql
CREATE TABLE execution_wait_states (
    id VARCHAR(255) PRIMARY KEY,
    execution_id VARCHAR(255) NOT NULL REFERENCES executions(id) ON DELETE CASCADE,
    node_id VARCHAR(255) NOT NULL,
    correlation_id VARCHAR(255) NOT NULL,
    wait_type VARCHAR(50) NOT NULL,  -- 'api_response', 'kafka_event', 'both'
    api_response_data JSONB,
    kafka_event_data JSONB,
    received_events JSONB,  -- Array of received event types
    status VARCHAR(50) NOT NULL DEFAULT 'waiting',  -- waiting, completed, timeout, failed
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMP NOT NULL,
    UNIQUE(execution_id, node_id)
);

CREATE INDEX idx_execution_wait_states_correlation_id ON execution_wait_states(correlation_id);
CREATE INDEX idx_execution_wait_states_execution_id ON execution_wait_states(execution_id);
CREATE INDEX idx_execution_wait_states_status ON execution_wait_states(status);
CREATE INDEX idx_execution_wait_states_expires_at ON execution_wait_states(expires_at);
```

### Fields
- `id`: Unique wait state identifier
- `execution_id`: Reference to execution
- `node_id`: Node identifier waiting for events
- `correlation_id`: Correlation ID to match API response and Kafka event
- `wait_type`: Type of events being waited for
- `api_response_data`: JSONB containing API response data (when received)
- `kafka_event_data`: JSONB containing Kafka event data (when received)
- `received_events`: JSONB array of received event types
- `status`: Wait state status (waiting, completed, timeout, failed)
- `created_at`: Record creation timestamp
- `updated_at`: Last update timestamp
- `expires_at`: Expiration timestamp for timeout handling

### Related Documentation
- See [Async Event Aggregation](../../technical/integration/async-event-aggregation.md) for usage details

## Analytics Aggregates

### Table: `analytics_daily`

Stores daily aggregated analytics data.

```sql
CREATE TABLE analytics_daily (
    id VARCHAR(255) PRIMARY KEY,
    date DATE NOT NULL,
    workflow_id VARCHAR(255) REFERENCES workflows(id),
    channel VARCHAR(50),
    metric_type VARCHAR(50) NOT NULL,  -- executions, deliveries, etc.
    metric_value BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(date, workflow_id, channel, metric_type)
);

CREATE INDEX idx_analytics_daily_date ON analytics_daily(date);
CREATE INDEX idx_analytics_daily_workflow_id ON analytics_daily(workflow_id);
CREATE INDEX idx_analytics_daily_channel ON analytics_daily(channel);
```

### Fields
- `id`: Unique record identifier
- `date`: Date for aggregation
- `workflow_id`: Reference to workflow (nullable for global metrics)
- `metric_type`: Type of metric (executions, node_executions, errors, etc.)
- `metric_value`: Aggregated value
- `created_at`: Record creation timestamp

### Data Retention
- Keep aggregated data longer than raw data
- Can be used for historical reporting

## Workflow Reports

### Table: `workflow_reports`

Stores automated report configurations for workflows.

```sql
CREATE TABLE workflow_reports (
    id VARCHAR(255) PRIMARY KEY,
    workflow_id VARCHAR(255) NOT NULL REFERENCES workflows(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    analyst_query TEXT NOT NULL,  -- SQL query for analyzing execution data
    period_type VARCHAR(50) NOT NULL,  -- last_24h, last_7d, last_30d, last_90d, custom
    period_start_date TIMESTAMP,  -- For custom period
    period_end_date TIMESTAMP,  -- For custom period
    schedule_cron VARCHAR(255) NOT NULL,  -- Cron expression for scheduling
    recipients TEXT[] NOT NULL,  -- Array of email addresses
    format VARCHAR(50) NOT NULL DEFAULT 'csv',  -- csv, excel, json
    timezone VARCHAR(100) NOT NULL DEFAULT 'UTC',  -- Timezone for schedule and date calculations
    status VARCHAR(50) NOT NULL DEFAULT 'active',  -- active, inactive, paused
    last_generated_at TIMESTAMP,
    next_generation_at TIMESTAMP,
    last_generation_status VARCHAR(50),  -- success, failed
    last_generation_error TEXT,
    generation_count INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP,
    UNIQUE(workflow_id)  -- One report config per workflow
);

CREATE INDEX idx_workflow_reports_workflow_id ON workflow_reports(workflow_id);
CREATE INDEX idx_workflow_reports_status ON workflow_reports(status);
CREATE INDEX idx_workflow_reports_next_generation_at ON workflow_reports(next_generation_at);
CREATE INDEX idx_workflow_reports_deleted_at ON workflow_reports(deleted_at) WHERE deleted_at IS NULL;
```

### Fields
- `id`: Unique report configuration identifier
- `workflow_id`: Reference to workflow (one-to-one relationship)
- `name`: Report name
- `analyst_query`: SQL query for analyzing execution data (TEXT, NOT NULL)
  - Query can use parameters: `:workflow_id`, `:start_date`, `:end_date`
  - Query must be read-only (SELECT only)
- `period_type`: Fixed period type (last_24h, last_7d, last_30d, last_90d, custom)
- `period_start_date`: Custom period start date (TIMESTAMP, nullable, required if period_type = 'custom')
- `period_end_date`: Custom period end date (TIMESTAMP, nullable, required if period_type = 'custom')
- `schedule_cron`: Cron expression for scheduling (VARCHAR, NOT NULL)
  - Example: "0 9 * * *" (daily at 9:00 AM)
  - Example: "0 9 * * 1" (every Monday at 9:00 AM)
- `recipients`: Array of email addresses for business team members
- `format`: Report file format (csv, excel, json, default: 'csv')
- `timezone`: Timezone for schedule and date calculations (e.g., "Asia/Ho_Chi_Minh", "UTC")
- `status`: Report status (active, inactive, paused)
- `last_generated_at`: Timestamp of last report generation
- `next_generation_at`: Next scheduled generation timestamp
- `last_generation_status`: Status of last generation (success, failed)
- `last_generation_error`: Error message if last generation failed
- `generation_count`: Total number of reports generated
- `created_at`: Creation timestamp
- `updated_at`: Last update timestamp
- `deleted_at`: Soft delete timestamp

### Report History Table: `workflow_report_history`

Stores history of generated reports.

```sql
CREATE TABLE workflow_report_history (
    id VARCHAR(255) PRIMARY KEY,
    workflow_report_id VARCHAR(255) NOT NULL REFERENCES workflow_reports(id) ON DELETE CASCADE,
    workflow_id VARCHAR(255) NOT NULL REFERENCES workflows(id) ON DELETE CASCADE,
    report_period_start TIMESTAMP NOT NULL,
    report_period_end TIMESTAMP NOT NULL,
    file_path VARCHAR(500),  -- Storage path for report file
    file_size BIGINT,  -- File size in bytes
    format VARCHAR(50) NOT NULL,  -- pdf, excel, csv
    recipients TEXT[] NOT NULL,  -- Recipients who received the report
    delivery_status VARCHAR(50) NOT NULL,  -- sent, failed, partial
    generated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_workflow_report_history_workflow_id ON workflow_report_history(workflow_id);
CREATE INDEX idx_workflow_report_history_workflow_report_id ON workflow_report_history(workflow_report_id);
CREATE INDEX idx_workflow_report_history_generated_at ON workflow_report_history(generated_at);
```

### Fields
- `id`: Unique report history identifier
- `workflow_report_id`: Reference to report configuration
- `workflow_id`: Reference to workflow
- `report_period_start`: Start of report period
- `report_period_end`: End of report period
- `file_path`: Storage path for generated report file
- `file_size`: File size in bytes
- `format`: Report format (pdf, excel, csv)
- `recipients`: List of recipients who received the report
- `delivery_status`: Delivery status (sent, failed, partial)
- `generated_at`: Report generation timestamp
- `created_at`: Record creation timestamp

### Data Retention
- Report history retained for 3 months (configurable)
- Old reports can be archived or deleted


