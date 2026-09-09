-- V20__user_token_revocation.sql
-- DB-backed account-disable token revocation.
--
-- Replaces the per-JVM in-memory Caffeine marker in JwtService. A row here records
-- the instant an account was disabled/suspended so that any bearer token issued
-- at-or-before `revoked_at` is rejected by JwtAuthenticationFilter. The row is
-- intentionally RETAINED when the account is re-enabled, so tokens issued before a
-- previous disable do not come back to life without a fresh login.
--
-- Primary key is user_id because one revocation marker exists per user; no separate
-- surrogate id. A foreign key to user_profiles with ON DELETE CASCADE keeps the disk
-- clean when a user is hard-deleted.

CREATE TABLE user_token_revocation (
    user_id    uuid        PRIMARY KEY REFERENCES user_profiles(id) ON DELETE CASCADE,
    revoked_at timestamptz NOT NULL
);
