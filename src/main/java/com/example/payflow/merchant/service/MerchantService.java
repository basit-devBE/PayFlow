package com.example.payflow.merchant.service;

import com.example.payflow.merchant.MerchantAlreadyExistsException;
import com.example.payflow.merchant.api.request.RegisterMerchantRequest;
import com.example.payflow.merchant.api.response.RegisterMerchantResponse;
import com.example.payflow.merchant.domain.Merchant;
import com.example.payflow.merchant.domain.MerchantApiKey;
import com.example.payflow.merchant.infra.MerchantApiKeyRepository;
import com.example.payflow.merchant.infra.MerchantRepository;
import com.example.payflow.payments.infra.PaymentRepository;
import com.example.payflow.security.ApiKeyEncryption;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MerchantService {

    private final MerchantRepository merchantRepository;
    private final MerchantApiKeyRepository apiKeyRepository;
    private final ApiKeyEncryption encryption;

    public RegisterMerchantResponse register(RegisterMerchantRequest request) {
        if (merchantRepository.findByEmail(request.email()).isPresent()) {
            throw new MerchantAlreadyExistsException(request.email());
        }

        var merchant = Merchant.create(request.name(), request.email());
        merchantRepository.save(merchant);

        var rawKey = issueApiKey(merchant.getId());
        return new RegisterMerchantResponse(merchant.getId(), merchant.getEmail(), rawKey);
    }

    public String rotateApiKey(UUID merchantId) {
        apiKeyRepository.findByMerchantIdAndActiveTrue(merchantId)
                .ifPresent(MerchantApiKey::revoke);
        return issueApiKey(merchantId);
    }

    private String issueApiKey(UUID merchantId) {
        var rawKey = generateRawKey();
        var encryptedKey = encryption.encrypt(rawKey);
        apiKeyRepository.save(MerchantApiKey.create(merchantId, encryptedKey));
        return rawKey;
    }

    private String generateRawKey() {
        var bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
