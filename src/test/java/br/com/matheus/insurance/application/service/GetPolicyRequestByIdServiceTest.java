package br.com.matheus.insurance.application.service;

import br.com.matheus.insurance.domain.exception.ResourceNotFoundException;
import br.com.matheus.insurance.domain.model.PolicyRequest;
import br.com.matheus.insurance.domain.port.PolicyRequestRepository;
import br.com.matheus.insurance.support.PolicyRequestTestFactory;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetPolicyRequestByIdServiceTest {

    @Test
    void should_return_policy_request_when_found() {
        PolicyRequestRepository repository = mock(PolicyRequestRepository.class);
        GetPolicyRequestByIdService service = new GetPolicyRequestByIdService(repository);

        PolicyRequest request = PolicyRequestTestFactory.newPolicyRequest();

        when(repository.findById(request.getId())).thenReturn(Optional.of(request));

        PolicyRequest result = service.execute(request.getId());

        assertNotNull(result);
        assertEquals(request.getId(), result.getId());
    }

    @Test
    void should_throw_when_policy_request_not_found() {
        PolicyRequestRepository repository = mock(PolicyRequestRepository.class);
        GetPolicyRequestByIdService service = new GetPolicyRequestByIdService(repository);

        UUID id = UUID.randomUUID();

        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.execute(id));
    }
}