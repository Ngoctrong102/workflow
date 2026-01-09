-- Add description and status columns to templates table
ALTER TABLE templates ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE templates ADD COLUMN IF NOT EXISTS status VARCHAR(50);

-- Create index on status if it doesn't exist
CREATE INDEX IF NOT EXISTS idx_templates_status ON templates(status) WHERE status IS NOT NULL;

