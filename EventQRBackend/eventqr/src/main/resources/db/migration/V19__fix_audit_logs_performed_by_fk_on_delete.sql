-- Fix: performed_by_user_id FK used ON DELETE RESTRICT, blocking user deletion.
-- target_user_id already has ON DELETE SET NULL (set in V18), so only performed_by needs change.

-- Drop the existing RESTRICT constraint
ALTER TABLE audit_logs DROP CONSTRAINT IF EXISTS fk_audit_logs_performed_by;

-- Recreate with SET NULL so deleting a user nulls out audit log references
ALTER TABLE audit_logs
    ADD CONSTRAINT fk_audit_logs_performed_by
    FOREIGN KEY (performed_by_user_id)
    REFERENCES user_profiles(id)
    ON DELETE SET NULL;
