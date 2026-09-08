-- V17__indexes_and_guards.sql
-- Performance indexes + anti-duplicate guards + registration-number race fix.
-- Requires V16 to have created all tables.

BEGIN;

-- ============================================================
-- EVENTS INDEXES
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_events_status_start ON events(status, event_start_at);
CREATE INDEX IF NOT EXISTS idx_events_status_end ON events(status, event_end_at);

-- ============================================================
-- EVENT_REGISTRATIONS INDEXES + FUNCTIONAL UNIQUE
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_event_registrations_event_id ON event_registrations(event_id);
CREATE INDEX IF NOT EXISTS idx_event_registrations_attendee_user_id ON event_registrations(attendee_user_id);
CREATE INDEX IF NOT EXISTS idx_event_registrations_qr_credential_id ON event_registrations(qr_credential_id);

-- Functional unique: prevent duplicate email registration per event (case-insensitive).
-- Uses lower() to match the app's existsByEventIdAndAttendeeEmailIgnoreCase check.
CREATE UNIQUE INDEX IF NOT EXISTS uq_event_registrations_event_email_lower
    ON event_registrations(event_id, lower(attendee_email));

-- ============================================================
-- TRANSACTION_LOGS INDEXES
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_transaction_logs_event_scanned ON transaction_logs(event_id, scanned_at DESC);
CREATE INDEX IF NOT EXISTS idx_transaction_logs_event_id ON transaction_logs(event_id);
CREATE INDEX IF NOT EXISTS idx_transaction_logs_reg_purpose_scanned ON transaction_logs(registration_id, scan_purpose_id, scanned_at DESC);
CREATE INDEX IF NOT EXISTS idx_transaction_logs_attendee_user_id ON transaction_logs(attendee_user_id);
CREATE INDEX IF NOT EXISTS idx_transaction_logs_staff_scanned ON transaction_logs(staff_user_id, scanned_at DESC);
CREATE INDEX IF NOT EXISTS idx_transaction_logs_attendee_event_scanned ON transaction_logs(attendee_user_id, event_id, scanned_at DESC);

-- ============================================================
-- PARTIAL UNIQUE: approved scans per registration+purpose
-- SAFE because: TransactionRule.maxUsesPerRegistration defaults to 1 for non-duplicate
-- purposes, and allowDuplicate=false is the default. Only one APPROVED scan per
-- registration+purpose is ever expected. If business ever changes to allow multi-use
-- purposes, replace with SELECT...FOR UPDATE in app layer and DROP this index.
--
-- Guard: on a legacy-drifted DB with duplicate (registration_id, scan_purpose_id)
-- APPROVED groups, this index cannot be created. Skip only if duplicates exist;
-- greenfield DBs with clean data pass the guard and create normally.
DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM (
      SELECT registration_id, scan_purpose_id
      FROM transaction_logs
      WHERE transaction_result = 'APPROVED'
      GROUP BY registration_id, scan_purpose_id
      HAVING count(*) > 1
  ) d) THEN
    CREATE UNIQUE INDEX IF NOT EXISTS uq_txn_logs_approved_reg_purpose
        ON transaction_logs(registration_id, scan_purpose_id)
        WHERE transaction_result = 'APPROVED';
  END IF;
END $$;

-- ============================================================
-- QR_CREDENTIALS INDEXES
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_qr_credentials_event_id ON qr_credentials(event_id);
CREATE INDEX IF NOT EXISTS idx_qr_credentials_attendee_user_id ON qr_credentials(attendee_user_id);

-- ============================================================
-- SCAN_PURPOSES INDEXES
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_scan_purposes_event_code ON scan_purposes(event_id, code);

-- ============================================================
-- TRANSACTION_RULES UNIQUE
-- ============================================================
CREATE UNIQUE INDEX IF NOT EXISTS uq_transaction_rules_event_purpose
    ON transaction_rules(event_id, scan_purpose_id);

-- ============================================================
-- NOTIFICATIONS INDEXES
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_notifications_recipient_status_created ON notifications(recipient_user_id, status, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notifications_event_id ON notifications(event_id);

-- ============================================================
-- REWARDS INDEXES
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_rewards_event_id ON rewards(event_id);

-- ============================================================
-- REWARD_REDEMPTIONS INDEXES + PARTIAL UNIQUE
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_reward_redemptions_event_att_reward ON reward_redemptions(event_id, attendee_user_id, reward_id);
CREATE INDEX IF NOT EXISTS idx_reward_redemptions_att_reward_status ON reward_redemptions(attendee_user_id, reward_id, status);

-- Partial unique: one REDEEMED redemption per user+reward+event.
-- REDEEMED is terminal; PENDING/CANCELLED can be re-requested.
--
-- Guard: on a legacy-drifted DB with duplicate (event_id, attendee_user_id, reward_id)
-- REDEEMED groups, this index cannot be created. Skip only if duplicates exist;
-- greenfield DBs with clean data pass the guard and create normally.
DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM (
      SELECT event_id, attendee_user_id, reward_id
      FROM reward_redemptions
      WHERE status = 'REDEEMED'
      GROUP BY event_id, attendee_user_id, reward_id
      HAVING count(*) > 1
  ) d) THEN
    CREATE UNIQUE INDEX IF NOT EXISTS uq_reward_redemptions_redeemed
        ON reward_redemptions(event_id, attendee_user_id, reward_id)
        WHERE status = 'REDEEMED';
  END IF;
END $$;

-- ============================================================
-- POINT_TRANSACTIONS INDEXES
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_point_transactions_event_id ON point_transactions(event_id);
CREATE INDEX IF NOT EXISTS idx_point_transactions_event_attendee ON point_transactions(event_id, attendee_user_id);

-- ============================================================
-- ATTENDEE_POINT_BALANCES UNIQUE
-- ============================================================
CREATE UNIQUE INDEX IF NOT EXISTS uq_attendee_point_balances_event_user
    ON attendee_point_balances(event_id, attendee_user_id);

-- ============================================================
-- AUDIT_LOGS INDEXES
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_audit_logs_performed_by ON audit_logs(performed_by_user_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_event_id ON audit_logs(event_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at ON audit_logs(created_at DESC);

-- ============================================================
-- ID_PRINT_LOGS INDEXES
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_id_print_logs_event_id ON id_print_logs(event_id);
CREATE INDEX IF NOT EXISTS idx_id_print_logs_registration_id ON id_print_logs(registration_id);

-- ============================================================
-- EMAIL_DELIVERY_LOGS INDEXES
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_email_delivery_logs_registration_id ON email_delivery_logs(registration_id, attempted_at DESC);
CREATE INDEX IF NOT EXISTS idx_email_delivery_logs_qr_credential_id ON email_delivery_logs(qr_credential_id, attempted_at DESC);

-- ============================================================
-- REGISTRATION NUMBER RACE FIX
-- Replace the MAX()+1 trigger (V13) with advisory-lock-based assignment.
-- pg_advisory_xact_lock is transaction-scoped: concurrent inserts to the same event
-- serialize on the lock, guaranteeing sequential number assignment without collisions.
-- The lock key is derived from event_id so different events never block each other.
-- ============================================================
DROP TRIGGER IF EXISTS trg_assign_registration_number ON public.event_registrations;
DROP FUNCTION IF EXISTS public.assign_registration_number();

CREATE OR REPLACE FUNCTION public.assign_registration_number()
RETURNS trigger
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
    lock_key bigint;
BEGIN
    IF NEW.registration_number IS NOT NULL THEN
        RETURN NEW;
    END IF;

    -- Advisory lock per event: hashtext gives a bigint hash of "reg:" || event_id
    lock_key := hashtext('reg:' || NEW.event_id::text);
    PERFORM pg_advisory_xact_lock(lock_key);

    SELECT COALESCE(MAX(registration_number), 0) + 1
    INTO NEW.registration_number
    FROM public.event_registrations
    WHERE event_id = NEW.event_id;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_assign_registration_number
BEFORE INSERT ON public.event_registrations
FOR EACH ROW
EXECUTE FUNCTION public.assign_registration_number();

COMMIT;
