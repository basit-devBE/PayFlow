package com.example.payflow.merchant.api;

import com.example.payflow.merchant.service.MerchantService;
import com.example.payflow.merchant.api.request.RegisterMerchantRequest;
import com.example.payflow.merchant.api.response.RegisterMerchantResponse;
import com.example.payflow.shared.MerchantPrincipal;
import com.example.payflow.shared.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/merchants")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;

    @PostMapping("/register")
    ResponseEntity<RegisterMerchantResponse> register(@Valid @RequestBody RegisterMerchantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(merchantService.register(request));
    }

    @PostMapping("/keys/rotate")
    ResponseEntity<ApiResponse<String>> rotateApiKey(@AuthenticationPrincipal MerchantPrincipal principal) {
        var newKey = merchantService.rotateApiKey(principal.merchantId());
        return ResponseEntity.ok(ApiResponse.success(newKey, "API key rotated successfully", HttpStatus.OK));
    }
}
