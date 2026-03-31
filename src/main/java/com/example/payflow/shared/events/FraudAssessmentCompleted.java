package com.example.payflow.shared.events;

import lombok.Getter;

import java.util.UUID;

@Getter
public class FraudAssessmentCompleted extends DomainEvent {

    private final UUID transactionId;
    private final Integer score;
    private final String decision;

    public FraudAssessmentCompleted(String correlationId, UUID transactionId, Integer score, String decision) {
        super(correlationId);
        this.transactionId = transactionId;
        this.score = score;
        this.decision = decision;
    }
}
