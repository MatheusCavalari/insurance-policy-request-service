package br.com.matheus.insurance.application.service;

import br.com.matheus.insurance.domain.model.PolicyRequest;
import br.com.matheus.insurance.domain.port.PolicyRequestRepository;
import br.com.matheus.insurance.support.PolicyRequestTestFactory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetPolicyRequestsByCustomerIdServiceTest {

    @Test
    void should_return_policy_requests_by_customer_id() {
        PolicyRequestRepository repository = mock(PolicyRequestRepository.class);
        GetPolicyRequestsByCustomerIdService service = new GetPolicyRequestsByCustomerIdService(repository);

        PolicyRequest request1 = PolicyRequestTestFactory.newPolicyRequest();
        PolicyRequest request2 = PolicyRequestTestFactory.newPolicyRequest();

        UUID customerId = request1.getCustomerId();

        when(repository.findByCustomerId(customerId)).thenReturn(List.of(request1, request2));

        List<PolicyRequest> result = service.execute(customerId);

        assertEquals(2, result.size());
    }

    @Test
    void should_return_empty_list_when_customer_has_no_requests() {
        PolicyRequestRepository repository = mock(PolicyRequestRepository.class);
        GetPolicyRequestsByCustomerIdService service = new GetPolicyRequestsByCustomerIdService(repository);

        UUID customerId = UUID.randomUUID();

        when(repository.findByCustomerId(customerId)).thenReturn(List.of());

        List<PolicyRequest> result = service.execute(customerId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}