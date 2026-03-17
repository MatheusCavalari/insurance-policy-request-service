package br.com.matheus.insurance.interfaces.rest;

import br.com.matheus.insurance.application.dto.CreatePolicyRequestResult;
import br.com.matheus.insurance.application.usecase.CancelPolicyRequestUseCase;
import br.com.matheus.insurance.application.usecase.CreatePolicyRequestUseCase;
import br.com.matheus.insurance.application.usecase.GetPolicyRequestByIdUseCase;
import br.com.matheus.insurance.application.usecase.GetPolicyRequestsByCustomerIdUseCase;
import br.com.matheus.insurance.domain.enums.PolicyRequestStatus;
import br.com.matheus.insurance.domain.model.PolicyRequest;
import br.com.matheus.insurance.interfaces.rest.mapper.PolicyRequestHttpMapper;
import br.com.matheus.insurance.support.PolicyRequestTestFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PolicyRequestController.class)
@Import(PolicyRequestHttpMapper.class)
class PolicyRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreatePolicyRequestUseCase createPolicyRequestUseCase;

    @MockitoBean
    private GetPolicyRequestByIdUseCase getPolicyRequestByIdUseCase;

    @MockitoBean
    private GetPolicyRequestsByCustomerIdUseCase getPolicyRequestsByCustomerIdUseCase;

    @MockitoBean
    private CancelPolicyRequestUseCase cancelPolicyRequestUseCase;

    @Test
    void should_create_policy_request() throws Exception {
        UUID id = UUID.randomUUID();

        when(createPolicyRequestUseCase.execute(any())).thenReturn(
                new CreatePolicyRequestResult(
                        id,
                        PolicyRequestStatus.RECEIVED,
                        Instant.parse("2026-03-14T10:00:00Z")
                )
        );

        String payload = """
                {
                  "customerId": "22222222-2222-2222-2222-222222222222",
                  "productId": 123,
                  "category": "AUTO",
                  "salesChannel": "MOBILE",
                  "paymentMethod": "CREDIT_CARD",
                  "totalMonthlyPremiumAmount": 75.25,
                  "insuredAmount": 200000.00,
                  "coverages": {
                    "Roubo": 100000.00,
                    "Perda Total": 100000.00
                  },
                  "assistances": ["Guincho 24h", "Chaveiro"]
                }
                """;

        mockMvc.perform(post("/policy-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/policy-requests/" + id))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.status").value("RECEIVED"));
    }

    @Test
    void should_get_policy_request_by_id() throws Exception {
        PolicyRequest policyRequest = PolicyRequestTestFactory.newPolicyRequest();

        when(getPolicyRequestByIdUseCase.execute(eq(policyRequest.getId()))).thenReturn(policyRequest);

        mockMvc.perform(get("/policy-requests/{id}", policyRequest.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(policyRequest.getId().toString()))
                .andExpect(jsonPath("$.customerId").value(policyRequest.getCustomerId().toString()))
                .andExpect(jsonPath("$.status").value("RECEIVED"))
                .andExpect(jsonPath("$.history.length()").value(1));
    }

    @Test
    void should_get_policy_requests_by_customer_id() throws Exception {
        PolicyRequest request = PolicyRequestTestFactory.newPolicyRequest();

        when(getPolicyRequestsByCustomerIdUseCase.execute(eq(request.getCustomerId())))
                .thenReturn(List.of(request));

        mockMvc.perform(get("/policy-requests")
                        .param("customerId", request.getCustomerId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(request.getId().toString()))
                .andExpect(jsonPath("$[0].customerId").value(request.getCustomerId().toString()));
    }

    @Test
    void should_cancel_policy_request() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(cancelPolicyRequestUseCase).execute(id);

        mockMvc.perform(post("/policy-requests/{id}/cancel", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void should_return_bad_request_when_payload_is_invalid() throws Exception {
        String payload = """
                {
                  "productId": 123
                }
                """;

        mockMvc.perform(post("/policy-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }
}