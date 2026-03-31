package com.example.payflow.shared.events;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class PaymentTransactionAuthorized extends DomainEvent {

    private final UUID paymentId;
    private final UUID merchantId;
    private final UUID payeeAccountId;
    private final BigDecimal amount;
    private final String currency;

    public PaymentTransactionAuthorized(String correlationId, UUID paymentId, UUID merchantId,
                                       UUID payeeAccountId, BigDecimal amount, String currency) {
        super(correlationId);
        this.paymentId = paymentId;
        this.merchantId = merchantId;
        this.payeeAccountId = payeeAccountId;
        this.amount = amount;
        this.currency = currency;
    }
}
