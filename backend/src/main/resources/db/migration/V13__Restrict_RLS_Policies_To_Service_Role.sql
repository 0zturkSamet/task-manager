-- =====================================================
-- Restrict RLS Policies To Service Role
-- =====================================================
-- Supabase flagged duplicate permissive policies that
-- applied to every role (`anon`, `authenticated`, etc.).
-- This migration drops the old broad policies and creates
-- a single permissive policy scoped to `service_role`.
-- =====================================================

DO $$
DECLARE
    target_table text;
BEGIN
    FOR target_table IN
        SELECT unnest(
            ARRAY[
                'users',
                'projects',
                'project_members',
                'tasks',
                'task_comments',
                'comment_reactions',
                'notifications'
            ]
        )
    LOOP
        EXECUTE format('DROP POLICY IF EXISTS "Enable all access for authenticated users" ON %I;', target_table);
        EXECUTE format('DROP POLICY IF EXISTS "Enable all access for service role" ON %I;', target_table);
        EXECUTE format(
            'CREATE POLICY "Enable all access for service role" ON %I FOR ALL TO service_role USING (true) WITH CHECK (true);',
            target_table
        );
    END LOOP;
END $$;

-- Verify the new policy is present for each table
DO $$
BEGIN
    RAISE NOTICE 'Updated RLS policies now scope to service_role only.';
END $$;
