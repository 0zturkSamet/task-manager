-- Create project_members table for managing team collaboration
CREATE TABLE IF NOT EXISTS project_members (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_project_members_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_project_members_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_project_user UNIQUE (project_id, user_id)
);

-- Add index for faster queries
CREATE INDEX IF NOT EXISTS idx_project_members_project ON project_members(project_id);
CREATE INDEX IF NOT EXISTS idx_project_members_user ON project_members(user_id);

-- Add assigned_to column to tasks table
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='tasks' AND column_name='assigned_to_id') THEN
        ALTER TABLE tasks ADD COLUMN assigned_to_id UUID;
    END IF;
END $$;

ALTER TABLE tasks DROP CONSTRAINT IF EXISTS fk_tasks_assigned_to;
ALTER TABLE tasks ADD CONSTRAINT fk_tasks_assigned_to FOREIGN KEY (assigned_to_id) REFERENCES users(id) ON DELETE SET NULL;

-- Create index for task assignee queries
CREATE INDEX IF NOT EXISTS idx_tasks_assigned_to ON tasks(assigned_to_id);

-- Automatically add project creator as OWNER when project is created
-- This will be handled in the application layer, but we can add existing project owners
INSERT INTO project_members (project_id, user_id, role, joined_at)
SELECT id, owner_id, 'OWNER', created_at
FROM projects
ON CONFLICT (project_id, user_id) DO NOTHING;
