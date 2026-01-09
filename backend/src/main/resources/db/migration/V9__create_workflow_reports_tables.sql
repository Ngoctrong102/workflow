-- Create workflow_reports table
-- Stores automated report configurations for workflows.
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

-- Indexes
CREATE INDEX idx_workflow_reports_workflow_id ON workflow_reports(workflow_id);
CREATE INDEX idx_workflow_reports_status ON workflow_reports(status);
CREATE INDEX idx_workflow_reports_next_generation_at ON workflow_reports(next_generation_at);
CREATE INDEX idx_workflow_reports_deleted_at ON workflow_reports(deleted_at) WHERE deleted_at IS NULL;

-- Create workflow_report_history table
-- Stores history of generated reports.
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

-- Indexes
CREATE INDEX idx_workflow_report_history_workflow_id ON workflow_report_history(workflow_id);
CREATE INDEX idx_workflow_report_history_workflow_report_id ON workflow_report_history(workflow_report_id);
CREATE INDEX idx_workflow_report_history_generated_at ON workflow_report_history(generated_at);

