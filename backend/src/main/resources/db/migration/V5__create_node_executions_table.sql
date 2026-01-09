-- Create node_executions table
-- Stores individual node execution records with comprehensive data for troubleshooting, reporting, and analytics.
CREATE TABLE node_executions (
    id VARCHAR(255) PRIMARY KEY,
    execution_id VARCHAR(255) NOT NULL REFERENCES executions(id) ON DELETE CASCADE,
    node_id VARCHAR(255) NOT NULL,
    node_label VARCHAR(255),  -- Human-readable node label
    node_type VARCHAR(50) NOT NULL,  -- trigger, logic, action
    node_sub_type VARCHAR(50),  -- api-call, condition, delay, etc.
    registry_id VARCHAR(255),  -- Registry ID for trigger/action nodes
    
    status VARCHAR(50) NOT NULL,  -- RUNNING, WAITING, PAUSED, COMPLETED, FAILED
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    duration INTEGER,  -- Duration in milliseconds
    
    -- Comprehensive node execution data (JSONB)
    input_data JSONB NOT NULL,  -- Input data to the node
    output_data JSONB,  -- Output data from the node
    node_config JSONB,  -- Node configuration at execution time
    execution_context JSONB,  -- Execution context available to node
    
    error TEXT,
    error_details JSONB,  -- Detailed error information
    
    retry_count INTEGER DEFAULT 0,
    retry_details JSONB,  -- Retry attempts information (Resilience4j)
    
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Indexes for JSONB queries
CREATE INDEX idx_node_executions_execution_id ON node_executions(execution_id);
CREATE INDEX idx_node_executions_node_id ON node_executions(node_id);
CREATE INDEX idx_node_executions_node_type ON node_executions(node_type);
CREATE INDEX idx_node_executions_status ON node_executions(status);
CREATE INDEX idx_node_executions_started_at ON node_executions(started_at);
CREATE INDEX idx_node_executions_input_data ON node_executions USING GIN (input_data);
CREATE INDEX idx_node_executions_output_data ON node_executions USING GIN (output_data);
CREATE INDEX idx_node_executions_node_config ON node_executions USING GIN (node_config);

