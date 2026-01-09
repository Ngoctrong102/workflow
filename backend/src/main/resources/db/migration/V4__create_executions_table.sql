-- Create executions table
-- Stores workflow execution records with comprehensive data for troubleshooting, reporting, and analytics.
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

