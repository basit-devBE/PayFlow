ALTER TABLE merchant.api_keys
    ADD COLUMN expires_at TIMESTAMPTZ;

UPDATE merchant.api_keys
SET expires_at = created_at + INTERVAL '90 days';

ALTER TABLE merchant.api_keys
    ALTER COLUMN expires_at SET NOT NULL;

CREATE INDEX idx_api_keys_expires_at ON merchant.api_keys (expires_at);
