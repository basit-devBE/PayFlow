package com.example.payflow.merchant.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class ApiKeyEncryption {

    private static final String ALGORITHM = "AES";
    private static final String LEGACY_CIPHER = "AES";
    private static final String CIPHER = "AES/GCM/NoPadding";
    private static final String VERSION_PREFIX = "v2:";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private final SecretKeySpec secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public ApiKeyEncryption(@Value("${payflow.encryption.secret}") String secret) {
        this.secretKey = deriveKey(secret);
    }

    public String encrypt(String plaintext) {
        try {
            var cipher = Cipher.getInstance(CIPHER);
            var iv = new byte[IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            var encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            var payload = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, payload, 0, iv.length);
            System.arraycopy(encrypted, 0, payload, iv.length, encrypted.length);
            return VERSION_PREFIX + Base64.getEncoder().encodeToString(payload);
        } catch (Exception e) {
            throw new IllegalStateException("Encryption failed", e);
        }
    }

    public String decrypt(String ciphertext) {
        if (ciphertext != null && ciphertext.startsWith(VERSION_PREFIX)) {
            return decryptGcm(ciphertext.substring(VERSION_PREFIX.length()));
        }
        return decryptLegacy(ciphertext);
    }

    private String decryptGcm(String ciphertext) {
        try {
            var decoded = Base64.getDecoder().decode(ciphertext);
            var iv = new byte[IV_LENGTH_BYTES];
            var encrypted = new byte[decoded.length - IV_LENGTH_BYTES];
            System.arraycopy(decoded, 0, iv, 0, IV_LENGTH_BYTES);
            System.arraycopy(decoded, IV_LENGTH_BYTES, encrypted, 0, encrypted.length);
            var cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            var decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Decryption failed", e);
        }
    }

    private String decryptLegacy(String ciphertext) {
        try {
            var cipher = Cipher.getInstance(LEGACY_CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            var decoded = Base64.getDecoder().decode(ciphertext);
            var decrypted = cipher.doFinal(decoded);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Decryption failed", e);
        }
    }

    private SecretKeySpec deriveKey(String secret) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            var hash = digest.digest(secret.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(hash, ALGORITHM);
        } catch (Exception e) {
            throw new IllegalStateException("Key derivation failed", e);
        }
    }
}
