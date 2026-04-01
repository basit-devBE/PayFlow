ALTER TABLE audit.event_log
    ADD COLUMN IF NOT EXISTS merchant_id UUID,
    ADD COLUMN IF NOT EXISTS payment_id UUID;

CREATE INDEX IF NOT EXISTS idx_audit_event_log_merchant_id
    ON audit.event_log(merchant_id);

CREATE INDEX IF NOT EXISTS idx_audit_event_log_payment_id
    ON audit.event_log(payment_id);
