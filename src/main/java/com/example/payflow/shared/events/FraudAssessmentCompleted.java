package com.example.payflow.shared.events;

import lombok.Getter;

import java.util.UUID;

@Getter
public class FraudAssessmentCompleted extends DomainEvent {

    private final UUID transactionId;
    private final UUID merchantId;
    private final Integer score;
    private final String decision;

    public FraudAssessmentCompleted(String correlationId, UUID transactionId, UUID merchantId, Integer score, String decision) {
        super(correlationId);
        this.transactionId = transactionId;
        this.merchantId = merchantId;
        this.score = score;
        this.decision = decision;
    }
}
