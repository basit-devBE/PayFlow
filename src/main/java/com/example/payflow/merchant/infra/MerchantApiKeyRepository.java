package com.example.payflow.merchant.infra;

import com.example.payflow.merchant.domain.MerchantApiKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MerchantApiKeyRepository extends JpaRepository<MerchantApiKey, UUID> {
    Optional<MerchantApiKey> findByKeyHashAndActiveTrue(String keyHash);
}
