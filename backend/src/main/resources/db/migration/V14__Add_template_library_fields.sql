-- Add template library and sharing fields
ALTER TABLE templates ADD COLUMN IF NOT EXISTS is_library BOOLEAN DEFAULT FALSE;
ALTER TABLE templates ADD COLUMN IF NOT EXISTS is_public BOOLEAN DEFAULT FALSE;
ALTER TABLE templates ADD COLUMN IF NOT EXISTS owner_id VARCHAR(255);
ALTER TABLE templates ADD COLUMN IF NOT EXISTS library_category VARCHAR(100);
ALTER TABLE templates ADD COLUMN IF NOT EXISTS install_count INTEGER DEFAULT 0;

-- Indexes
CREATE INDEX IF NOT EXISTS idx_templates_is_library ON templates(is_library);
CREATE INDEX IF NOT EXISTS idx_templates_is_public ON templates(is_public);
CREATE INDEX IF NOT EXISTS idx_templates_library_category ON templates(library_category);
CREATE INDEX IF NOT EXISTS idx_templates_owner_id ON templates(owner_id);

