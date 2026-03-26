package com.example.payflow.merchant.domain;

import com.example.payflow.shared.MerchantStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = "merchant", name = "merchants")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Merchant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MerchantStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public static Merchant create(String name, String email) {
        var merchant = new Merchant();
        merchant.name = name;
        merchant.email = email;
        merchant.status = MerchantStatus.ACTIVE;
        merchant.createdAt = Instant.now();
        return merchant;
    }

    public void suspend() {
        this.status = MerchantStatus.SUSPENDED;
    }
}
