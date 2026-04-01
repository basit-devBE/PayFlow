package com.example.payflow.fraud.api.response;

import java.time.Instant;
import java.util.UUID;

public record FraudAssessmentResponse(
        UUID id,
        UUID transactionId,
        Integer score,
        String decision,
        Instant assessedAt
) {}
