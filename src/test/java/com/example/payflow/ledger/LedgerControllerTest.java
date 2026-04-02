package com.example.payflow.ledger;

import com.example.payflow.ledger.api.response.JournalEntryResponse;
import com.example.payflow.ledger.service.LedgerService;
import com.example.payflow.merchant.service.MerchantLookupService;
import com.example.payflow.security.HmacSignatureVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LedgerControllerTest {

    private static final String TIMESTAMP = String.valueOf(Instant.now().getEpochSecond());
    private static final String SIGNATURE = "test-signature";
    private static final UUID MERCHANT_ID = UUID.randomUUID();

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    LedgerService ledgerService;

    @MockitoBean
    MerchantLookupService merchantLookupService;

    @MockitoBean
    HmacSignatureVerifier signatureVerifier;

    @BeforeEach
    void setUp() {
        when(signatureVerifier.isTimestampStale(any())).thenReturn(false);
        when(signatureVerifier.verify(any(), any(), any(), any())).thenReturn(true);
        when(merchantLookupService.findActiveKeyByMerchantId(any()))
                .thenReturn(Optional.of(new MerchantLookupService.MerchantIdentity(MERCHANT_ID, "merchant@example.com", "test-key-hash")));
    }

    @Test
    void findByCorrelationId_returns200() throws Exception {
        var correlationId = UUID.randomUUID();
        var paymentId = UUID.randomUUID();
        var entryId = UUID.randomUUID();
        when(ledgerService.findByCorrelationId(eq(MERCHANT_ID), eq(correlationId))).thenReturn(List.of(
                new JournalEntryResponse(
                        entryId,
                        correlationId,
                        paymentId,
                        "merchant:" + MERCHANT_ID,
                        "payee:" + UUID.randomUUID(),
                        new BigDecimal("150.00"),
                        "USD",
                        Instant.now()
                )
        ));

        mockMvc.perform(get("/api/v1/ledger/journal/{correlationId}", correlationId)
                        .header("X-Merchant-ID", MERCHANT_ID.toString())
                        .header("X-Timestamp", TIMESTAMP)
                        .header("X-Signature", SIGNATURE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(entryId.toString()))
                .andExpect(jsonPath("$.data[0].correlationId").value(correlationId.toString()))
                .andExpect(jsonPath("$.data[0].amount").value(150.00));
    }
}
