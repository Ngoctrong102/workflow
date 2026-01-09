-- Create actions table
-- Stores action definitions in the registry. Actions must be defined here before they can be used in workflows.
CREATE TABLE actions (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,  -- api-call, publish-event, function, custom-action
    action_type VARCHAR(50),  -- For custom actions (send-email, send-sms, etc.)
    description TEXT,
    config_template JSONB NOT NULL,  -- Configuration template
    metadata JSONB,  -- Icon, color, version, etc.
    version VARCHAR(50) NOT NULL DEFAULT '1.0.0',
    enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP
);

-- Indexes
CREATE INDEX idx_actions_type ON actions(type);
CREATE INDEX idx_actions_action_type ON actions(action_type);
CREATE INDEX idx_actions_enabled ON actions(enabled);
CREATE INDEX idx_actions_deleted_at ON actions(deleted_at) WHERE deleted_at IS NULL;

