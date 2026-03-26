package com.example.payflow.payments.api.response;

import com.example.payflow.shared.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID correlationId,
        UUID merchantId,
        UUID payeeAccountId,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        Instant createdAt
) {}
