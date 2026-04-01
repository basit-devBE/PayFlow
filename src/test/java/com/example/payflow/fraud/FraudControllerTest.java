package com.example.payflow.fraud;

import com.example.payflow.fraud.api.response.FraudAssessmentResponse;
import com.example.payflow.fraud.service.FraudService;
import com.example.payflow.merchant.service.MerchantLookupService;
import com.example.payflow.security.HmacSignatureVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FraudControllerTest {

    private static final String TIMESTAMP = String.valueOf(Instant.now().getEpochSecond());
    private static final String SIGNATURE = "test-signature";
    private static final UUID MERCHANT_ID = UUID.randomUUID();
    private static final UUID TRANSACTION_ID = UUID.randomUUID();

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    FraudService fraudService;

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
    void findByTransactionId_existingAssessment_returns200() throws Exception {
        when(fraudService.findByTransactionId(MERCHANT_ID, TRANSACTION_ID)).thenReturn(
                new FraudAssessmentResponse(
                        UUID.randomUUID(),
                        TRANSACTION_ID,
                        100,
                        "APPROVE",
                        Instant.now()
                )
        );

        mockMvc.perform(get("/api/v1/fraud/assessments/{transactionId}", TRANSACTION_ID)
                        .header("X-Merchant-ID", MERCHANT_ID.toString())
                        .header("X-Timestamp", TIMESTAMP)
                        .header("X-Signature", SIGNATURE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.transactionId").value(TRANSACTION_ID.toString()))
                .andExpect(jsonPath("$.data.decision").value("APPROVE"));
    }

    @Test
    void findByTransactionId_missingAssessment_returns404() throws Exception {
        when(fraudService.findByTransactionId(MERCHANT_ID, TRANSACTION_ID))
                .thenThrow(new FraudAssessmentNotFoundException(TRANSACTION_ID));

        mockMvc.perform(get("/api/v1/fraud/assessments/{transactionId}", TRANSACTION_ID)
                        .header("X-Merchant-ID", MERCHANT_ID.toString())
                        .header("X-Timestamp", TIMESTAMP)
                        .header("X-Signature", SIGNATURE))
                .andExpect(status().isNotFound());
    }
}
