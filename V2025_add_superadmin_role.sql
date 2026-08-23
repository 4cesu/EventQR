-- Migration: Add SUPERADMIN role to user profiles
-- SUPERADMIN inherits all ADMIN capabilities plus exclusive admin-account-creation power

-- The AccountRole enum already includes SUPER_ADMIN in the backend code
-- This migration ensures the database column accepts SUPER_ADMIN as a valid role value

-- If using a CHECK constraint on role column, update it:
-- ALTER TABLE user_profiles DROP CONSTRAINT IF EXISTS user_profiles_role_check;
-- ALTER TABLE user_profiles ADD CONSTRAINT user_profiles_role_check 
--     CHECK (role IN ('ATTENDEE', 'ORGANIZER', 'STAFF', 'ADMIN', 'SUPER_ADMIN'));

-- Or if using enum type in PostgreSQL:
-- ALTER TYPE account_role ADD VALUE 'SUPER_ADMIN';

-- For standard VARCHAR column with no constraint, no schema change needed
-- The application enum already includes SUPER_ADMIN

-- Optional: Create a SUPER_ADMIN user for testing (adjust email/password as needed)
-- INSERT INTO user_profiles (id, email, full_name, phone_number, password_hash, role, status, created_at, updated_at)
-- VALUES (gen_random_uuid(), 'superadmin@eventqr.local', 'Super Admin', '+10000000000', 
--         '$2a$10$...', 'SUPER_ADMIN', 'ACTIVE', NOW(), NOW())
-- ON CONFLICT (email) DO NOTHING;