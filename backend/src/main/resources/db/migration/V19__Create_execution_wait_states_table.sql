-- Execution wait states table for async event aggregation
CREATE TABLE execution_wait_states (
    id VARCHAR(255) PRIMARY KEY,
    execution_id VARCHAR(255) NOT NULL REFERENCES executions(id) ON DELETE CASCADE,
    node_id VARCHAR(255) NOT NULL,
    correlation_id VARCHAR(255) NOT NULL,
    aggregation_strategy VARCHAR(50) NOT NULL, -- 'all', 'any', 'required_only', 'custom'
    required_events JSONB, -- Array of required event types
    enabled_events JSONB, -- Array of enabled event types
    api_call_enabled BOOLEAN NOT NULL DEFAULT false,
    kafka_event_enabled BOOLEAN NOT NULL DEFAULT false,
    api_response_data JSONB,
    kafka_event_data JSONB,
    received_events JSONB, -- Array of received event types: ['api_response', 'kafka_event']
    status VARCHAR(50) NOT NULL DEFAULT 'waiting', -- waiting, completed, timeout, failed, resuming
    resumed_at TIMESTAMP, -- Timestamp when execution was resumed (for idempotency)
    resumed_by VARCHAR(255), -- Instance ID that resumed (for debugging)
    version INTEGER NOT NULL DEFAULT 1, -- Optimistic locking version
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMP NOT NULL,
    UNIQUE(execution_id, node_id)
);

-- Indexes for performance
CREATE INDEX idx_execution_wait_states_correlation_id ON execution_wait_states(correlation_id);
CREATE INDEX idx_execution_wait_states_execution_id ON execution_wait_states(execution_id);
CREATE INDEX idx_execution_wait_states_status ON execution_wait_states(status) WHERE status = 'waiting';
CREATE INDEX idx_execution_wait_states_expires_at ON execution_wait_states(expires_at) WHERE status = 'waiting';
CREATE INDEX idx_execution_wait_states_exec_corr ON execution_wait_states(execution_id, correlation_id);

