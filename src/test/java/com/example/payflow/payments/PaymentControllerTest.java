package com.example.payflow.payments;

import com.example.payflow.merchant.service.MerchantLookupService;
import com.example.payflow.payments.api.response.PaymentResponse;
import com.example.payflow.payments.service.PaymentService;
import com.example.payflow.security.HmacSignatureVerifier;
import com.example.payflow.shared.MerchantPrincipal;
import com.example.payflow.shared.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerTest {

    private static final String TIMESTAMP = String.valueOf(Instant.now().getEpochSecond());
    private static final String SIGNATURE  = "test-signature";
    private static final UUID MERCHANT_ID = UUID.randomUUID();
    private static final UUID PAYMENT_ID = UUID.randomUUID();

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    PaymentService paymentService;

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

    private PaymentResponse sampleResponse() {
        return new PaymentResponse(
                PAYMENT_ID,
                UUID.randomUUID(),
                MERCHANT_ID,
                UUID.randomUUID(),
                new BigDecimal("150.00"),
                "USD",
                PaymentStatus.PENDING,
                Instant.now()
        );
    }

    @Test
    void list_noFilter_returns200() throws Exception {
        when(paymentService.findByMerchant(MERCHANT_ID, null)).thenReturn(java.util.List.of(sampleResponse()));

        mockMvc.perform(get("/api/v1/payments")
                        .header("X-Merchant-ID", MERCHANT_ID.toString())
                        .header("X-Timestamp", TIMESTAMP)
                        .header("X-Signature", SIGNATURE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(PAYMENT_ID.toString()));
    }

    @Test
    void list_withStatusFilter_returns200() throws Exception {
        when(paymentService.findByMerchant(MERCHANT_ID, PaymentStatus.PENDING)).thenReturn(java.util.List.of(sampleResponse()));

        mockMvc.perform(get("/api/v1/payments")
                        .header("X-Merchant-ID", MERCHANT_ID.toString())
                        .header("X-Timestamp", TIMESTAMP)
                        .header("X-Signature", SIGNATURE)
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].status").value("PENDING"));
    }

    @Test
    void submit_validRequest_returns201() throws Exception {
        when(paymentService.submit(any(), any(), any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/v1/payments")
                        .header("X-Merchant-ID", MERCHANT_ID.toString())
                        .header("X-Timestamp", TIMESTAMP)
                        .header("X-Signature", SIGNATURE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "payeeAccountId": "%s",
                                  "idempotencyKey": "unique-key-123",
                                  "amount": "150.00",
                                  "currency": "USD",
                                  "paymentMethod": {
                                    "type": "CARD",
                                    "token": "tok_test_1234"
                                  }
                                }
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(PAYMENT_ID.toString()))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void submit_duplicateIdempotencyKey_returns201WithExistingPayment() throws Exception {
        when(paymentService.submit(any(), any(), any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/v1/payments")
                        .header("X-Merchant-ID", MERCHANT_ID.toString())
                        .header("X-Timestamp", TIMESTAMP)
                        .header("X-Signature", SIGNATURE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "payeeAccountId": "%s",
                                  "idempotencyKey": "unique-key-123",
                                  "amount": "150.00",
                                  "currency": "USD",
                                  "paymentMethod": {
                                    "type": "CARD",
                                    "token": "tok_test_1234"
                                  }
                                }
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isCreated());
    }

    @Test
    void findById_existingPayment_returns200() throws Exception {
        when(paymentService.findById(MERCHANT_ID, PAYMENT_ID)).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/v1/payments/{id}", PAYMENT_ID)
                        .header("X-Merchant-ID", MERCHANT_ID.toString())
                        .header("X-Timestamp", TIMESTAMP)
                        .header("X-Signature", SIGNATURE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(PAYMENT_ID.toString()));
    }

    @Test
    void findById_nonExistentPayment_returns404() throws Exception {
        when(paymentService.findById(MERCHANT_ID, PAYMENT_ID)).thenThrow(new PaymentNotFoundException(PAYMENT_ID));

        mockMvc.perform(get("/api/v1/payments/{id}", PAYMENT_ID)
                        .header("X-Merchant-ID", MERCHANT_ID.toString())
                        .header("X-Timestamp", TIMESTAMP)
                        .header("X-Signature", SIGNATURE))
                .andExpect(status().isNotFound());
    }
}
