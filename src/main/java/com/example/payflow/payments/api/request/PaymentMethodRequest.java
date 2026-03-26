package com.example.payflow.payments.api.request;

import com.example.payflow.payments.domain.PaymentMethodType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentMethodRequest(
        @NotNull PaymentMethodType type,
        @NotBlank String token
) {}
