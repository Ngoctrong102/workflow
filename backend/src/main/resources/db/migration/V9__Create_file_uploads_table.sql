CREATE TABLE file_uploads (
    id VARCHAR(255) PRIMARY KEY,
    trigger_id VARCHAR(255) NOT NULL REFERENCES triggers(id),
    filename VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    status VARCHAR(50) NOT NULL,
    rows_total INTEGER,
    rows_processed INTEGER DEFAULT 0,
    notifications_sent INTEGER DEFAULT 0,
    error TEXT,
    uploaded_at TIMESTAMP NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMP
);

CREATE INDEX idx_file_uploads_trigger_id ON file_uploads(trigger_id);
CREATE INDEX idx_file_uploads_status ON file_uploads(status);
CREATE INDEX idx_file_uploads_uploaded_at ON file_uploads(uploaded_at);

