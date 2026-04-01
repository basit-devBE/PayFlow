package com.example.payflow.audit.service;

import com.example.payflow.shared.events.FraudAssessmentCompleted;
import com.example.payflow.shared.events.LedgerEntryPosted;
import com.example.payflow.shared.events.PaymentTransactionAuthorized;
import com.example.payflow.shared.events.PaymentTransactionDeclined;
import com.example.payflow.shared.events.PaymentTransactionInitiated;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuditEventListener {

    private final AuditService auditService;

    @ApplicationModuleListener
    public void on(PaymentTransactionInitiated event) {
        auditService.record(event);
    }

    @ApplicationModuleListener
    public void on(FraudAssessmentCompleted event) {
        auditService.record(event);
    }

    @ApplicationModuleListener
    public void on(PaymentTransactionAuthorized event) {
        auditService.record(event);
    }

    @ApplicationModuleListener
    public void on(PaymentTransactionDeclined event) {
        auditService.record(event);
    }

    @ApplicationModuleListener
    public void on(LedgerEntryPosted event) {
        auditService.record(event);
    }
}
