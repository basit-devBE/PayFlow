package com.example.payflow.payments.api.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record SubmitPaymentRequest(
        @NotNull UUID payeeAccountId,
        @NotBlank @Size(max = 128) String idempotencyKey,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotBlank @Size(min = 3, max = 3) String currency,
        @NotNull @Valid PaymentMethodRequest paymentMethod
) {}
