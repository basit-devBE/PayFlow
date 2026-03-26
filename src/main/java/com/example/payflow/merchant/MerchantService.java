package com.example.payflow.merchant;

import com.example.payflow.merchant.api.request.RegisterMerchantRequest;
import com.example.payflow.merchant.api.response.RegisterMerchantResponse;
import com.example.payflow.merchant.domain.Merchant;
import com.example.payflow.merchant.domain.MerchantApiKey;
import com.example.payflow.merchant.infra.MerchantApiKeyRepository;
import com.example.payflow.merchant.infra.MerchantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
@Transactional
public class MerchantService {

    private final MerchantRepository merchantRepository;
    private final MerchantApiKeyRepository apiKeyRepository;

    public RegisterMerchantResponse register(RegisterMerchantRequest request) {
        if (merchantRepository.findByEmail(request.email()).isPresent()) {
            throw new MerchantAlreadyExistsException(request.email());
        }

        var merchant = Merchant.create(request.name(), request.email());
        merchantRepository.save(merchant);

        var rawKey = issueApiKey(merchant.getId());
        return new RegisterMerchantResponse(merchant.getId(), merchant.getEmail(), rawKey);
    }

    public String rotateApiKey(java.util.UUID merchantId) {
        apiKeyRepository.findByMerchantIdAndActiveTrue(merchantId)
                .ifPresent(MerchantApiKey::revoke);
        return issueApiKey(merchantId);
    }

    private String issueApiKey(java.util.UUID merchantId) {
        var rawKey = generateRawKey();
        apiKeyRepository.save(MerchantApiKey.create(merchantId, hashKey(rawKey)));
        return rawKey;
    }

    private String generateRawKey() {
        var bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashKey(String rawKey) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            var hashBytes = digest.digest(rawKey.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
