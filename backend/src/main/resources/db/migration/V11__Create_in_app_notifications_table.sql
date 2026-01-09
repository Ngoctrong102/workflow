CREATE TABLE in_app_notifications (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    channel_id VARCHAR(255) REFERENCES channels(id),
    workflow_id VARCHAR(255) REFERENCES workflows(id),
    execution_id VARCHAR(255) REFERENCES executions(id),
    title VARCHAR(500) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL DEFAULT 'info', -- info, warning, error, success
    action_url TEXT,
    action_label VARCHAR(255),
    image_url TEXT,
    read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP,
    expires_at TIMESTAMP,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP
);

CREATE INDEX idx_in_app_notifications_user_id ON in_app_notifications(user_id);
CREATE INDEX idx_in_app_notifications_user_id_read ON in_app_notifications(user_id, read);
CREATE INDEX idx_in_app_notifications_user_id_created_at ON in_app_notifications(user_id, created_at DESC);
CREATE INDEX idx_in_app_notifications_channel_id ON in_app_notifications(channel_id);
CREATE INDEX idx_in_app_notifications_workflow_id ON in_app_notifications(workflow_id);
CREATE INDEX idx_in_app_notifications_execution_id ON in_app_notifications(execution_id);
CREATE INDEX idx_in_app_notifications_type ON in_app_notifications(type);
CREATE INDEX idx_in_app_notifications_expires_at ON in_app_notifications(expires_at);
CREATE INDEX idx_in_app_notifications_deleted_at ON in_app_notifications(deleted_at);

