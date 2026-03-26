CREATE TABLE payments.payment
(
    id                  UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    correlation_id      UUID         NOT NULL,
    idempotency_key     VARCHAR(128) NOT NULL,
    merchant_id         UUID         NOT NULL,
    payee_account_id    UUID         NOT NULL,
    amount              DECIMAL(18, 4) NOT NULL,
    currency            VARCHAR(3)   NOT NULL,
    status              VARCHAR(20)  NOT NULL,
    payment_method_type VARCHAR(10),
    payment_method_token VARCHAR(255),
    version             BIGINT       NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ  NOT NULL,
    updated_at          TIMESTAMPTZ  NOT NULL,

    CONSTRAINT uq_payment_idempotency_key UNIQUE (idempotency_key),
    CONSTRAINT chk_payment_status CHECK (status IN ('PENDING', 'AUTHORISED', 'DECLINED')),
    CONSTRAINT chk_payment_currency CHECK (char_length(currency) = 3),
    CONSTRAINT chk_payment_amount CHECK (amount > 0)
);
