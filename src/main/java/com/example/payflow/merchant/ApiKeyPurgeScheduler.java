package com.example.payflow.merchant;

import com.example.payflow.merchant.infra.MerchantApiKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
class ApiKeyPurgeScheduler {

    private final MerchantApiKeyRepository apiKeyRepository;

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void purgeExpiredAndRevokedKeys() {
        log.info("Purging expired and revoked API keys");
        apiKeyRepository.deleteByActiveFalseOrExpiresAtBefore(Instant.now());
        log.info("API key purge complete");
    }
}
