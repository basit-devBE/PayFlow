package com.example.payflow.security;

import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

@Component
public class HmacSignatureVerifier {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final long MAX_TIMESTAMP_AGE_SECONDS = 300;

    public boolean isTimestampStale(String timestamp) {
        try {
            var requestTime = Instant.ofEpochSecond(Long.parseLong(timestamp));
            var age = Math.abs(Instant.now().getEpochSecond() - requestTime.getEpochSecond());
            return age > MAX_TIMESTAMP_AGE_SECONDS;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    public boolean verify(String secret, String timestamp, String body, String signature) {
        var expected = computeSignature(secret, timestamp, body);
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                signature.getBytes(StandardCharsets.UTF_8)
        );
    }

    private String computeSignature(String secret, String timestamp, String body) {
        try {
            var mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            var payload = timestamp + "." + body;
            return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("HMAC-SHA256 unavailable", e);
        }
    }
}
