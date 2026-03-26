package com.example.payflow.merchant;

import com.example.payflow.merchant.api.response.RegisterMerchantResponse;
import com.example.payflow.merchant.service.MerchantService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MerchantControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    MerchantService merchantService;

    @Test
    void register_validRequest_returns201() throws Exception {
        var merchantId = UUID.randomUUID();
        when(merchantService.register(any())).thenReturn(
                new RegisterMerchantResponse(merchantId, "merchant@example.com", "raw-api-key")
        );

        mockMvc.perform(post("/api/v1/merchants/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Acme Corp",
                                  "email": "merchant@example.com"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.merchantId").value(merchantId.toString()))
                .andExpect(jsonPath("$.apiKey").exists());
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        when(merchantService.register(any()))
                .thenThrow(new MerchantAlreadyExistsException("merchant@example.com"));

        mockMvc.perform(post("/api/v1/merchants/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Acme Corp",
                                  "email": "merchant@example.com"
                                }
                                """))
                .andExpect(status().isConflict());
    }
}
