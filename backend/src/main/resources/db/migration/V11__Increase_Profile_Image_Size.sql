-- Increase profile_image column size to support base64 encoded images
-- Base64 images can be very large (2MB file = ~2.7MB base64)
ALTER TABLE users ALTER COLUMN profile_image TYPE TEXT;

-- Add comment for documentation
COMMENT ON COLUMN users.profile_image IS 'Profile image URL or base64 encoded image data';
