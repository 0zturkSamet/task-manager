-- Add role column to users table for system-wide authorization
ALTER TABLE users
ADD COLUMN IF NOT EXISTS role VARCHAR(20) NOT NULL DEFAULT 'USER';

-- Add check constraint to ensure only valid roles
ALTER TABLE users
ADD CONSTRAINT chk_user_role CHECK (role IN ('USER', 'ADMIN'));

-- Create index on role column for query performance
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

-- Add comment for documentation
COMMENT ON COLUMN users.role IS 'System-wide user role: USER (regular user) or ADMIN (system administrator with access to all resources)';
