package com.example.payflow.ledger.service;

import com.example.payflow.shared.events.PaymentTransactionAuthorized;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthorizedPaymentListener {

    private final LedgerService ledgerService;

    @ApplicationModuleListener
    public void on(PaymentTransactionAuthorized event) {
        log.info("Posting ledger entry for authorised payment {}", event.getPaymentId());
        ledgerService.postEntry(
                UUID.fromString(event.getCorrelationId()),
                event.getPaymentId(),
                event.getMerchantId(),
                event.getPayeeAccountId(),
                event.getAmount(),
                event.getCurrency()
        );
    }
}
