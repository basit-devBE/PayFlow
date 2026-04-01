package com.example.payflow.merchant.service;

import com.example.payflow.merchant.infra.MerchantApiKeyRepository;
import com.example.payflow.merchant.infra.MerchantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MerchantLookupService {

    private final MerchantApiKeyRepository apiKeyRepository;
    private final MerchantRepository merchantRepository;
    private final ApiKeyEncryption encryption;

    @Transactional
    public Optional<MerchantIdentity> findActiveKeyByMerchantId(UUID merchantId) {
        return apiKeyRepository.findByMerchantIdAndActiveTrueAndExpiresAtAfter(merchantId, Instant.now())
                .flatMap(apiKey -> {
                    apiKey.recordUsage();
                    var rawKey = encryption.decrypt(apiKey.getEncryptedKey());
                    return merchantRepository.findById(apiKey.getMerchantId())
                            .map(merchant -> new MerchantIdentity(merchant.getId(), merchant.getEmail(), rawKey));
                });
    }

    @Transactional(readOnly = true)
    public Optional<String> findEmailByMerchantId(UUID merchantId) {
        return merchantRepository.findById(merchantId).map(merchant -> merchant.getEmail());
    }

    public record MerchantIdentity(UUID merchantId, String email, String rawKey) {}
}
