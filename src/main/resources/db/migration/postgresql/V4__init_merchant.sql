CREATE SCHEMA IF NOT EXISTS merchant;

CREATE TABLE merchant.merchants
(
    id         UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL,
    status     VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ  NOT NULL,

    CONSTRAINT uq_merchant_email UNIQUE (email),
    CONSTRAINT chk_merchant_status CHECK (status IN ('ACTIVE', 'SUSPENDED'))
);
