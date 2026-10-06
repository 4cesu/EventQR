-- V30__stored_file_storage_key.sql
-- Lets a stored file live in object storage (S3 or compatible) instead of the bytea column.
--
-- storage_key is the object key when the bytes are in the bucket; it stays NULL for files that
-- are still in the database. content becomes nullable because object-stored files keep no copy
-- there. Existing rows are untouched and keep being served from the database.

ALTER TABLE stored_files ADD COLUMN IF NOT EXISTS storage_key varchar(512);
ALTER TABLE stored_files ALTER COLUMN content DROP NOT NULL;
