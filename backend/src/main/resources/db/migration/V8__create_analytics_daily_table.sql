-- Create analytics_daily table
-- Stores daily aggregated analytics data.
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

-- Indexes
CREATE INDEX idx_analytics_daily_date ON analytics_daily(date);
CREATE INDEX idx_analytics_daily_workflow_id ON analytics_daily(workflow_id);
CREATE INDEX idx_analytics_daily_channel ON analytics_daily(channel);

