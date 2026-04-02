package com.example.payflow.shared.events;

import java.math.BigDecimal;
import java.util.UUID;

public class PaymentTransactionInitiated extends DomainEvent {

    private final UUID paymentId;
    private final UUID merchantId;
    private final String merchantEmail;
    private final BigDecimal amount;
    private final String currency;

    public PaymentTransactionInitiated(UUID paymentId, UUID merchantId, String merchantEmail, String correlationId,
                                       BigDecimal amount, String currency) {
        super(correlationId.toString());
        this.paymentId = paymentId;
        this.merchantId = merchantId;
        this.merchantEmail = merchantEmail;
        this.amount = amount;
        this.currency = currency;
    }

    public UUID getPaymentId() { return paymentId; }
    public UUID getMerchantId() { return merchantId; }
    public String getMerchantEmail() { return merchantEmail; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
}
