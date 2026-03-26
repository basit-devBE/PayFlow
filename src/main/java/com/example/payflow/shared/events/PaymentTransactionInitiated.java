package com.example.payflow.shared.events;

import java.math.BigDecimal;
import java.util.UUID;

public class PaymentTransactionInitiated extends DomainEvent {

    private final UUID paymentId;
    private final UUID merchantId;
    private final BigDecimal amount;
    private final String currency;

    public PaymentTransactionInitiated(UUID paymentId, UUID merchantId, String correlationId,
                                       BigDecimal amount, String currency) {
        super(correlationId.toString());
        this.paymentId = paymentId;
        this.merchantId = merchantId;
        this.amount = amount;
        this.currency = currency;
    }

    public UUID getPaymentId() { return paymentId; }
    public UUID getMerchantId() { return merchantId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
}
