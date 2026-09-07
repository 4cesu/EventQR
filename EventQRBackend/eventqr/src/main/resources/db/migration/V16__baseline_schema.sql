-- V16__baseline_schema.sql
-- Bring Hibernate-managed tables under Flyway governance.
-- Uses CREATE TABLE IF NOT EXISTS for idempotency (tables may already exist from ddl-auto=update).
-- Does NOT add foreign keys (migration V18 handles FKs selectively).
-- DO NOT RUN this on a production DB with existing data until you have verified column parity
-- between this DDL and the current live schema (use \d+ table_name).

BEGIN;

-- ============================================================
-- events
-- ============================================================
CREATE TABLE IF NOT EXISTS events (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    title varchar(255) NOT NULL,
    description varchar(2000),
    location varchar(255),
    event_logo_url varchar(255),
    registration_open_at timestamptz,
    registration_close_at timestamptz,
    event_start_at timestamptz,
    event_end_at timestamptz,
    capacity integer NOT NULL DEFAULT 0,
    current_attendee_count integer NOT NULL DEFAULT 0,
    status varchar(50) NOT NULL DEFAULT 'DRAFT',
    rewards_enabled boolean NOT NULL DEFAULT false,
    organizer_user_id uuid,
    approved_by_user_id uuid,
    approved_at timestamptz,
    rejection_reason varchar(2000)
);

-- ============================================================
-- user_profiles
-- ============================================================
CREATE TABLE IF NOT EXISTS user_profiles (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    email varchar(255) NOT NULL,
    full_name varchar(255) NOT NULL,
    phone_number varchar(255),
    avatar_file_id uuid,
    avatar_path varchar(255),
    password_hash varchar(255) NOT NULL,
    role varchar(50) NOT NULL DEFAULT 'ATTENDEE',
    status varchar(50) NOT NULL DEFAULT 'ACTIVE'
);

-- ============================================================
-- event_registrations
-- ============================================================
CREATE TABLE IF NOT EXISTS event_registrations (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    event_id uuid NOT NULL,
    attendee_user_id uuid NOT NULL,
    attendee_email varchar(255) NOT NULL,
    attendee_name varchar(255) NOT NULL,
    status varchar(50) NOT NULL DEFAULT 'REGISTERED',
    registration_number integer,
    qr_credential_id uuid,
    registered_at timestamptz,
    entered_at timestamptz,
    exited_at timestamptz,
    attended_at timestamptz,
    points_earned integer NOT NULL DEFAULT 0,
    checked_in_by_user_id uuid
);

-- ============================================================
-- transaction_logs
-- ============================================================
CREATE TABLE IF NOT EXISTS transaction_logs (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    event_id uuid NOT NULL,
    attendee_user_id uuid NOT NULL,
    registration_id uuid NOT NULL,
    qr_credential_id uuid NOT NULL,
    scan_purpose_id uuid NOT NULL,
    transaction_type varchar(50) NOT NULL,
    transaction_result varchar(50) NOT NULL,
    staff_user_id uuid,
    reward_id uuid,
    scanned_at timestamptz NOT NULL DEFAULT now(),
    reason varchar(2000),
    metadata varchar(4000) NOT NULL DEFAULT '{}',
    points_delta integer NOT NULL DEFAULT 0
);

-- ============================================================
-- qr_credentials
-- ============================================================
CREATE TABLE IF NOT EXISTS qr_credentials (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    qr_value varchar(120) NOT NULL,
    event_id uuid NOT NULL,
    attendee_user_id uuid NOT NULL,
    registration_id uuid NOT NULL,
    display_status varchar(50) NOT NULL DEFAULT 'PENDING',
    delivery_status varchar(50) NOT NULL DEFAULT 'PENDING',
    active boolean NOT NULL DEFAULT true,
    downloaded boolean NOT NULL DEFAULT false
);

-- ============================================================
-- scan_purposes
-- ============================================================
CREATE TABLE IF NOT EXISTS scan_purposes (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    event_id uuid NOT NULL,
    name varchar(255) NOT NULL,
    code varchar(50) NOT NULL,
    active boolean NOT NULL DEFAULT true,
    tracking_only boolean NOT NULL DEFAULT false,
    description varchar(2000)
);

-- ============================================================
-- transaction_rules
-- ============================================================
CREATE TABLE IF NOT EXISTS transaction_rules (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    event_id uuid NOT NULL,
    scan_purpose_id uuid NOT NULL,
    active boolean NOT NULL DEFAULT true,
    allow_duplicate boolean NOT NULL DEFAULT false,
    duplicate_window_minutes integer NOT NULL DEFAULT 0,
    max_uses_per_registration integer NOT NULL DEFAULT 1,
    requires_staff_assignment boolean NOT NULL DEFAULT true,
    points_awarded integer NOT NULL DEFAULT 0
);

-- ============================================================
-- notifications
-- ============================================================
CREATE TABLE IF NOT EXISTS notifications (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    event_id uuid NOT NULL,
    recipient_user_id uuid NOT NULL,
    title varchar(255) NOT NULL,
    message varchar(2000) NOT NULL,
    status varchar(50) NOT NULL DEFAULT 'PENDING',
    notification_type varchar(50) NOT NULL DEFAULT 'GENERAL',
    related_transaction_id uuid,
    related_reward_redemption_id uuid,
    read_at timestamptz
);

-- ============================================================
-- rewards
-- ============================================================
CREATE TABLE IF NOT EXISTS rewards (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    event_id uuid NOT NULL,
    name varchar(255) NOT NULL,
    points_required integer NOT NULL,
    status varchar(50) NOT NULL DEFAULT 'ACTIVE',
    stock_quantity integer,
    allow_duplicate_claims boolean NOT NULL DEFAULT false
);

-- ============================================================
-- reward_redemptions
-- ============================================================
CREATE TABLE IF NOT EXISTS reward_redemptions (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    event_id uuid NOT NULL,
    attendee_user_id uuid NOT NULL,
    reward_id uuid NOT NULL,
    points_spent integer NOT NULL,
    status varchar(50) NOT NULL DEFAULT 'PENDING',
    redeemed_at timestamptz,
    reason varchar(2000),
    redemption_scan_log_id uuid,
    staff_user_id uuid
);

-- ============================================================
-- point_transactions
-- ============================================================
CREATE TABLE IF NOT EXISTS point_transactions (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    event_id uuid NOT NULL,
    attendee_user_id uuid NOT NULL,
    source_transaction_id uuid NOT NULL,
    points_changed integer NOT NULL,
    occurred_at timestamptz NOT NULL,
    reason varchar(2000)
);

-- ============================================================
-- attendee_point_balances
-- ============================================================
CREATE TABLE IF NOT EXISTS attendee_point_balances (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    event_id uuid NOT NULL,
    attendee_user_id uuid NOT NULL,
    points_balance integer NOT NULL DEFAULT 0
);

-- ============================================================
-- audit_logs
-- ============================================================
CREATE TABLE IF NOT EXISTS audit_logs (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    action varchar(255) NOT NULL,
    details text,
    performed_by_user_id uuid NOT NULL,
    performed_by_full_name varchar(255),
    event_id uuid,
    target_user_id uuid
);

-- ============================================================
-- id_print_logs
-- ============================================================
CREATE TABLE IF NOT EXISTS id_print_logs (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    event_id uuid NOT NULL,
    attendee_user_id uuid NOT NULL,
    registration_id uuid NOT NULL,
    qr_credential_id uuid NOT NULL,
    template_id uuid NOT NULL,
    reprint boolean NOT NULL DEFAULT false,
    success boolean NOT NULL DEFAULT false,
    printed_at timestamptz,
    message varchar(2000)
);

-- ============================================================
-- email_delivery_logs
-- ============================================================
CREATE TABLE IF NOT EXISTS email_delivery_logs (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_email varchar(320) NOT NULL,
    registration_id uuid NOT NULL,
    qr_credential_id uuid NOT NULL,
    status varchar(30) NOT NULL,
    attempted_at timestamptz NOT NULL,
    error_message text,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

-- ============================================================
-- event_staff_assignments (already created by V2; IF NOT EXISTS is safe)
-- ============================================================
CREATE TABLE IF NOT EXISTS event_staff_assignments (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    event_id uuid NOT NULL,
    staff_user_id uuid NOT NULL,
    role_label varchar(120) NOT NULL DEFAULT 'Staff',
    staff_role varchar(64) NOT NULL DEFAULT 'STAFF',
    active boolean NOT NULL DEFAULT true,
    can_scan boolean NOT NULL DEFAULT true,
    can_print_id boolean NOT NULL DEFAULT false,
    can_view_logs boolean NOT NULL DEFAULT false,
    can_manage_rewards boolean NOT NULL DEFAULT false,
    permissions varchar(2000),
    added_by_user_id uuid,
    added_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT uq_event_staff_assignment UNIQUE (event_id, staff_user_id),
    CONSTRAINT chk_event_staff_assignments_staff_role CHECK (staff_role = 'STAFF')
);

-- ============================================================
-- event_requests (already created by V3; IF NOT EXISTS is safe)
-- ============================================================
CREATE TABLE IF NOT EXISTS event_requests (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    requester_user_id uuid NOT NULL,
    event_name varchar(255) NOT NULL,
    event_description varchar(3000) NOT NULL,
    event_category varchar(255) NOT NULL,
    target_audience varchar(255),
    capacity integer NOT NULL,
    venue varchar(255) NOT NULL,
    start_date_time timestamptz NOT NULL,
    end_date_time timestamptz NOT NULL,
    registration_start_date_time timestamptz,
    registration_end_date_time timestamptz,
    requester_name varchar(255) NOT NULL,
    contact_email varchar(255) NOT NULL,
    contact_number varchar(255) NOT NULL,
    requested_features jsonb DEFAULT '[]',
    event_logo_url varchar(255),
    additional_notes varchar(3000),
    reason_for_request varchar(3000) NOT NULL,
    status varchar(50) NOT NULL,
    event_id uuid,
    admin_remarks varchar(2000),
    reviewed_by_user_id uuid,
    reviewed_at timestamptz
);

-- ============================================================
-- id_templates (no migration yet; entity exists)
-- ============================================================
CREATE TABLE IF NOT EXISTS id_templates (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    event_id uuid NOT NULL,
    name varchar(255) NOT NULL,
    active boolean NOT NULL DEFAULT true,
    template_json varchar(4000),
    created_by_user_id uuid
);

-- ============================================================
-- password_reset_tokens (no migration yet; entity exists)
-- ============================================================
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL,
    token varchar(255) NOT NULL,
    expires_at timestamptz NOT NULL,
    used boolean NOT NULL DEFAULT false,
    created_at timestamptz NOT NULL,
    CONSTRAINT uq_password_reset_token UNIQUE (token)
);

-- ============================================================
-- stored_files (no migration yet; entity exists)
-- ============================================================
CREATE TABLE IF NOT EXISTS stored_files (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    owner_id uuid,
    purpose varchar(255),
    file_name varchar(255),
    content_type varchar(255),
    size int8 NOT NULL DEFAULT 0,
    stored_at timestamptz NOT NULL,
    content bytea NOT NULL
);

-- ============================================================
-- event_activities (entity exists, no migration yet)
-- ============================================================
CREATE TABLE IF NOT EXISTS event_activities (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    event_id uuid NOT NULL,
    name varchar(255) NOT NULL,
    active boolean NOT NULL DEFAULT true,
    description varchar(2000)
);

-- ============================================================
-- event_benefits (entity exists, no migration yet)
-- ============================================================
CREATE TABLE IF NOT EXISTS event_benefits (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    event_id uuid NOT NULL,
    name varchar(255) NOT NULL,
    active boolean NOT NULL DEFAULT true,
    description varchar(2000)
);

COMMIT;
