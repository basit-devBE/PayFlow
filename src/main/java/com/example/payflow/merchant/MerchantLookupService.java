package com.example.payflow.merchant;

import com.example.payflow.merchant.infra.MerchantApiKeyRepository;
import com.example.payflow.merchant.infra.MerchantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MerchantLookupService {

    private final MerchantApiKeyRepository apiKeyRepository;
    private final MerchantRepository merchantRepository;

    @Transactional
    public Optional<MerchantIdentity> findByApiKeyHash(String keyHash) {
        return apiKeyRepository.findByKeyHashAndActiveTrue(keyHash)
                .flatMap(apiKey -> {
                    apiKey.recordUsage();
                    return merchantRepository.findById(apiKey.getMerchantId())
                            .map(merchant -> new MerchantIdentity(merchant.getId(), merchant.getEmail()));
                });
    }

    public record MerchantIdentity(java.util.UUID merchantId, String email) {}
}
