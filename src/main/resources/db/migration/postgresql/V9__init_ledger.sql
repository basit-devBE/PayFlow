CREATE SCHEMA IF NOT EXISTS ledger;

CREATE TABLE IF NOT EXISTS ledger.journal_entries (
    id UUID PRIMARY KEY,
    correlation_id UUID NOT NULL,
    payment_id UUID NOT NULL UNIQUE,
    debit_account VARCHAR(100) NOT NULL,
    credit_account VARCHAR(100) NOT NULL,
    amount DECIMAL(18, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    posted_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_ledger_journal_entries_correlation_id
    ON ledger.journal_entries(correlation_id);
