package com.example.payflow.merchant.api.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterMerchantRequest(
        @NotBlank String name,
        @NotBlank @Email String email
) {}
