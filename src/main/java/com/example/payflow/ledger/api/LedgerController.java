package com.example.payflow.ledger.api;

import com.example.payflow.ledger.api.response.JournalEntryResponse;
import com.example.payflow.ledger.service.LedgerService;
import com.example.payflow.shared.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ledger")
@RequiredArgsConstructor
public class LedgerController {

    private final LedgerService ledgerService;

    @GetMapping("/journal/{correlationId}")
    ResponseEntity<ApiResponse<List<JournalEntryResponse>>> findByCorrelationId(@PathVariable UUID correlationId) {
        var entries = ledgerService.findByCorrelationId(correlationId);
        return ResponseEntity.ok(ApiResponse.success(entries, "Ledger entries retrieved successfully", HttpStatus.OK));
    }
}
