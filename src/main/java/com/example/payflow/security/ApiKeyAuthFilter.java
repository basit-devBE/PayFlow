package com.example.payflow.security;

import com.example.payflow.merchant.service.MerchantLookupService;
import com.example.payflow.shared.MerchantPrincipal;
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
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final String HEADER_MERCHANT_ID = "X-Merchant-ID";
    private static final String HEADER_TIMESTAMP   = "X-Timestamp";
    private static final String HEADER_SIGNATURE   = "X-Signature";
    private static final String REGISTER_PATH      = "/api/v1/merchants/register";

    private final MerchantLookupService merchantLookupService;
    private final HmacSignatureVerifier signatureVerifier;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().equals(REGISTER_PATH);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        var wrapped = new CachedBodyRequestWrapper(request);

        var merchantIdHeader = wrapped.getHeader(HEADER_MERCHANT_ID);
        var timestamp        = wrapped.getHeader(HEADER_TIMESTAMP);
        var signature        = wrapped.getHeader(HEADER_SIGNATURE);

        if (isMissing(merchantIdHeader) || isMissing(timestamp) || isMissing(signature)) {
            rejectUnauthorized(response);
            return;
        }

        if (signatureVerifier.isTimestampStale(timestamp)) {
            rejectUnauthorized(response);
            return;
        }

        UUID merchantId;
        try {
            merchantId = UUID.fromString(merchantIdHeader);
        } catch (IllegalArgumentException e) {
            rejectUnauthorized(response);
            return;
        }

        var body = new String(wrapped.getBody(), java.nio.charset.StandardCharsets.UTF_8);

        merchantLookupService.findActiveKeyByMerchantId(merchantId)
                .ifPresent(identity -> {
                    if (signatureVerifier.verify(identity.rawKey(), timestamp, body, signature)) {
                        SecurityContextHolder.getContext()
                                .setAuthentication(new MerchantPrincipal(identity.merchantId(), identity.email()));
                    }
                });

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            rejectUnauthorized(response);
            return;
        }

        filterChain.doFilter(wrapped, response);
    }

    private boolean isMissing(String value) {
        return value == null || value.isBlank();
    }

    private void rejectUnauthorized(HttpServletResponse response) throws IOException {
        SecurityContextHolder.clearContext();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"status\":401,\"detail\":\"Missing, invalid, or expired request signature\"}");
    }
}
