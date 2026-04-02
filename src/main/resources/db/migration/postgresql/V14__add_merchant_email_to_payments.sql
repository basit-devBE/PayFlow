ALTER TABLE payments.payment
    ADD COLUMN merchant_email VARCHAR(255);

UPDATE payments.payment payment
SET merchant_email = merchant.email
FROM merchant.merchants merchant
WHERE merchant.id = payment.merchant_id
  AND payment.merchant_email IS NULL;

ALTER TABLE payments.payment
    ALTER COLUMN merchant_email SET NOT NULL;
