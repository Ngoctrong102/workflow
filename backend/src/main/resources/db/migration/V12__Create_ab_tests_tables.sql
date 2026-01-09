CREATE TABLE ab_tests (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    workflow_id VARCHAR(255) REFERENCES workflows(id),
    status VARCHAR(50) NOT NULL DEFAULT 'draft', -- draft, running, paused, completed, archived
    success_metric VARCHAR(100) NOT NULL DEFAULT 'open_rate', -- open_rate, click_rate, conversion_rate, engagement_rate
    traffic_split_type VARCHAR(50) NOT NULL DEFAULT 'equal', -- equal, custom, weighted
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    min_sample_size INTEGER DEFAULT 1000,
    confidence_level DECIMAL(5,2) DEFAULT 95.00,
    winner_variant_id VARCHAR(255),
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP
);

CREATE TABLE ab_test_variants (
    id VARCHAR(255) PRIMARY KEY,
    ab_test_id VARCHAR(255) NOT NULL REFERENCES ab_tests(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL, -- A, B, C, etc.
    label VARCHAR(255), -- Descriptive label
    template_id VARCHAR(255) REFERENCES templates(id),
    channel_id VARCHAR(255) REFERENCES channels(id),
    config JSONB, -- Variant-specific configuration
    traffic_percentage DECIMAL(5,2) DEFAULT 50.00, -- Percentage of traffic for this variant
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP
);

CREATE TABLE ab_test_results (
    id VARCHAR(255) PRIMARY KEY,
    ab_test_id VARCHAR(255) NOT NULL REFERENCES ab_tests(id) ON DELETE CASCADE,
    variant_id VARCHAR(255) NOT NULL REFERENCES ab_test_variants(id) ON DELETE CASCADE,
    execution_id VARCHAR(255) REFERENCES executions(id),
    notification_id VARCHAR(255) REFERENCES notifications(id),
    user_id VARCHAR(255),
    assigned_at TIMESTAMP NOT NULL DEFAULT NOW(),
    opened_at TIMESTAMP,
    clicked_at TIMESTAMP,
    converted_at TIMESTAMP,
    engagement_score DECIMAL(10,2),
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE ab_test_aggregates (
    id VARCHAR(255) PRIMARY KEY,
    ab_test_id VARCHAR(255) NOT NULL REFERENCES ab_tests(id) ON DELETE CASCADE,
    variant_id VARCHAR(255) NOT NULL REFERENCES ab_test_variants(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    total_assignments INTEGER DEFAULT 0,
    total_opens INTEGER DEFAULT 0,
    total_clicks INTEGER DEFAULT 0,
    total_conversions INTEGER DEFAULT 0,
    total_engagement_score DECIMAL(15,2) DEFAULT 0,
    open_rate DECIMAL(10,4) DEFAULT 0,
    click_rate DECIMAL(10,4) DEFAULT 0,
    conversion_rate DECIMAL(10,4) DEFAULT 0,
    engagement_rate DECIMAL(10,4) DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(ab_test_id, variant_id, date)
);

-- Indexes
CREATE INDEX idx_ab_tests_workflow_id ON ab_tests(workflow_id);
CREATE INDEX idx_ab_tests_status ON ab_tests(status);
CREATE INDEX idx_ab_tests_deleted_at ON ab_tests(deleted_at);
CREATE INDEX idx_ab_test_variants_ab_test_id ON ab_test_variants(ab_test_id);
CREATE INDEX idx_ab_test_variants_deleted_at ON ab_test_variants(deleted_at);
CREATE INDEX idx_ab_test_results_ab_test_id ON ab_test_results(ab_test_id);
CREATE INDEX idx_ab_test_results_variant_id ON ab_test_results(variant_id);
CREATE INDEX idx_ab_test_results_user_id ON ab_test_results(user_id);
CREATE INDEX idx_ab_test_results_execution_id ON ab_test_results(execution_id);
CREATE INDEX idx_ab_test_results_notification_id ON ab_test_results(notification_id);
CREATE INDEX idx_ab_test_results_assigned_at ON ab_test_results(assigned_at);
CREATE INDEX idx_ab_test_aggregates_ab_test_id ON ab_test_aggregates(ab_test_id);
CREATE INDEX idx_ab_test_aggregates_variant_id ON ab_test_aggregates(variant_id);
CREATE INDEX idx_ab_test_aggregates_date ON ab_test_aggregates(date);
CREATE INDEX idx_ab_test_aggregates_test_variant_date ON ab_test_aggregates(ab_test_id, variant_id, date);

