package com.example.payflow.fraud.service;

import com.example.payflow.fraud.domain.FraudDecision;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class FraudRuleEngineTest {

    private final FraudRuleEngine engine = new FraudRuleEngine();

    @Test
    void shouldApproveWhenAmountIsUnderThreshold() {
        var result = engine.evaluate(new BigDecimal("9999.99"));
        assertThat(result.decision()).isEqualTo(FraudDecision.APPROVE);
        assertThat(result.score()).isEqualTo(100);
    }

    @Test
    void shouldDeclineWhenAmountIsAboveThreshold() {
        var result = engine.evaluate(new BigDecimal("10000.01"));
        assertThat(result.decision()).isEqualTo(FraudDecision.DECLINE);
        assertThat(result.score()).isEqualTo(0);
    }
}
