-- Rate limiting tracking table
CREATE TABLE rate_limit_tracking (
    id VARCHAR(255) PRIMARY KEY,
    channel_id VARCHAR(255) REFERENCES channels(id),
    window_start TIMESTAMP NOT NULL,
    window_end TIMESTAMP NOT NULL,
    request_count INTEGER DEFAULT 0,
    limit_value INTEGER NOT NULL,
    window_type VARCHAR(50) NOT NULL DEFAULT 'minute', -- minute, hour, day
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(channel_id, window_start, window_type)
);

-- Dead letter queue table
CREATE TABLE dead_letter_queue (
    id VARCHAR(255) PRIMARY KEY,
    source_type VARCHAR(50) NOT NULL, -- notification, kafka, rabbitmq, workflow
    source_id VARCHAR(255), -- notification_id, message_id, execution_id, etc.
    channel_id VARCHAR(255) REFERENCES channels(id),
    workflow_id VARCHAR(255) REFERENCES workflows(id),
    execution_id VARCHAR(255) REFERENCES executions(id),
    notification_id VARCHAR(255) REFERENCES notifications(id),
    original_message JSONB NOT NULL,
    error_message TEXT,
    error_type VARCHAR(100), -- rate_limit, network_error, provider_error, validation_error, etc.
    retry_count INTEGER DEFAULT 0,
    max_retries INTEGER DEFAULT 3,
    last_retry_at TIMESTAMP,
    next_retry_at TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'pending', -- pending, retrying, failed, resolved
    resolved_at TIMESTAMP,
    resolved_by VARCHAR(255), -- user_id or system
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_rate_limit_tracking_channel_id ON rate_limit_tracking(channel_id);
CREATE INDEX idx_rate_limit_tracking_window_start ON rate_limit_tracking(window_start);
CREATE INDEX idx_rate_limit_tracking_window_end ON rate_limit_tracking(window_end);
CREATE INDEX idx_rate_limit_tracking_channel_window ON rate_limit_tracking(channel_id, window_start, window_type);

CREATE INDEX idx_dlq_source_type ON dead_letter_queue(source_type);
CREATE INDEX idx_dlq_source_id ON dead_letter_queue(source_id);
CREATE INDEX idx_dlq_channel_id ON dead_letter_queue(channel_id);
CREATE INDEX idx_dlq_workflow_id ON dead_letter_queue(workflow_id);
CREATE INDEX idx_dlq_status ON dead_letter_queue(status);
CREATE INDEX idx_dlq_next_retry_at ON dead_letter_queue(next_retry_at);
CREATE INDEX idx_dlq_created_at ON dead_letter_queue(created_at);
CREATE INDEX idx_dlq_source_type_status ON dead_letter_queue(source_type, status);

