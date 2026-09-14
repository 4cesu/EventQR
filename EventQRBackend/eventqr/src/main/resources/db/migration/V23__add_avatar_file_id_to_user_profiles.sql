BEGIN;
ALTER TABLE user_profiles ADD COLUMN IF NOT EXISTS avatar_file_id_new VARCHAR(255);
UPDATE user_profiles SET avatar_file_id_new = COALESCE(avatar_file_id::varchar, '');
ALTER TABLE user_profiles DROP COLUMN IF EXISTS avatar_file_id;
ALTER TABLE user_profiles RENAME COLUMN avatar_file_id_new TO avatar_file_id;
COMMIT;