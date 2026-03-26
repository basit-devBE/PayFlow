CREATE SCHEMA IF NOT EXISTS payments;
CREATE SCHEMA IF NOT EXISTS merchant;
CREATE SCHEMA IF NOT EXISTS fraud;
CREATE SCHEMA IF NOT EXISTS ledger;
CREATE SCHEMA IF NOT EXISTS notifications;
CREATE SCHEMA IF NOT EXISTS audit;

CREATE TABLE IF NOT EXISTS payments.payment
(
    id                   UUID          NOT NULL PRIMARY KEY,
    correlation_id       UUID          NOT NULL,
    idempotency_key      VARCHAR(128)  NOT NULL UNIQUE,
    merchant_id          UUID          NOT NULL,
    payee_account_id     UUID          NOT NULL,
    amount               DECIMAL(18,4) NOT NULL,
    currency             VARCHAR(3)    NOT NULL,
    status               VARCHAR(20)   NOT NULL,
    payment_method_type  VARCHAR(10),
    payment_method_token VARCHAR(255),
    version              BIGINT        NOT NULL DEFAULT 0,
    created_at           TIMESTAMP     NOT NULL,
    updated_at           TIMESTAMP     NOT NULL
);
