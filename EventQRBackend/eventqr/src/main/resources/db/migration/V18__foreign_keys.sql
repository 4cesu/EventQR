-- V18__foreign_keys.sql
-- Add foreign keys selectively based on delete-flow analysis.
--
-- DELETE-FLOW ANALYSIS:
--   - deleteScanPurpose: deletes scan_purposes row ONLY if no transaction_logs reference it.
--     But currently NO FK protects this — a scan_purpose could be deleted even with tx logs.
--     Decision: ADD FK with ON DELETE RESTRICT — safe because app already checks for tx logs
--     before delete (TransactionService.determineDuplicateReason loads history). If app doesn't
--     actually check, RESTRICT will throw a clear DB error instead of silent data corruption.
--
--   - deleteReward: deletes reward row WITHOUT checking reward_redemptions.
--     Decision: ADD FK with ON DELETE RESTRICT — existing code doesn't check redemptions,
--     so this prevents accidental deletion. Force the app to handle it (or add CASCADE if
--     business requires).
--
--   - Registration deletion: no deleteRegistration method found in codebase.
--     Event deletion: no explicit event delete in service layer found.
--
--   - transaction_logs → event_registrations: ON DELETE RESTRICT prevents deleting a
--     registration that has scan history (audit integrity).
--
--   - Notifications related IDs: ON DELETE SET NULL — notifications are append-only history;
--     they should survive the deletion of the linked transaction/reward.
--
--   - qr_credentials → event_registrations: ON DELETE CASCADE — when a registration is
--     cancelled, the QR credential should follow (V4/V5 trigger handles re-issue on next reg).
--     Actually, CANCELLED registrations are NOT deleted (status change), so this FK is
--     defensive-only. Using RESTRICT for safety.
--
--   - Points FKs: ON DELETE CASCADE for source_transaction_id (if tx log deleted, point tx follows).
--     Actually, point_transactions are append-only history — use RESTRICT.
--
-- SAFE TO APPLY: All FKs below use NO ACTION (PostgreSQL default = immediate check).
-- If you need deferred checks for bulk operations, use DEFERRABLE INITIALLY DEFERRED.

BEGIN;

-- event_registrations → events
ALTER TABLE event_registrations
    ADD CONSTRAINT fk_event_registrations_event
    FOREIGN KEY (event_id) REFERENCES events(id)
    ON DELETE RESTRICT;

-- event_registrations → user_profiles (attendee)
ALTER TABLE event_registrations
    ADD CONSTRAINT fk_event_registrations_attendee
    FOREIGN KEY (attendee_user_id) REFERENCES user_profiles(id)
    ON DELETE RESTRICT;

-- event_registrations → qr_credentials
ALTER TABLE event_registrations
    ADD CONSTRAINT fk_event_registrations_qr_credential
    FOREIGN KEY (qr_credential_id) REFERENCES qr_credentials(id)
    ON DELETE SET NULL;

-- transaction_logs → events
ALTER TABLE transaction_logs
    ADD CONSTRAINT fk_transaction_logs_event
    FOREIGN KEY (event_id) REFERENCES events(id)
    ON DELETE RESTRICT;

-- transaction_logs → event_registrations
ALTER TABLE transaction_logs
    ADD CONSTRAINT fk_transaction_logs_registration
    FOREIGN KEY (registration_id) REFERENCES event_registrations(id)
    ON DELETE RESTRICT;

-- transaction_logs → qr_credentials
ALTER TABLE transaction_logs
    ADD CONSTRAINT fk_transaction_logs_qr_credential
    FOREIGN KEY (qr_credential_id) REFERENCES qr_credentials(id)
    ON DELETE RESTRICT;

-- transaction_logs → scan_purposes
ALTER TABLE transaction_logs
    ADD CONSTRAINT fk_transaction_logs_scan_purpose
    FOREIGN KEY (scan_purpose_id) REFERENCES scan_purposes(id)
    ON DELETE RESTRICT;

-- transaction_logs → user_profiles (attendee)
ALTER TABLE transaction_logs
    ADD CONSTRAINT fk_transaction_logs_attendee
    FOREIGN KEY (attendee_user_id) REFERENCES user_profiles(id)
    ON DELETE RESTRICT;

-- transaction_logs → user_profiles (staff) — nullable, SET NULL on delete
ALTER TABLE transaction_logs
    ADD CONSTRAINT fk_transaction_logs_staff
    FOREIGN KEY (staff_user_id) REFERENCES user_profiles(id)
    ON DELETE SET NULL;

-- transaction_logs → rewards (nullable)
ALTER TABLE transaction_logs
    ADD CONSTRAINT fk_transaction_logs_reward
    FOREIGN KEY (reward_id) REFERENCES rewards(id)
    ON DELETE SET NULL;

-- qr_credentials → events
ALTER TABLE qr_credentials
    ADD CONSTRAINT fk_qr_credentials_event
    FOREIGN KEY (event_id) REFERENCES events(id)
    ON DELETE RESTRICT;

-- qr_credentials → user_profiles (attendee)
ALTER TABLE qr_credentials
    ADD CONSTRAINT fk_qr_credentials_attendee
    FOREIGN KEY (attendee_user_id) REFERENCES user_profiles(id)
    ON DELETE RESTRICT;

-- qr_credentials → event_registrations
ALTER TABLE qr_credentials
    ADD CONSTRAINT fk_qr_credentials_registration
    FOREIGN KEY (registration_id) REFERENCES event_registrations(id)
    ON DELETE RESTRICT;

-- scan_purposes → events
ALTER TABLE scan_purposes
    ADD CONSTRAINT fk_scan_purposes_event
    FOREIGN KEY (event_id) REFERENCES events(id)
    ON DELETE CASCADE;

-- transaction_rules → events
ALTER TABLE transaction_rules
    ADD CONSTRAINT fk_transaction_rules_event
    FOREIGN KEY (event_id) REFERENCES events(id)
    ON DELETE CASCADE;

-- transaction_rules → scan_purposes
ALTER TABLE transaction_rules
    ADD CONSTRAINT fk_transaction_rules_scan_purpose
    FOREIGN KEY (scan_purpose_id) REFERENCES scan_purposes(id)
    ON DELETE CASCADE;

-- notifications → events
ALTER TABLE notifications
    ADD CONSTRAINT fk_notifications_event
    FOREIGN KEY (event_id) REFERENCES events(id)
    ON DELETE CASCADE;

-- notifications → user_profiles (recipient)
ALTER TABLE notifications
    ADD CONSTRAINT fk_notifications_recipient
    FOREIGN KEY (recipient_user_id) REFERENCES user_profiles(id)
    ON DELETE CASCADE;

-- notifications → transaction_logs (nullable)
ALTER TABLE notifications
    ADD CONSTRAINT fk_notifications_transaction
    FOREIGN KEY (related_transaction_id) REFERENCES transaction_logs(id)
    ON DELETE SET NULL;

-- notifications → reward_redemptions (nullable)
ALTER TABLE notifications
    ADD CONSTRAINT fk_notifications_redemption
    FOREIGN KEY (related_reward_redemption_id) REFERENCES reward_redemptions(id)
    ON DELETE SET NULL;

-- rewards → events
ALTER TABLE rewards
    ADD CONSTRAINT fk_rewards_event
    FOREIGN KEY (event_id) REFERENCES events(id)
    ON DELETE CASCADE;

-- reward_redemptions → events
ALTER TABLE reward_redemptions
    ADD CONSTRAINT fk_reward_redemptions_event
    FOREIGN KEY (event_id) REFERENCES events(id)
    ON DELETE RESTRICT;

-- reward_redemptions → user_profiles (attendee)
ALTER TABLE reward_redemptions
    ADD CONSTRAINT fk_reward_redemptions_attendee
    FOREIGN KEY (attendee_user_id) REFERENCES user_profiles(id)
    ON DELETE RESTRICT;

-- reward_redemptions → rewards
ALTER TABLE reward_redemptions
    ADD CONSTRAINT fk_reward_redemptions_reward
    FOREIGN KEY (reward_id) REFERENCES rewards(id)
    ON DELETE RESTRICT;

-- reward_redemptions → transaction_logs (nullable)
ALTER TABLE reward_redemptions
    ADD CONSTRAINT fk_reward_redemptions_scan_log
    FOREIGN KEY (redemption_scan_log_id) REFERENCES transaction_logs(id)
    ON DELETE SET NULL;

-- reward_redemptions → user_profiles (staff, nullable)
ALTER TABLE reward_redemptions
    ADD CONSTRAINT fk_reward_redemptions_staff
    FOREIGN KEY (staff_user_id) REFERENCES user_profiles(id)
    ON DELETE SET NULL;

-- point_transactions → events
ALTER TABLE point_transactions
    ADD CONSTRAINT fk_point_transactions_event
    FOREIGN KEY (event_id) REFERENCES events(id)
    ON DELETE CASCADE;

-- point_transactions → user_profiles (attendee)
ALTER TABLE point_transactions
    ADD CONSTRAINT fk_point_transactions_attendee
    FOREIGN KEY (attendee_user_id) REFERENCES user_profiles(id)
    ON DELETE RESTRICT;

-- point_transactions → transaction_logs (source)
ALTER TABLE point_transactions
    ADD CONSTRAINT fk_point_transactions_source
    FOREIGN KEY (source_transaction_id) REFERENCES transaction_logs(id)
    ON DELETE RESTRICT;

-- attendee_point_balances → events
ALTER TABLE attendee_point_balances
    ADD CONSTRAINT fk_attendee_point_balances_event
    FOREIGN KEY (event_id) REFERENCES events(id)
    ON DELETE CASCADE;

-- attendee_point_balances → user_profiles (attendee)
ALTER TABLE attendee_point_balances
    ADD CONSTRAINT fk_attendee_point_balances_attendee
    FOREIGN KEY (attendee_user_id) REFERENCES user_profiles(id)
    ON DELETE RESTRICT;

-- audit_logs → events (nullable)
ALTER TABLE audit_logs
    ADD CONSTRAINT fk_audit_logs_event
    FOREIGN KEY (event_id) REFERENCES events(id)
    ON DELETE SET NULL;

-- audit_logs → user_profiles (performed_by, nullable)
ALTER TABLE audit_logs
    ADD CONSTRAINT fk_audit_logs_performed_by
    FOREIGN KEY (performed_by_user_id) REFERENCES user_profiles(id)
    ON DELETE RESTRICT;

-- audit_logs → user_profiles (target, nullable)
ALTER TABLE audit_logs
    ADD CONSTRAINT fk_audit_logs_target_user
    FOREIGN KEY (target_user_id) REFERENCES user_profiles(id)
    ON DELETE SET NULL;

-- id_print_logs → events
ALTER TABLE id_print_logs
    ADD CONSTRAINT fk_id_print_logs_event
    FOREIGN KEY (event_id) REFERENCES events(id)
    ON DELETE RESTRICT;

-- id_print_logs → user_profiles (attendee)
ALTER TABLE id_print_logs
    ADD CONSTRAINT fk_id_print_logs_attendee
    FOREIGN KEY (attendee_user_id) REFERENCES user_profiles(id)
    ON DELETE RESTRICT;

-- id_print_logs → event_registrations
ALTER TABLE id_print_logs
    ADD CONSTRAINT fk_id_print_logs_registration
    FOREIGN KEY (registration_id) REFERENCES event_registrations(id)
    ON DELETE RESTRICT;

-- id_print_logs → qr_credentials
ALTER TABLE id_print_logs
    ADD CONSTRAINT fk_id_print_logs_qr_credential
    FOREIGN KEY (qr_credential_id) REFERENCES qr_credentials(id)
    ON DELETE RESTRICT;

-- id_print_logs → id_templates
ALTER TABLE id_print_logs
    ADD CONSTRAINT fk_id_print_logs_template
    FOREIGN KEY (template_id) REFERENCES id_templates(id)
    ON DELETE RESTRICT;

-- email_delivery_logs → event_registrations
ALTER TABLE email_delivery_logs
    ADD CONSTRAINT fk_email_delivery_logs_registration
    FOREIGN KEY (registration_id) REFERENCES event_registrations(id)
    ON DELETE RESTRICT;

-- email_delivery_logs → qr_credentials
ALTER TABLE email_delivery_logs
    ADD CONSTRAINT fk_email_delivery_logs_qr_credential
    FOREIGN KEY (qr_credential_id) REFERENCES qr_credentials(id)
    ON DELETE RESTRICT;

-- event_staff_assignments → events
ALTER TABLE event_staff_assignments
    ADD CONSTRAINT fk_event_staff_assignments_event
    FOREIGN KEY (event_id) REFERENCES events(id)
    ON DELETE CASCADE;

-- event_staff_assignments → user_profiles (staff)
ALTER TABLE event_staff_assignments
    ADD CONSTRAINT fk_event_staff_assignments_staff
    FOREIGN KEY (staff_user_id) REFERENCES user_profiles(id)
    ON DELETE RESTRICT;

-- event_staff_assignments → user_profiles (added_by, nullable)
ALTER TABLE event_staff_assignments
    ADD CONSTRAINT fk_event_staff_assignments_added_by
    FOREIGN KEY (added_by_user_id) REFERENCES user_profiles(id)
    ON DELETE SET NULL;

-- event_requests → events (nullable)
ALTER TABLE event_requests
    ADD CONSTRAINT fk_event_requests_event
    FOREIGN KEY (event_id) REFERENCES events(id)
    ON DELETE SET NULL;

-- event_requests → user_profiles (requester)
ALTER TABLE event_requests
    ADD CONSTRAINT fk_event_requests_requester
    FOREIGN KEY (requester_user_id) REFERENCES user_profiles(id)
    ON DELETE RESTRICT;

-- event_requests → user_profiles (reviewed_by, nullable)
ALTER TABLE event_requests
    ADD CONSTRAINT fk_event_requests_reviewed_by
    FOREIGN KEY (reviewed_by_user_id) REFERENCES user_profiles(id)
    ON DELETE SET NULL;

-- id_templates → events
ALTER TABLE id_templates
    ADD CONSTRAINT fk_id_templates_event
    FOREIGN KEY (event_id) REFERENCES events(id)
    ON DELETE CASCADE;

-- id_templates → user_profiles (created_by, nullable)
ALTER TABLE id_templates
    ADD CONSTRAINT fk_id_templates_created_by
    FOREIGN KEY (created_by_user_id) REFERENCES user_profiles(id)
    ON DELETE SET NULL;

-- password_reset_tokens → user_profiles
ALTER TABLE password_reset_tokens
    ADD CONSTRAINT fk_password_reset_tokens_user
    FOREIGN KEY (user_id) REFERENCES user_profiles(id)
    ON DELETE CASCADE;

-- stored_files → user_profiles (owner, nullable)
ALTER TABLE stored_files
    ADD CONSTRAINT fk_stored_files_owner
    FOREIGN KEY (owner_id) REFERENCES user_profiles(id)
    ON DELETE SET NULL;

-- event_activities → events
ALTER TABLE event_activities
    ADD CONSTRAINT fk_event_activities_event
    FOREIGN KEY (event_id) REFERENCES events(id)
    ON DELETE CASCADE;

-- event_benefits → events
ALTER TABLE event_benefits
    ADD CONSTRAINT fk_event_benefits_event
    FOREIGN KEY (event_id) REFERENCES events(id)
    ON DELETE CASCADE;

COMMIT;
