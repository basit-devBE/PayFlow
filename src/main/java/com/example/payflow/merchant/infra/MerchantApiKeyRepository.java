package com.example.payflow.merchant.infra;

import com.example.payflow.merchant.domain.MerchantApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MerchantApiKeyRepository extends JpaRepository<MerchantApiKey, UUID> {
    Optional<MerchantApiKey> findByMerchantIdAndActiveTrueAndExpiresAtAfter(UUID merchantId, Instant now);
    Optional<MerchantApiKey> findByMerchantIdAndActiveTrue(UUID merchantId);
    void deleteByActiveFalseOrExpiresAtBefore(Instant now);
}
