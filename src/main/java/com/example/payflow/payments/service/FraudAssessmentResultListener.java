package com.example.payflow.payments.service;

import com.example.payflow.shared.events.FraudAssessmentCompleted;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FraudAssessmentResultListener {

    private final PaymentService paymentService;

    @ApplicationModuleListener
    public void on(FraudAssessmentCompleted event) {
        log.info("Received fraud assessment for transaction {}: {}", event.getTransactionId(), event.getDecision());
        paymentService.processFraudAssessment(
                event.getTransactionId(),
                event.getDecision(),
                event.getCorrelationId()
        );
    }
}
