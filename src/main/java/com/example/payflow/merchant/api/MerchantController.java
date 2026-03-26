package com.example.payflow.merchant.api;

import com.example.payflow.merchant.MerchantService;
import com.example.payflow.merchant.api.request.RegisterMerchantRequest;
import com.example.payflow.merchant.api.response.RegisterMerchantResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/merchants")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;

    @PostMapping("/register")
    ResponseEntity<RegisterMerchantResponse> register(@Valid @RequestBody RegisterMerchantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(merchantService.register(request));
    }
}
