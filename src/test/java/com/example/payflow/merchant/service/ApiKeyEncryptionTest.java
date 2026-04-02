package com.example.payflow.merchant.service;

import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class ApiKeyEncryptionTest {

    private static final String SECRET = "change-this-in-production-must-be-32-chars-min";

    private final ApiKeyEncryption encryption = new ApiKeyEncryption(SECRET);

    @Test
    void encrypt_usesVersionedGcmPayload() {
        var ciphertext = encryption.encrypt("plain-api-key");

        assertThat(ciphertext).startsWith("v2:");
        assertThat(ciphertext).isNotEqualTo("plain-api-key");
    }

    @Test
    void encryptAndDecrypt_roundTripsPlaintext() {
        var ciphertext = encryption.encrypt("plain-api-key");

        assertThat(encryption.decrypt(ciphertext)).isEqualTo("plain-api-key");
    }

    @Test
    void decrypt_supportsLegacyEcbCiphertext() {
        assertThat(encryption.decrypt(legacyEncrypt("plain-api-key"))).isEqualTo("plain-api-key");
    }

    private static String legacyEncrypt(String plaintext) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            var hash = digest.digest(SECRET.getBytes(StandardCharsets.UTF_8));
            var key = new SecretKeySpec(hash, "AES");
            var cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return Base64.getEncoder().encodeToString(cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
