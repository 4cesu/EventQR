CREATE TABLE IF NOT EXISTS email_delivery_logs (
    id UUID PRIMARY KEY,
    recipient_email VARCHAR(320) NOT NULL,
    registration_id UUID NOT NULL,
    qr_credential_id UUID NOT NULL,
    status VARCHAR(30) NOT NULL,
    attempted_at TIMESTAMP WITH TIME ZONE NOT NULL,
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_email_delivery_logs_registration_id
    ON email_delivery_logs (registration_id, attempted_at DESC);

CREATE INDEX IF NOT EXISTS idx_email_delivery_logs_qr_credential_id
    ON email_delivery_logs (qr_credential_id, attempted_at DESC);
