-- Scheduled reports table
CREATE TABLE scheduled_reports (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    report_type VARCHAR(50) NOT NULL, -- workflow, delivery, channel, custom
    frequency VARCHAR(50) NOT NULL, -- daily, weekly, monthly
    cron_expression VARCHAR(100),
    format VARCHAR(50) NOT NULL DEFAULT 'csv', -- csv, pdf, excel, json
    recipients TEXT[] NOT NULL, -- Array of email addresses
    filters JSONB, -- Report filters (workflowId, channelId, dateRange, etc.)
    status VARCHAR(50) NOT NULL DEFAULT 'active', -- active, paused, inactive
    last_run_at TIMESTAMP,
    next_run_at TIMESTAMP,
    last_run_status VARCHAR(50), -- success, failed
    last_run_error TEXT,
    run_count INTEGER DEFAULT 0,
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP
);

-- Indexes
CREATE INDEX idx_scheduled_reports_status ON scheduled_reports(status);
CREATE INDEX idx_scheduled_reports_next_run_at ON scheduled_reports(next_run_at);
CREATE INDEX idx_scheduled_reports_deleted_at ON scheduled_reports(deleted_at);
CREATE INDEX idx_scheduled_reports_report_type ON scheduled_reports(report_type);

