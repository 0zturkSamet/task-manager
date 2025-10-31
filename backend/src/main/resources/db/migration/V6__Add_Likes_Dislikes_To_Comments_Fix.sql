-- Fix: Add likes_count and dislikes_count to task_comments table (if not exists)
ALTER TABLE task_comments
ADD COLUMN IF NOT EXISTS likes_count INTEGER NOT NULL DEFAULT 0,
ADD COLUMN IF NOT EXISTS dislikes_count INTEGER NOT NULL DEFAULT 0;

-- Add comments for documentation
COMMENT ON COLUMN task_comments.likes_count IS 'Number of likes for this comment';
COMMENT ON COLUMN task_comments.dislikes_count IS 'Number of dislikes for this comment';
