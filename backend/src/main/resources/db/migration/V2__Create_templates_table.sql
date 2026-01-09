CREATE TABLE templates (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    channel VARCHAR(50) NOT NULL,
    subject VARCHAR(500),
    body TEXT NOT NULL,
    variables JSONB,
    category VARCHAR(100),
    tags TEXT[],
    version INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP
);

CREATE INDEX idx_templates_channel ON templates(channel);
CREATE INDEX idx_templates_category ON templates(category);
CREATE INDEX idx_templates_deleted_at ON templates(deleted_at) WHERE deleted_at IS NULL;

