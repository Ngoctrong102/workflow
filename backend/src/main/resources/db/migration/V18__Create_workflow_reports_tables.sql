-- Workflow reports table
CREATE TABLE workflow_reports (
    id VARCHAR(255) PRIMARY KEY,
    workflow_id VARCHAR(255) NOT NULL UNIQUE REFERENCES workflows(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    recipients TEXT[] NOT NULL, -- Array of email addresses
    schedule_type VARCHAR(50) NOT NULL, -- daily, weekly, monthly, custom
    schedule_time TIME, -- Time of day for daily/weekly/monthly (e.g., 09:00:00)
    schedule_day INTEGER, -- Day of week (1-7, Monday=1) for weekly, or day of month (1-31) for monthly
    schedule_cron VARCHAR(255), -- Cron expression for custom schedules
    timezone VARCHAR(100) DEFAULT 'UTC', -- Timezone for schedule (e.g., "Asia/Ho_Chi_Minh", "UTC")
    format VARCHAR(50) NOT NULL DEFAULT 'pdf', -- pdf, excel, csv
    sections JSONB, -- Array of section IDs to include (e.g., ["execution_summary", "notification_summary", "error_analysis"])
    status VARCHAR(50) NOT NULL DEFAULT 'inactive', -- active, inactive, paused
    last_generated_at TIMESTAMP,
    next_generation_at TIMESTAMP,
    last_generation_status VARCHAR(50), -- success, failed
    last_generation_error TEXT,
    generation_count INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP
);

-- Workflow report history table
CREATE TABLE workflow_report_history (
    id VARCHAR(255) PRIMARY KEY,
    workflow_report_id VARCHAR(255) NOT NULL REFERENCES workflow_reports(id) ON DELETE CASCADE,
    workflow_id VARCHAR(255) NOT NULL REFERENCES workflows(id) ON DELETE CASCADE,
    report_period_start TIMESTAMP NOT NULL,
    report_period_end TIMESTAMP NOT NULL,
    file_path VARCHAR(500), -- Storage path for report file
    file_size BIGINT, -- File size in bytes
    format VARCHAR(50) NOT NULL, -- pdf, excel, csv
    recipients TEXT[] NOT NULL, -- Recipients who received the report
    delivery_status VARCHAR(50) NOT NULL, -- sent, failed, partial
    generated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_workflow_reports_workflow_id ON workflow_reports(workflow_id);
CREATE INDEX idx_workflow_reports_status ON workflow_reports(status);
CREATE INDEX idx_workflow_reports_next_generation_at ON workflow_reports(next_generation_at);
CREATE INDEX idx_workflow_reports_deleted_at ON workflow_reports(deleted_at) WHERE deleted_at IS NULL;

CREATE INDEX idx_workflow_report_history_workflow_id ON workflow_report_history(workflow_id);
CREATE INDEX idx_workflow_report_history_workflow_report_id ON workflow_report_history(workflow_report_id);
CREATE INDEX idx_workflow_report_history_generated_at ON workflow_report_history(generated_at);

