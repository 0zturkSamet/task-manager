-- =====================================================
-- Fix Supabase Security and Performance Issues
-- =====================================================
-- This migration:
-- 1. Enables Row Level Security (RLS) on all tables
-- 2. Creates permissive policies for service role access
-- 3. Removes duplicate indexes
-- =====================================================

-- =====================================================
-- PART 1: Remove Duplicate Indexes
-- =====================================================

-- Drop duplicate indexes on project_members if they exist
DO $$
DECLARE
    index_name text;
BEGIN
    -- Find and drop duplicate indexes
    FOR index_name IN
        SELECT indexname
        FROM pg_indexes
        WHERE tablename = 'project_members'
        AND schemaname = 'public'
        AND indexname LIKE 'idx_project_members_%'
    LOOP
        -- Keep only the original indexes, drop any duplicates
        IF index_name NOT IN ('idx_project_members_project', 'idx_project_members_user') THEN
            EXECUTE 'DROP INDEX IF EXISTS ' || index_name;
            RAISE NOTICE 'Dropped duplicate index: %', index_name;
        END IF;
    END LOOP;
END $$;

-- =====================================================
-- PART 2: Enable Row Level Security (RLS)
-- =====================================================

-- Enable RLS on all tables
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE projects ENABLE ROW LEVEL SECURITY;
ALTER TABLE project_members ENABLE ROW LEVEL SECURITY;
ALTER TABLE tasks ENABLE ROW LEVEL SECURITY;
ALTER TABLE task_comments ENABLE ROW LEVEL SECURITY;
ALTER TABLE comment_reactions ENABLE ROW LEVEL SECURITY;

-- =====================================================
-- PART 3: Create RLS Policies
-- =====================================================
-- Note: These policies allow all operations since the backend
-- uses service_role key which bypasses RLS anyway.
-- This is to satisfy Supabase security warnings.
-- =====================================================

-- Users table policies
DROP POLICY IF EXISTS "Enable all access for authenticated users" ON users;
DROP POLICY IF EXISTS "Enable all access for service role" ON users;
CREATE POLICY "Enable all access for service role"
    ON users FOR ALL
    TO service_role
    USING (true)
    WITH CHECK (true);

-- Projects table policies
DROP POLICY IF EXISTS "Enable all access for authenticated users" ON projects;
DROP POLICY IF EXISTS "Enable all access for service role" ON projects;
CREATE POLICY "Enable all access for service role"
    ON projects FOR ALL
    TO service_role
    USING (true)
    WITH CHECK (true);

-- Project members table policies
DROP POLICY IF EXISTS "Enable all access for authenticated users" ON project_members;
DROP POLICY IF EXISTS "Enable all access for service role" ON project_members;
CREATE POLICY "Enable all access for service role"
    ON project_members FOR ALL
    TO service_role
    USING (true)
    WITH CHECK (true);

-- Tasks table policies
DROP POLICY IF EXISTS "Enable all access for authenticated users" ON tasks;
DROP POLICY IF EXISTS "Enable all access for service role" ON tasks;
CREATE POLICY "Enable all access for service role"
    ON tasks FOR ALL
    TO service_role
    USING (true)
    WITH CHECK (true);

-- Task comments table policies
DROP POLICY IF EXISTS "Enable all access for authenticated users" ON task_comments;
DROP POLICY IF EXISTS "Enable all access for service role" ON task_comments;
CREATE POLICY "Enable all access for service role"
    ON task_comments FOR ALL
    TO service_role
    USING (true)
    WITH CHECK (true);

-- Comment reactions table policies
DROP POLICY IF EXISTS "Enable all access for authenticated users" ON comment_reactions;
DROP POLICY IF EXISTS "Enable all access for service role" ON comment_reactions;
CREATE POLICY "Enable all access for service role"
    ON comment_reactions FOR ALL
    TO service_role
    USING (true)
    WITH CHECK (true);

-- =====================================================
-- PART 4: Verify Changes
-- =====================================================

-- This will show which tables now have RLS enabled
DO $$
BEGIN
    RAISE NOTICE 'RLS Status:';
    RAISE NOTICE '- users: %', (SELECT relrowsecurity FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace WHERE n.nspname = 'public' AND c.relname = 'users');
    RAISE NOTICE '- projects: %', (SELECT relrowsecurity FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace WHERE n.nspname = 'public' AND c.relname = 'projects');
    RAISE NOTICE '- project_members: %', (SELECT relrowsecurity FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace WHERE n.nspname = 'public' AND c.relname = 'project_members');
    RAISE NOTICE '- tasks: %', (SELECT relrowsecurity FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace WHERE n.nspname = 'public' AND c.relname = 'tasks');
    RAISE NOTICE '- task_comments: %', (SELECT relrowsecurity FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace WHERE n.nspname = 'public' AND c.relname = 'task_comments');
    RAISE NOTICE '- comment_reactions: %', (SELECT relrowsecurity FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace WHERE n.nspname = 'public' AND c.relname = 'comment_reactions');
END $$;
