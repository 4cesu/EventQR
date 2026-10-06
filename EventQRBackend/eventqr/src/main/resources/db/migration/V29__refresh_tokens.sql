-- V29__refresh_tokens.sql
-- Rotating refresh tokens.
--
-- Access tokens become short-lived; a refresh token lets the app get a new one without the
-- user signing in again. Each refresh token is single-use: refreshing marks it used and issues
-- a successor in the same family. If a token that was already used is presented again outside a
-- short retry window, the whole family is revoked (the token was probably stolen).
--
-- Only a SHA-256 hex digest of the token is stored, never the token itself.

CREATE TABLE IF NOT EXISTS refresh_token (
    id         uuid        PRIMARY KEY,
    user_id    uuid        NOT NULL REFERENCES user_profiles(id) ON DELETE CASCADE,
    family_id  uuid        NOT NULL,
    token_hash varchar(64) NOT NULL,
    created_at timestamptz NOT NULL,
    expires_at timestamptz NOT NULL,
    used_at    timestamptz,
    revoked_at timestamptz
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_refresh_token_hash ON refresh_token (token_hash);
CREATE INDEX IF NOT EXISTS idx_refresh_token_user ON refresh_token (user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_token_family ON refresh_token (family_id);
CREATE INDEX IF NOT EXISTS idx_refresh_token_expires_at ON refresh_token (expires_at);

-- Supabase exposes every table in the public schema through its REST API to the anon and
-- authenticated roles unless row level security is on. The backend connects as the table owner
-- (which bypasses RLS), so enabling RLS with no policies locks the tables to the backend only.
-- revoked_token was created by V27 without this; it is closed here rather than editing V27,
-- whose checksum is already recorded in deployed databases.
ALTER TABLE refresh_token ENABLE ROW LEVEL SECURITY;
ALTER TABLE revoked_token ENABLE ROW LEVEL SECURITY;
