package com.example.payflow.merchant.api.response;

import java.util.UUID;

public record RegisterMerchantResponse(
        UUID merchantId,
        String email,
        String apiKey
) {}
