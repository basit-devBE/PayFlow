package com.example.payflow.ledger.api.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record JournalEntryResponse(
        UUID id,
        UUID correlationId,
        UUID paymentId,
        String debitAccount,
        String creditAccount,
        BigDecimal amount,
        String currency,
        Instant postedAt
) {}
