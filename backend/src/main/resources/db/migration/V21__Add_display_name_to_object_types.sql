-- Add display_name column to object_types table
ALTER TABLE object_types ADD COLUMN display_name VARCHAR(255);

-- Create index for display_name
CREATE INDEX idx_object_types_display_name ON object_types(display_name);

