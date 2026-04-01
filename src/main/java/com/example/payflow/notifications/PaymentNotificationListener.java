package com.example.payflow.notifications;

import com.example.payflow.merchant.service.MerchantLookupService;
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

    private final MerchantLookupService merchantLookupService;
    private final EmailNotificationService emailNotificationService;

    @ApplicationModuleListener
    public void on(PaymentTransactionAuthorized event) {
        merchantLookupService.findEmailByMerchantId(event.getMerchantId())
                .ifPresentOrElse(
                        email -> emailNotificationService.sendPaymentAuthorized(
                                email,
                                event.getPaymentId(),
                                event.getAmount(),
                                event.getCurrency()
                        ),
                        () -> log.warn("No merchant email found for authorised payment {}", event.getPaymentId())
                );
    }

    @ApplicationModuleListener
    public void on(PaymentTransactionDeclined event) {
        merchantLookupService.findEmailByMerchantId(event.getMerchantId())
                .ifPresentOrElse(
                        email -> emailNotificationService.sendPaymentDeclined(
                                email,
                                event.getPaymentId(),
                                event.getReason()
                        ),
                        () -> log.warn("No merchant email found for declined payment {}", event.getPaymentId())
                );
    }
}
