package com.example.payflow.payments.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentMethod {

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method_type", length = 10)
    private PaymentMethodType type;

    @Column(name = "payment_method_token", length = 255)
    private String token;

    public static PaymentMethod of(PaymentMethodType type, String token) {
        var method = new PaymentMethod();
        method.type = type;
        method.token = token;
        return method;
    }
}
