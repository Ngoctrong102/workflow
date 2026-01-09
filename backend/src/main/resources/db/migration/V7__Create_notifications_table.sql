CREATE TABLE notifications (
    id VARCHAR(255) PRIMARY KEY,
    execution_id VARCHAR(255) REFERENCES executions(id),
    workflow_id VARCHAR(255) REFERENCES workflows(id),
    channel VARCHAR(50) NOT NULL,
    template_id VARCHAR(255) REFERENCES templates(id),
    status VARCHAR(50) NOT NULL,
    recipients_count INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    sent_at TIMESTAMP
);

CREATE INDEX idx_notifications_execution_id ON notifications(execution_id);
CREATE INDEX idx_notifications_workflow_id ON notifications(workflow_id);
CREATE INDEX idx_notifications_channel ON notifications(channel);
CREATE INDEX idx_notifications_status ON notifications(status);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);

