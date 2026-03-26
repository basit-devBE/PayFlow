package com.example.payflow.security;

import com.example.payflow.merchant.MerchantLookupService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String REGISTER_PATH = "/api/v1/merchants/register";

    private final MerchantLookupService merchantLookupService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().equals(REGISTER_PATH);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        var rawKey = request.getHeader(API_KEY_HEADER);
        log.info("Received Api key: {}", rawKey);

        if (rawKey == null || rawKey.isBlank()) {
            rejectUnauthorized(response);
            return;
        }

        var keyHash = hashKey(rawKey);

        merchantLookupService.findByApiKeyHash(keyHash)
                .ifPresentOrElse(
                        identity -> {
                            var principal = new MerchantPrincipal(identity.merchantId(), identity.email());
                            SecurityContextHolder.getContext().setAuthentication(principal);
                        },
                        () -> {}
                );

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            rejectUnauthorized(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void rejectUnauthorized(HttpServletResponse response) throws IOException {
        SecurityContextHolder.clearContext();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"status\":401,\"detail\":\"Missing or invalid API key\"}");
    }

    private String hashKey(String rawKey) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            var hashBytes = digest.digest(rawKey.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
