package com.example.payflow.notifications;

import com.example.payflow.shared.events.PaymentTransactionAuthorized;
import com.example.payflow.shared.events.PaymentTransactionDeclined;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentNotificationListener {

    private final EmailNotificationService emailNotificationService;

    @ApplicationModuleListener
    public void on(PaymentTransactionAuthorized event) {
        if (event.getMerchantEmail() == null || event.getMerchantEmail().isBlank()) {
            log.warn("No merchant email found for authorised payment {}", event.getPaymentId());
            return;
        }

        emailNotificationService.sendPaymentAuthorized(
                event.getMerchantEmail(),
                event.getPaymentId(),
                event.getAmount(),
                event.getCurrency()
        );
    }

    @ApplicationModuleListener
    public void on(PaymentTransactionDeclined event) {
        if (event.getMerchantEmail() == null || event.getMerchantEmail().isBlank()) {
            log.warn("No merchant email found for declined payment {}", event.getPaymentId());
            return;
        }

        emailNotificationService.sendPaymentDeclined(
                event.getMerchantEmail(),
                event.getPaymentId(),
                event.getReason()
        );
    }
}
