-- Provider health monitoring table
CREATE TABLE provider_health (
    id VARCHAR(255) PRIMARY KEY,
    channel_id VARCHAR(255) NOT NULL,
    channel_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL, -- healthy, degraded, down
    response_time_ms INTEGER,
    error_rate DECIMAL(5,2) DEFAULT 0.0,
    success_count INTEGER DEFAULT 0,
    failure_count INTEGER DEFAULT 0,
    last_check_at TIMESTAMP,
    last_success_at TIMESTAMP,
    last_failure_at TIMESTAMP,
    consecutive_failures INTEGER DEFAULT 0,
    health_score DECIMAL(5,2) DEFAULT 100.0, -- 0-100 health score
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Alert rules table
CREATE TABLE alert_rules (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    rule_type VARCHAR(50) NOT NULL, -- failure_threshold, rate_limit, response_time, error_rate
    channel_type VARCHAR(50), -- null for all channels
    threshold_value DECIMAL(10,2) NOT NULL,
    threshold_unit VARCHAR(50), -- count, percentage, milliseconds
    condition VARCHAR(50) NOT NULL, -- greater_than, less_than, equals
    severity VARCHAR(50) NOT NULL DEFAULT 'warning', -- info, warning, critical
    enabled BOOLEAN DEFAULT TRUE,
    notification_channels TEXT[], -- Array of notification channels (email, in-app, webhook)
    notification_recipients TEXT[], -- Array of email addresses or user IDs
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP
);

-- Alerts table
CREATE TABLE alerts (
    id VARCHAR(255) PRIMARY KEY,
    alert_rule_id VARCHAR(255) NOT NULL,
    channel_id VARCHAR(255),
    channel_type VARCHAR(50),
    alert_type VARCHAR(50) NOT NULL, -- provider_down, high_error_rate, slow_response, rate_limited
    severity VARCHAR(50) NOT NULL, -- info, warning, critical
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'open', -- open, acknowledged, resolved, closed
    triggered_at TIMESTAMP NOT NULL DEFAULT NOW(),
    acknowledged_at TIMESTAMP,
    acknowledged_by VARCHAR(255),
    resolved_at TIMESTAMP,
    resolved_by VARCHAR(255),
    metadata JSONB, -- Additional alert data
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Alert history table (for tracking alert events)
CREATE TABLE alert_history (
    id VARCHAR(255) PRIMARY KEY,
    alert_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(50) NOT NULL, -- triggered, acknowledged, resolved, closed
    event_data JSONB,
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_provider_health_channel_id ON provider_health(channel_id);
CREATE INDEX idx_provider_health_channel_type ON provider_health(channel_type);
CREATE INDEX idx_provider_health_status ON provider_health(status);
CREATE INDEX idx_provider_health_last_check_at ON provider_health(last_check_at);

CREATE INDEX idx_alert_rules_rule_type ON alert_rules(rule_type);
CREATE INDEX idx_alert_rules_channel_type ON alert_rules(channel_type);
CREATE INDEX idx_alert_rules_enabled ON alert_rules(enabled);
CREATE INDEX idx_alert_rules_deleted_at ON alert_rules(deleted_at) WHERE deleted_at IS NULL;

CREATE INDEX idx_alerts_alert_rule_id ON alerts(alert_rule_id);
CREATE INDEX idx_alerts_channel_id ON alerts(channel_id);
CREATE INDEX idx_alerts_status ON alerts(status);
CREATE INDEX idx_alerts_severity ON alerts(severity);
CREATE INDEX idx_alerts_triggered_at ON alerts(triggered_at);

CREATE INDEX idx_alert_history_alert_id ON alert_history(alert_id);
CREATE INDEX idx_alert_history_event_type ON alert_history(event_type);
CREATE INDEX idx_alert_history_created_at ON alert_history(created_at);

