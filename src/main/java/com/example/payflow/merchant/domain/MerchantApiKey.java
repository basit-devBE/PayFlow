package com.example.payflow.merchant.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = "merchant", name = "api_keys")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MerchantApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID merchantId;

    @Column(nullable = false, length = 512)
    private String encryptedKey;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column
    private Instant lastUsedAt;

    private static final long KEY_TTL_DAYS = 90;

    public static MerchantApiKey create(UUID merchantId, String encryptedKey) {
        var apiKey = new MerchantApiKey();
        apiKey.merchantId = merchantId;
        apiKey.encryptedKey = encryptedKey;
        apiKey.active = true;
        apiKey.createdAt = Instant.now();
        apiKey.expiresAt = Instant.now().plus(KEY_TTL_DAYS, java.time.temporal.ChronoUnit.DAYS);
        return apiKey;
    }

    public void recordUsage() {
        this.lastUsedAt = Instant.now();
    }

    public void revoke() {
        this.active = false;
    }
}
