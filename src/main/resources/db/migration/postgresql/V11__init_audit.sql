CREATE SCHEMA IF NOT EXISTS audit;

CREATE TABLE IF NOT EXISTS audit.event_log (
    id UUID PRIMARY KEY,
    correlation_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload JSONB NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_audit_event_log_correlation_id
    ON audit.event_log(correlation_id);

CREATE INDEX idx_audit_event_log_occurred_at
    ON audit.event_log(occurred_at);
