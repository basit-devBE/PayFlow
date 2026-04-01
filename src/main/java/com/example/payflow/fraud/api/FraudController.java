package com.example.payflow.fraud.api;

import com.example.payflow.fraud.api.response.FraudAssessmentResponse;
import com.example.payflow.fraud.service.FraudService;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fraud")
@RequiredArgsConstructor
public class FraudController {

    private final FraudService fraudService;

    @GetMapping("/assessments/{transactionId}")
    ResponseEntity<ApiResponse<FraudAssessmentResponse>> findByTransactionId(
            @AuthenticationPrincipal MerchantPrincipal principal,
            @PathVariable UUID transactionId) {
        var response = fraudService.findByTransactionId(principal.merchantId(), transactionId);
        return ResponseEntity.ok(ApiResponse.success(response, "Fraud assessment retrieved successfully", HttpStatus.OK));
    }
}
