package com.example.payflow.payments;

import com.example.payflow.merchant.service.MerchantLookupService;
import com.example.payflow.payments.api.response.PaymentResponse;
import com.example.payflow.payments.service.PaymentService;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerTest {

    private static final String TEST_API_KEY = "test-api-key";
    private static final UUID MERCHANT_ID = UUID.randomUUID();
    private static final UUID PAYMENT_ID = UUID.randomUUID();

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    PaymentService paymentService;

    @MockitoBean
    MerchantLookupService merchantLookupService;

    @BeforeEach
    void setUp() {
        when(merchantLookupService.findByApiKeyHash(any()))
                .thenReturn(java.util.Optional.of(new MerchantLookupService.MerchantIdentity(MERCHANT_ID, "merchant@example.com")));
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
        when(paymentService.findByMerchant(MERCHANT_ID, null)).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/v1/payments")
                        .header("X-API-Key", TEST_API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(PAYMENT_ID.toString()));
    }

    @Test
    void list_withStatusFilter_returns200() throws Exception {
        when(paymentService.findByMerchant(MERCHANT_ID, PaymentStatus.PENDING)).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/v1/payments")
                        .header("X-API-Key", TEST_API_KEY)
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].status").value("PENDING"));
    }

    @Test
    void submit_validRequest_returns201() throws Exception {
        when(paymentService.submit(eq(MERCHANT_ID), any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/v1/payments")
                        .header("X-API-Key", TEST_API_KEY)
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
        when(paymentService.submit(eq(MERCHANT_ID), any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/v1/payments")
                        .header("X-API-Key", TEST_API_KEY)
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
        when(paymentService.findById(PAYMENT_ID)).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/v1/payments/{id}", PAYMENT_ID)
                        .header("X-API-Key", TEST_API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(PAYMENT_ID.toString()));
    }

    @Test
    void findById_nonExistentPayment_returns404() throws Exception {
        when(paymentService.findById(PAYMENT_ID)).thenThrow(new PaymentNotFoundException(PAYMENT_ID));

        mockMvc.perform(get("/api/v1/payments/{id}", PAYMENT_ID)
                        .header("X-API-Key", TEST_API_KEY))
                .andExpect(status().isNotFound());
    }
}
