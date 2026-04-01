package com.example.payflow.shared.events;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class LedgerEntryPosted extends DomainEvent {

    private final UUID journalEntryId;
    private final UUID paymentId;
    private final UUID merchantId;
    private final BigDecimal amount;
    private final String currency;

    public LedgerEntryPosted(String correlationId, UUID journalEntryId, UUID paymentId,
                             UUID merchantId, BigDecimal amount, String currency) {
        super(correlationId);
        this.journalEntryId = journalEntryId;
        this.paymentId = paymentId;
        this.merchantId = merchantId;
        this.amount = amount;
        this.currency = currency;
    }
}
