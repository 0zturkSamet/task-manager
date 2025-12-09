-- V14: Create time_entries table for time tracking functionality
-- This table stores individual time tracking entries linked to tasks

CREATE TABLE time_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    task_id UUID,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    duration_minutes INTEGER,
    description TEXT,
    is_manual BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign key constraints
    CONSTRAINT fk_time_entries_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_time_entries_task FOREIGN KEY (task_id)
        REFERENCES tasks(id) ON DELETE CASCADE,

    -- Business logic constraint: end_time must be after start_time
    CONSTRAINT valid_time_range CHECK (end_time IS NULL OR end_time > start_time)
);

-- Indexes for performance optimization
CREATE INDEX idx_time_entries_user_id ON time_entries(user_id);
CREATE INDEX idx_time_entries_task_id ON time_entries(task_id);
CREATE INDEX idx_time_entries_start_time ON time_entries(start_time);
CREATE INDEX idx_time_entries_created_at ON time_entries(created_at);

-- Index for finding running timers (where end_time is NULL)
CREATE INDEX idx_time_entries_running ON time_entries(user_id, end_time)
    WHERE end_time IS NULL;

-- Index for date range queries
CREATE INDEX idx_time_entries_date_range ON time_entries(user_id, start_time, end_time);

-- Create a function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_time_entries_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger to automatically update updated_at
CREATE TRIGGER trigger_update_time_entries_updated_at
    BEFORE UPDATE ON time_entries
    FOR EACH ROW
    EXECUTE FUNCTION update_time_entries_updated_at();

-- Optional: Create a view for easy querying of task time summaries
CREATE OR REPLACE VIEW task_time_summary AS
SELECT
    t.id as task_id,
    t.title,
    t.estimated_hours,
    COALESCE(SUM(te.duration_minutes), 0) / 60.0 as actual_hours_from_entries,
    COUNT(te.id) as entry_count,
    t.estimated_hours - (COALESCE(SUM(te.duration_minutes), 0) / 60.0) as remaining_hours
FROM tasks t
LEFT JOIN time_entries te ON t.id = te.task_id AND te.end_time IS NOT NULL
GROUP BY t.id, t.title, t.estimated_hours;

-- Optional: Create a view for daily time summaries
CREATE OR REPLACE VIEW daily_time_summary AS
SELECT
    user_id,
    DATE(start_time) as date,
    SUM(duration_minutes) / 60.0 as total_hours,
    COUNT(*) as entry_count
FROM time_entries
WHERE end_time IS NOT NULL
GROUP BY user_id, DATE(start_time);
