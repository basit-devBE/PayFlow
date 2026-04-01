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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/events")
    ResponseEntity<ApiResponse<List<AuditEventResponse>>> findEvents(@AuthenticationPrincipal MerchantPrincipal principal) {
        var events = auditService.findByMerchantId(principal.merchantId());
        return ResponseEntity.ok(ApiResponse.success(events, "Audit events retrieved successfully", HttpStatus.OK));
    }
}
