CREATE SCHEMA IF NOT EXISTS fraud;

CREATE TABLE IF NOT EXISTS fraud.fraud_assessments (
    id UUID PRIMARY KEY,
    transaction_id UUID NOT NULL,
    score INTEGER NOT NULL,
    decision VARCHAR(10) NOT NULL,
    assessed_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_fraud_assessments_transaction_id ON fraud.fraud_assessments(transaction_id);
