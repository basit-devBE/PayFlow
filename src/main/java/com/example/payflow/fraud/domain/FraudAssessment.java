package com.example.payflow.fraud.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = "fraud", name = "fraud_assessments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FraudAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID transactionId;

    @Column(nullable = false)
    private Integer score;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private FraudDecision decision;

    @Column(nullable = false, updatable = false)
    private Instant assessedAt;

    public static FraudAssessment create(UUID transactionId, Integer score, FraudDecision decision) {
        var assessment = new FraudAssessment();
        assessment.transactionId = transactionId;
        assessment.score = score;
        assessment.decision = decision;
        assessment.assessedAt = Instant.now();
        return assessment;
    }
}
