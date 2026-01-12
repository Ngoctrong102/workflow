-- Fix triggers table: Remove node_id and workflow_id columns
-- This script fixes the database schema to match the new design where
-- trigger configs are independent and don't have node_id or workflow_id

-- Step 1: Check if columns exist
DO $$
BEGIN
    -- Step 2: Drop foreign key constraint on workflow_id if exists
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'triggers_workflow_id_fkey' 
        AND table_name = 'triggers'
    ) THEN
        ALTER TABLE triggers DROP CONSTRAINT triggers_workflow_id_fkey;
        RAISE NOTICE 'Dropped foreign key constraint: triggers_workflow_id_fkey';
    END IF;

    -- Step 3: Drop indexes on workflow_id and node_id if they exist
    DROP INDEX IF EXISTS idx_triggers_workflow_id;
    DROP INDEX IF EXISTS idx_triggers_node_id;
    RAISE NOTICE 'Dropped indexes on workflow_id and node_id';

    -- Step 4: Add name column if it doesn't exist
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'triggers' AND column_name = 'name'
    ) THEN
        ALTER TABLE triggers ADD COLUMN name VARCHAR(255);
        RAISE NOTICE 'Added name column';
        
        -- Populate name from config or generate default
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
            -- Try to extract from config->'kafka'->>'topic' for event triggers
            CASE 
                WHEN trigger_type = 'event' AND config->'kafka'->>'topic' IS NOT NULL 
                THEN 'Event Trigger: ' || (config->'kafka'->>'topic')
                ELSE NULL
            END,
            -- Fallback to generated name
            'Trigger ' || id
        )
        WHERE name IS NULL;
        
        -- Set name to NOT NULL after populating
        ALTER TABLE triggers ALTER COLUMN name SET NOT NULL;
        RAISE NOTICE 'Populated name column and set to NOT NULL';
    ELSE
        RAISE NOTICE 'Name column already exists';
    END IF;

    -- Step 5: Remove workflow_id column if it exists
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'triggers' AND column_name = 'workflow_id'
    ) THEN
        ALTER TABLE triggers DROP COLUMN workflow_id;
        RAISE NOTICE 'Dropped workflow_id column';
    ELSE
        RAISE NOTICE 'workflow_id column does not exist';
    END IF;

    -- Step 6: Remove node_id column if it exists
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'triggers' AND column_name = 'node_id'
    ) THEN
        ALTER TABLE triggers DROP COLUMN node_id;
        RAISE NOTICE 'Dropped node_id column';
    ELSE
        RAISE NOTICE 'node_id column does not exist';
    END IF;

    -- Step 7: Create index on name if it doesn't exist
    CREATE INDEX IF NOT EXISTS idx_triggers_name ON triggers(name);
    RAISE NOTICE 'Created index on name column';

    RAISE NOTICE 'Migration completed successfully!';
END $$;

-- Verify the final schema
SELECT 
    column_name, 
    data_type, 
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_name = 'triggers' 
ORDER BY ordinal_position;

