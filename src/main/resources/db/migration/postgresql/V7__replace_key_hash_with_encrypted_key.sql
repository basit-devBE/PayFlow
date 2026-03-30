ALTER TABLE merchant.api_keys
    RENAME COLUMN key_hash TO encrypted_key;

ALTER TABLE merchant.api_keys
    ALTER COLUMN encrypted_key TYPE VARCHAR(512);

DROP INDEX IF EXISTS merchant.uq_api_key_hash;
