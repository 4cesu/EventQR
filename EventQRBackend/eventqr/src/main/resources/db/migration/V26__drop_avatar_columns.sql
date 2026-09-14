BEGIN;
ALTER TABLE user_profiles DROP COLUMN IF EXISTS avatar_file_id;
ALTER TABLE user_profiles DROP COLUMN IF EXISTS avatar_path;
COMMIT;
