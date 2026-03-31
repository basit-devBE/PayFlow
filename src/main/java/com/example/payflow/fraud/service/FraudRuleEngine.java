package com.example.payflow.fraud.service;

import com.example.payflow.fraud.domain.FraudDecision;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class FraudRuleEngine {

    private static final BigDecimal THRESHOLD = new BigDecimal("10000.00");

    public AssessmentResult evaluate(BigDecimal amount) {
        if (amount.compareTo(THRESHOLD) > 0) {
            return new AssessmentResult(0, FraudDecision.DECLINE);
        }
        return new AssessmentResult(100, FraudDecision.APPROVE);
    }

    public record AssessmentResult(Integer score, FraudDecision decision) {}
}
