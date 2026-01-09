CREATE TABLE deliveries (
    id VARCHAR(255) PRIMARY KEY,
    notification_id VARCHAR(255) NOT NULL REFERENCES notifications(id) ON DELETE CASCADE,
    recipient VARCHAR(255) NOT NULL,
    channel VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    opened_at TIMESTAMP,
    clicked_at TIMESTAMP,
    error TEXT,
    provider_message_id VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_deliveries_notification_id ON deliveries(notification_id);
CREATE INDEX idx_deliveries_recipient ON deliveries(recipient);
CREATE INDEX idx_deliveries_channel ON deliveries(channel);
CREATE INDEX idx_deliveries_status ON deliveries(status);
CREATE INDEX idx_deliveries_created_at ON deliveries(created_at);
CREATE INDEX idx_deliveries_delivered_at ON deliveries(delivered_at);
CREATE INDEX idx_deliveries_channel_status ON deliveries(channel, status);

