-- Sprint 28: Refactor Trigger Entity
-- Remove workflow_id and node_id, add name field
-- Triggers are now independent and can be shared across workflows

-- Step 1: Add name column (nullable first, will be populated then set to NOT NULL)
ALTER TABLE triggers ADD COLUMN IF NOT EXISTS name VARCHAR(255);

-- Step 2: Populate name from config or generate default
-- Try to extract name from config JSON, fallback to generated name
UPDATE triggers 
SET name = COALESCE(
    -- Try to extract from config->>'name' if exists
    (config->>'name'),
    -- Try to extract from config->>'endpointPath' for API triggers
    CASE 
        WHEN trigger_type = 'api-call' AND config->>'endpointPath' IS NOT NULL 
        THEN 'API Trigger: ' || (config->>'endpointPath')
        ELSE NULL
    END,
    -- Try to extract from config->>'cronExpression' for scheduler triggers
    CASE 
        WHEN trigger_type = 'scheduler' AND config->>'cronExpression' IS NOT NULL 
        THEN 'Schedule Trigger: ' || (config->>'cronExpression')
        ELSE NULL
    END,
    -- Try to extract from config->>'topic' for event triggers
    CASE 
        WHEN trigger_type = 'event' AND config->>'topic' IS NOT NULL 
        THEN 'Event Trigger: ' || (config->>'topic')
        ELSE NULL
    END,
    -- Fallback to generated name
    'Trigger ' || id
)
WHERE name IS NULL;

-- Step 3: Set name to NOT NULL after populating
ALTER TABLE triggers ALTER COLUMN name SET NOT NULL;

-- Step 4: Drop foreign key constraint on workflow_id
ALTER TABLE triggers DROP CONSTRAINT IF EXISTS triggers_workflow_id_fkey;

-- Step 5: Drop indexes on workflow_id and node_id
DROP INDEX IF EXISTS idx_triggers_workflow_id;
DROP INDEX IF EXISTS idx_triggers_node_id;

-- Step 6: Remove workflow_id column
ALTER TABLE triggers DROP COLUMN IF EXISTS workflow_id;

-- Step 7: Remove node_id column
ALTER TABLE triggers DROP COLUMN IF EXISTS node_id;

-- Step 8: Create index on name for faster lookups
CREATE INDEX IF NOT EXISTS idx_triggers_name ON triggers(name);

-- Step 9: Verify Execution entity still has trigger_id foreign key (should remain)
-- No changes needed to executions table - trigger_id foreign key remains valid

-- Note: This migration assumes triggers table exists and has data
-- Backup database before running this migration

