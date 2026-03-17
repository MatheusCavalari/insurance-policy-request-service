package br.com.matheus.insurance.application.service;

import br.com.matheus.insurance.application.dto.CreatePolicyRequestCommand;
import br.com.matheus.insurance.application.dto.CreatePolicyRequestResult;
import br.com.matheus.insurance.domain.enums.PolicyRequestStatus;
import br.com.matheus.insurance.domain.model.PolicyRequest;
import br.com.matheus.insurance.domain.port.PolicyRequestRepository;
import br.com.matheus.insurance.infrastructure.messaging.OutboxEventFactory;
import br.com.matheus.insurance.support.PolicyRequestTestFactory;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CreatePolicyRequestServiceTest {

    @Test
    void should_create_and_persist_policy_request() {
        PolicyRequestRepository repository = mock(PolicyRequestRepository.class);
        OutboxEventFactory outboxEventFactory = mock(OutboxEventFactory.class);

        CreatePolicyRequestService service = new CreatePolicyRequestService(repository, outboxEventFactory);

        CreatePolicyRequestCommand command = PolicyRequestTestFactory.newCreateCommand();

        when(repository.save(any(PolicyRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreatePolicyRequestResult result = service.execute(command);

        ArgumentCaptor<PolicyRequest> captor = ArgumentCaptor.forClass(PolicyRequest.class);
        verify(repository).save(captor.capture());

        PolicyRequest saved = captor.getValue();

        assertNotNull(result.id());
        assertEquals(PolicyRequestStatus.RECEIVED, result.status());
        assertNotNull(result.createdAt());

        assertEquals(command.customerId(), saved.getCustomerId());
        assertEquals(command.productId(), saved.getProductId());
        assertEquals(command.category(), saved.getCategory());
        assertEquals(command.salesChannel(), saved.getSalesChannel());
        assertEquals(command.paymentMethod(), saved.getPaymentMethod());
        assertEquals(PolicyRequestStatus.RECEIVED, saved.getStatus());

        verify(outboxEventFactory, times(1))
                .appendPolicyRequestReceived(any(PolicyRequest.class), any());
    }
}
