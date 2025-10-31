-- ====================================================================
-- Row Level Security (RLS) Setup for Task Manager
-- ====================================================================
-- This script is OPTIONAL for your Spring Boot backend.
-- Your backend uses the 'postgres' service role which bypasses RLS.
--
-- However, enabling RLS provides defense-in-depth security and is
-- useful if you ever want to access Supabase directly from frontend.
--
-- Run this in: Supabase Dashboard > SQL Editor
-- ====================================================================

-- ====================================================================
-- 1. USERS TABLE RLS
-- ====================================================================

-- Enable RLS on users table
ALTER TABLE IF EXISTS users ENABLE ROW LEVEL SECURITY;

-- Drop existing policies if any (for re-running this script)
DROP POLICY IF EXISTS "Service role has full access to users" ON users;
DROP POLICY IF EXISTS "Users can read own data" ON users;
DROP POLICY IF EXISTS "Users can update own data" ON users;

-- Policy: Service role (Spring Boot backend) has full access
CREATE POLICY "Service role has full access to users"
ON users
FOR ALL
TO service_role
USING (true)
WITH CHECK (true);

-- Policy: Authenticated users can read their own data
-- (Only needed if using Supabase Auth + direct frontend access)
CREATE POLICY "Users can read own data"
ON users
FOR SELECT
TO authenticated
USING (auth.uid()::text = id::text);

-- Policy: Authenticated users can update their own data
-- (Only needed if using Supabase Auth + direct frontend access)
CREATE POLICY "Users can update own data"
ON users
FOR UPDATE
TO authenticated
USING (auth.uid()::text = id::text)
WITH CHECK (auth.uid()::text = id::text);

-- ====================================================================
-- 2. PROJECTS TABLE RLS (Will be created in Phase 2)
-- ====================================================================
-- Uncomment and run these after creating the projects table

/*
-- Enable RLS on projects table
ALTER TABLE IF EXISTS projects ENABLE ROW LEVEL SECURITY;

-- Service role has full access
CREATE POLICY "Service role has full access to projects"
ON projects
FOR ALL
TO service_role
USING (true)
WITH CHECK (true);

-- Users can only see projects where they are members
CREATE POLICY "Users can view their projects"
ON projects
FOR SELECT
TO authenticated
USING (
  EXISTS (
    SELECT 1 FROM project_members
    WHERE project_members.project_id = projects.id
    AND project_members.user_id::text = auth.uid()::text
  )
);

-- Project owners can update their projects
CREATE POLICY "Owners can update projects"
ON projects
FOR UPDATE
TO authenticated
USING (owner_id::text = auth.uid()::text)
WITH CHECK (owner_id::text = auth.uid()::text);

-- Project owners can delete their projects
CREATE POLICY "Owners can delete projects"
ON projects
FOR DELETE
TO authenticated
USING (owner_id::text = auth.uid()::text);
*/

-- ====================================================================
-- 3. PROJECT_MEMBERS TABLE RLS (Will be created in Phase 2)
-- ====================================================================

/*
-- Enable RLS on project_members table
ALTER TABLE IF EXISTS project_members ENABLE ROW LEVEL SECURITY;

-- Service role has full access
CREATE POLICY "Service role has full access to project_members"
ON project_members
FOR ALL
TO service_role
USING (true)
WITH CHECK (true);

-- Users can view members of projects they belong to
CREATE POLICY "Users can view members of their projects"
ON project_members
FOR SELECT
TO authenticated
USING (
  EXISTS (
    SELECT 1 FROM project_members pm
    WHERE pm.project_id = project_members.project_id
    AND pm.user_id::text = auth.uid()::text
  )
);
*/

-- ====================================================================
-- 4. TASKS TABLE RLS (Will be created in Phase 3)
-- ====================================================================

/*
-- Enable RLS on tasks table
ALTER TABLE IF EXISTS tasks ENABLE ROW LEVEL SECURITY;

-- Service role has full access
CREATE POLICY "Service role has full access to tasks"
ON tasks
FOR ALL
TO service_role
USING (true)
WITH CHECK (true);

-- Users can view tasks in projects they are members of
CREATE POLICY "Users can view tasks in their projects"
ON tasks
FOR SELECT
TO authenticated
USING (
  EXISTS (
    SELECT 1 FROM project_members
    WHERE project_members.project_id = tasks.project_id
    AND project_members.user_id::text = auth.uid()::text
  )
);

-- Project members can create tasks
CREATE POLICY "Project members can create tasks"
ON tasks
FOR INSERT
TO authenticated
WITH CHECK (
  EXISTS (
    SELECT 1 FROM project_members
    WHERE project_members.project_id = tasks.project_id
    AND project_members.user_id::text = auth.uid()::text
  )
);

-- Task creators and project owners can update tasks
CREATE POLICY "Creators and owners can update tasks"
ON tasks
FOR UPDATE
TO authenticated
USING (
  created_by_id::text = auth.uid()::text
  OR EXISTS (
    SELECT 1 FROM projects
    WHERE projects.id = tasks.project_id
    AND projects.owner_id::text = auth.uid()::text
  )
)
WITH CHECK (
  created_by_id::text = auth.uid()::text
  OR EXISTS (
    SELECT 1 FROM projects
    WHERE projects.id = tasks.project_id
    AND projects.owner_id::text = auth.uid()::text
  )
);

-- Task creators and project owners can delete tasks
CREATE POLICY "Creators and owners can delete tasks"
ON tasks
FOR DELETE
TO authenticated
USING (
  created_by_id::text = auth.uid()::text
  OR EXISTS (
    SELECT 1 FROM projects
    WHERE projects.id = tasks.project_id
    AND projects.owner_id::text = auth.uid()::text
  )
);
*/

-- ====================================================================
-- VERIFICATION QUERIES
-- ====================================================================
-- Run these to verify RLS is enabled:

-- Check which tables have RLS enabled
SELECT schemaname, tablename, rowsecurity
FROM pg_tables
WHERE schemaname = 'public';

-- View all policies
SELECT schemaname, tablename, policyname, permissive, roles, cmd, qual
FROM pg_policies
WHERE schemaname = 'public';

-- ====================================================================
-- NOTES
-- ====================================================================
-- 1. Your Spring Boot backend uses 'postgres' user (service_role)
--    which BYPASSES all RLS policies automatically
--
-- 2. RLS only affects direct Supabase client access (e.g., from frontend)
--
-- 3. If you're only using Spring Boot API, RLS is optional but recommended
--    as an extra security layer
--
-- 4. To disable RLS on a table:
--    ALTER TABLE table_name DISABLE ROW LEVEL SECURITY;
--
-- 5. To drop a policy:
--    DROP POLICY "policy_name" ON table_name;
-- ====================================================================
