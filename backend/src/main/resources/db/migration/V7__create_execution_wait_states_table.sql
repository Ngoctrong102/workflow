-- Create execution_wait_states table
-- Stores execution states for nodes waiting for multiple asynchronous events (API response + Kafka event).
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

-- Indexes
CREATE INDEX idx_execution_wait_states_correlation_id ON execution_wait_states(correlation_id);
CREATE INDEX idx_execution_wait_states_execution_id ON execution_wait_states(execution_id);
CREATE INDEX idx_execution_wait_states_status ON execution_wait_states(status);
CREATE INDEX idx_execution_wait_states_expires_at ON execution_wait_states(expires_at);

