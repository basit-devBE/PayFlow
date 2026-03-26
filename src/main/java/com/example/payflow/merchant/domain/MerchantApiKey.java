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

    @Column(nullable = false, unique = true)
    private String keyHash;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column
    private Instant lastUsedAt;

    public static MerchantApiKey create(UUID merchantId, String keyHash) {
        var apiKey = new MerchantApiKey();
        apiKey.merchantId = merchantId;
        apiKey.keyHash = keyHash;
        apiKey.active = true;
        apiKey.createdAt = Instant.now();
        return apiKey;
    }

    public void recordUsage() {
        this.lastUsedAt = Instant.now();
    }

    public void revoke() {
        this.active = false;
    }
}
