-- Object types table for object type definitions and field management
CREATE TABLE object_types (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    fields JSONB NOT NULL, -- Array of field definitions
    tags TEXT[],
    version INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP -- Soft delete
);

-- Indexes for performance
CREATE INDEX idx_object_types_name ON object_types(name);
CREATE INDEX idx_object_types_deleted_at ON object_types(deleted_at) WHERE deleted_at IS NULL;
CREATE INDEX idx_object_types_tags ON object_types USING GIN(tags);

