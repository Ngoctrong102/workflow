-- Create retry_schedules table
-- Stores retry tasks for failed node executions and executions. Supports long-term retries (days, weeks) and is designed for multi-instance deployment.
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

