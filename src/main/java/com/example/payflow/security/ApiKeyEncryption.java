package com.example.payflow.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Component
public class ApiKeyEncryption {

    private static final String ALGORITHM = "AES";
    private final SecretKeySpec secretKey;

    public ApiKeyEncryption(@Value("${payflow.encryption.secret}") String secret) {
        this.secretKey = deriveKey(secret);
    }

    public String encrypt(String plaintext) {
        try {
            var cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            var encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new IllegalStateException("Encryption failed", e);
        }
    }

    public String decrypt(String ciphertext) {
        try {
            var cipher = Cipher.getInstance(ALGORITHM);
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
