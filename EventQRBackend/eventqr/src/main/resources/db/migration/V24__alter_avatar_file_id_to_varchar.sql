ALTER TABLE user_profiles ALTER COLUMN avatar_file_id TYPE VARCHAR(255) USING avatar_file_id::varchar;
