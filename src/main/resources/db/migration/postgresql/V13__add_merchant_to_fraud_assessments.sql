ALTER TABLE fraud.fraud_assessments
    ADD COLUMN IF NOT EXISTS merchant_id UUID;

CREATE INDEX IF NOT EXISTS idx_fraud_assessments_merchant_transaction
    ON fraud.fraud_assessments(merchant_id, transaction_id);
