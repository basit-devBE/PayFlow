package com.example.payflow.shared.events;

import lombok.Getter;

import java.util.UUID;

@Getter
public class PaymentTransactionDeclined extends DomainEvent {

    private final UUID paymentId;
    private final UUID merchantId;
    private final String merchantEmail;
    private final String reason;

    public PaymentTransactionDeclined(String correlationId, UUID paymentId, UUID merchantId, String merchantEmail, String reason) {
        super(correlationId);
        this.paymentId = paymentId;
        this.merchantId = merchantId;
        this.merchantEmail = merchantEmail;
        this.reason = reason;
    }
}
