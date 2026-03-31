package com.example.payflow.fraud.service;

import com.example.payflow.fraud.domain.FraudAssessment;
import com.example.payflow.fraud.infra.FraudAssessmentRepository;
import com.example.payflow.shared.events.DomainEventPublisher;
import com.example.payflow.shared.events.FraudAssessmentCompleted;
import com.example.payflow.shared.events.PaymentTransactionInitiated;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FraudAssessmentListener {

    private final FraudRuleEngine ruleEngine;
    private final FraudAssessmentRepository repository;
    private final DomainEventPublisher publisher;

    @ApplicationModuleListener
    public void on(PaymentTransactionInitiated event) {
        log.info("Evaluating payment transaction for fraud: {}", event.getPaymentId());
        
        var result = ruleEngine.evaluate(event.getAmount());
        var assessment = FraudAssessment.create(event.getPaymentId(), result.score(), result.decision());
        
        repository.save(assessment);
        
        log.info("Fraud assessment completed for transaction {}: decision={}", event.getPaymentId(), result.decision());
        
        publisher.publish(new FraudAssessmentCompleted(
                event.getCorrelationId(),
                assessment.getTransactionId(),
                assessment.getScore(),
                assessment.getDecision().name()
        ));
    }
}
