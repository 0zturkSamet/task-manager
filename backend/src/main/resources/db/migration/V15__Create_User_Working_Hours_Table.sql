-- Create user_working_hours table
CREATE TABLE IF NOT EXISTS user_working_hours (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    expected_hours_per_day DECIMAL(5, 2) NOT NULL DEFAULT 8.00,
    expected_hours_per_week DECIMAL(5, 2) NOT NULL DEFAULT 40.00,
    working_days TEXT NOT NULL DEFAULT '["MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY"]',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_working_hours_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT unique_user_working_hours UNIQUE (user_id)
);

-- Create index on user_id for faster lookups
CREATE INDEX IF NOT EXISTS idx_user_working_hours_user_id ON user_working_hours(user_id);

-- Add comments
COMMENT ON TABLE user_working_hours IS 'Stores expected working hours configuration per user for overtime and missed hours tracking';
COMMENT ON COLUMN user_working_hours.expected_hours_per_day IS 'Expected work hours per day (e.g., 8.00)';
COMMENT ON COLUMN user_working_hours.expected_hours_per_week IS 'Expected work hours per week (e.g., 40.00)';
COMMENT ON COLUMN user_working_hours.working_days IS 'JSON array of working days (e.g., ["MONDAY", "TUESDAY", ...])';
