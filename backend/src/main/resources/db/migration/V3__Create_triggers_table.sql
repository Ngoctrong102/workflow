CREATE TABLE triggers (
    id VARCHAR(255) PRIMARY KEY,
    workflow_id VARCHAR(255) NOT NULL REFERENCES workflows(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL,
    config JSONB NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'active',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP
);

CREATE INDEX idx_triggers_workflow_id ON triggers(workflow_id);
CREATE INDEX idx_triggers_type ON triggers(type);
CREATE INDEX idx_triggers_status ON triggers(status);
CREATE INDEX idx_triggers_deleted_at ON triggers(deleted_at) WHERE deleted_at IS NULL;

