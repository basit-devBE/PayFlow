package com.example.payflow.audit;

import com.example.payflow.audit.service.AuditEventResponse;
import com.example.payflow.audit.service.AuditService;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuditControllerTest {

    private static final String TIMESTAMP = String.valueOf(Instant.now().getEpochSecond());
    private static final String SIGNATURE = "test-signature";
    private static final UUID MERCHANT_ID = UUID.randomUUID();
    private static final UUID PAYMENT_ID = UUID.randomUUID();

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AuditService auditService;

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
    void findEvents_returnsMerchantAuditEvents() throws Exception {
        when(auditService.findByMerchantId(MERCHANT_ID)).thenReturn(List.of(
                new AuditEventResponse(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "Payment.Transaction.Initiated",
                        "{\"paymentId\":\"abc\"}",
                        Instant.now()
                )
        ));

        mockMvc.perform(get("/api/v1/audit/events")
                        .header("X-Merchant-ID", MERCHANT_ID.toString())
                        .header("X-Timestamp", TIMESTAMP)
                        .header("X-Signature", SIGNATURE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].eventType").value("Payment.Transaction.Initiated"));
    }

    @Test
    void findPaymentEvents_returnsPaymentAuditEvents() throws Exception {
        when(auditService.findByMerchantIdAndPaymentId(MERCHANT_ID, PAYMENT_ID)).thenReturn(List.of(
                new AuditEventResponse(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "Payment.Transaction.Initiated",
                        "{\"paymentId\":\"abc\"}",
                        Instant.now()
                )
        ));

        mockMvc.perform(get("/api/v1/payments/{paymentId}/events", PAYMENT_ID)
                        .header("X-Merchant-ID", MERCHANT_ID.toString())
                        .header("X-Timestamp", TIMESTAMP)
                        .header("X-Signature", SIGNATURE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].eventType").value("Payment.Transaction.Initiated"));
    }
}
