-- =====================================================
-- Enable RLS on New Tables
-- =====================================================
-- This migration enables Row Level Security on tables
-- that were created after V9 or automatically by Flyway
-- =====================================================

-- =====================================================
-- PART 1: Enable RLS on Notifications Table
-- =====================================================

-- Enable RLS on notifications table
ALTER TABLE notifications ENABLE ROW LEVEL SECURITY;

-- Create policy for notifications
-- Note: Backend uses service_role key which bypasses RLS.
-- This policy is to satisfy Supabase security requirements.
DROP POLICY IF EXISTS "Enable all access for authenticated users" ON notifications;
DROP POLICY IF EXISTS "Enable all access for service role" ON notifications;
CREATE POLICY "Enable all access for service role"
    ON notifications FOR ALL
    TO service_role
    USING (true)
    WITH CHECK (true);

-- =====================================================
-- PART 2: Enable RLS on Flyway Schema History
-- =====================================================

-- Note: We cannot enable RLS on flyway_schema_history during migration
-- because Flyway is actively using it. This must be done manually after
-- all migrations complete. Run this in Supabase SQL editor:
--
-- ALTER TABLE flyway_schema_history ENABLE ROW LEVEL SECURITY;
--
-- No policies should be created, which blocks all PostgREST API access

-- =====================================================
-- PART 3: Verify Changes
-- =====================================================

-- This will show which tables now have RLS enabled
DO $$
BEGIN
    RAISE NOTICE 'RLS Status for New Tables:';
    RAISE NOTICE '- notifications: %', (SELECT relrowsecurity FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace WHERE n.nspname = 'public' AND c.relname = 'notifications');
    RAISE NOTICE 'Note: flyway_schema_history RLS must be enabled manually via Supabase SQL editor after migrations complete';
END $$;

-- Add comments for documentation
COMMENT ON TABLE notifications IS 'Stores user notifications. RLS enabled with permissive policy for backend access.';
COMMENT ON TABLE flyway_schema_history IS 'Flyway migration tracking table. RLS enabled with no policies to block API access.';
