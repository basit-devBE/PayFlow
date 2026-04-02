package com.example.payflow.payments.api;

import com.example.payflow.payments.api.request.SubmitPaymentRequest;
import com.example.payflow.payments.api.response.PaymentResponse;
import com.example.payflow.payments.service.PaymentService;
import com.example.payflow.shared.ApiResponse;
import com.example.payflow.shared.MerchantPrincipal;
import com.example.payflow.shared.PaymentStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    ResponseEntity<ApiResponse<PaymentResponse>> submit(
            @AuthenticationPrincipal MerchantPrincipal principal,
            @Valid @RequestBody SubmitPaymentRequest request) {
        var response = paymentService.submit(principal.merchantId(), principal.email(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Payment submitted successfully", HttpStatus.CREATED));
    }

    @GetMapping
    ResponseEntity<ApiResponse<List<PaymentResponse>>> list(
            @AuthenticationPrincipal MerchantPrincipal principal,
            @RequestParam(required = false) PaymentStatus status) {
        var payments = paymentService.findByMerchant(principal.merchantId(), status);
        return ResponseEntity.ok(ApiResponse.success(payments, "Payments retrieved successfully", HttpStatus.OK));
    }

    @GetMapping("/{id}")
    ResponseEntity<ApiResponse<PaymentResponse>> findById(
            @AuthenticationPrincipal MerchantPrincipal principal,
            @PathVariable UUID id) {
        var response = paymentService.findById(principal.merchantId(), id);
        return ResponseEntity.ok(ApiResponse.success(response, "Payment retrieved successfully", HttpStatus.OK));
    }
}
