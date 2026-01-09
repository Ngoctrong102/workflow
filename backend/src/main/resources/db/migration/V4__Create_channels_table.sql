CREATE TABLE channels (
    id VARCHAR(255) PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    provider VARCHAR(100),
    config JSONB NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'active',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP
);

CREATE INDEX idx_channels_type ON channels(type);
CREATE INDEX idx_channels_status ON channels(status);
CREATE INDEX idx_channels_deleted_at ON channels(deleted_at) WHERE deleted_at IS NULL;

