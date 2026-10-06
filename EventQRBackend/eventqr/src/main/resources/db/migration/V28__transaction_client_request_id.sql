-- V28__transaction_client_request_id.sql
-- Idempotency key for staff scans.
--
-- Recording a scan is a network call that can time out after the server has already
-- logged it; staff then scan again and the attendee is logged twice. The app now sends a
-- client-generated UUID per scan attempt, and a retry with the same id returns the
-- original result instead of creating another row. NULL for older clients and rows.

ALTER TABLE transaction_logs ADD COLUMN IF NOT EXISTS client_request_id uuid;

CREATE UNIQUE INDEX IF NOT EXISTS ux_transaction_logs_client_request_id
    ON transaction_logs (client_request_id)
    WHERE client_request_id IS NOT NULL;
