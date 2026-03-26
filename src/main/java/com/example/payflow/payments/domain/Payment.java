package com.example.payflow.payments.domain;

import com.example.payflow.shared.PaymentStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = "payments", name = "payment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID correlationId;

    @Column(nullable = false, unique = true, length = 128)
    private String idempotencyKey;

    @Column(nullable = false)
    private UUID merchantId;

    @Column(nullable = false)
    private UUID payeeAccountId;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Embedded
    private PaymentMethod paymentMethod;

    @Version
    private Long version;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    public static Payment create(UUID correlationId, String idempotencyKey, UUID merchantId,
                                 UUID payeeAccountId, BigDecimal amount, String currency,
                                 PaymentMethod paymentMethod) {
        var payment = new Payment();
        payment.correlationId = correlationId;
        payment.idempotencyKey = idempotencyKey;
        payment.merchantId = merchantId;
        payment.payeeAccountId = payeeAccountId;
        payment.amount = amount;
        payment.currency = currency;
        payment.paymentMethod = paymentMethod;
        payment.status = PaymentStatus.PENDING;
        payment.createdAt = Instant.now();
        payment.updatedAt = Instant.now();
        return payment;
    }

    public void authorise() {
        this.status = PaymentStatus.AUTHORISED;
        this.updatedAt = Instant.now();
    }

    public void decline() {
        this.status = PaymentStatus.DECLINED;
        this.updatedAt = Instant.now();
    }
}
