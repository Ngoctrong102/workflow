-- Create triggers table
-- Note: Triggers in this table are only definitions/configurations and do not automatically run.
-- They only become active when a trigger node is added to a workflow definition and the workflow is activated.
CREATE TABLE triggers (
    id VARCHAR(255) PRIMARY KEY,
    workflow_id VARCHAR(255) NOT NULL REFERENCES workflows(id) ON DELETE CASCADE,
    node_id VARCHAR(255) NOT NULL,  -- Node ID in workflow definition
    trigger_type VARCHAR(50) NOT NULL CHECK (trigger_type IN ('api-call', 'scheduler', 'event')),
    config JSONB NOT NULL,  -- Trigger configuration
    status VARCHAR(50) NOT NULL DEFAULT 'active',  -- active, inactive
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP,
    UNIQUE(workflow_id, node_id)
);

-- Indexes
CREATE INDEX idx_triggers_workflow_id ON triggers(workflow_id);
CREATE INDEX idx_triggers_trigger_type ON triggers(trigger_type);
CREATE INDEX idx_triggers_status ON triggers(status);
CREATE INDEX idx_triggers_deleted_at ON triggers(deleted_at) WHERE deleted_at IS NULL;
CREATE INDEX idx_triggers_config ON triggers USING GIN (config);

