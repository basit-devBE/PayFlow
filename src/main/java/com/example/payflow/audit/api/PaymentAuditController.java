package com.example.payflow.audit.api;

import com.example.payflow.audit.service.AuditEventResponse;
import com.example.payflow.audit.service.AuditService;
import com.example.payflow.shared.ApiResponse;
import com.example.payflow.shared.MerchantPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentAuditController {

    private final AuditService auditService;

    @GetMapping("/{paymentId}/events")
    ResponseEntity<ApiResponse<List<AuditEventResponse>>> findPaymentEvents(
            @AuthenticationPrincipal MerchantPrincipal principal,
            @PathVariable UUID paymentId) {
        var events = auditService.findByMerchantIdAndPaymentId(principal.merchantId(), paymentId);
        return ResponseEntity.ok(ApiResponse.success(events, "Payment events retrieved successfully", HttpStatus.OK));
    }
}
