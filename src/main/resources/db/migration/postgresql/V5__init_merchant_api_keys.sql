CREATE TABLE merchant.api_keys
(
    id           UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    merchant_id  UUID        NOT NULL,
    key_hash     VARCHAR(64) NOT NULL,
    active       BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ NOT NULL,
    last_used_at TIMESTAMPTZ,

    CONSTRAINT uq_api_key_hash UNIQUE (key_hash),
    CONSTRAINT fk_api_key_merchant FOREIGN KEY (merchant_id) REFERENCES merchant.merchants (id)
);
