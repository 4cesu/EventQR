-- V27__revoked_tokens.sql
-- Durable logout denylist.
--
-- JwtService used to keep revoked bearer tokens only in per-JVM memory, so a restart or a
-- second instance forgot every logout and the token worked again until it expired. This
-- table makes revocation survive restarts and be shared across instances.
--
-- Only a SHA-256 hex digest of the token is stored, never the token itself. Rows are
-- useless once the token has expired, so expires_at drives a periodic purge.

CREATE TABLE IF NOT EXISTS revoked_token (
    token_hash varchar(64)  PRIMARY KEY,
    expires_at timestamptz  NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_revoked_token_expires_at ON revoked_token (expires_at);
