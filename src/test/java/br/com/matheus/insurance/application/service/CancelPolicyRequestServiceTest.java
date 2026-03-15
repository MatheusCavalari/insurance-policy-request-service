package br.com.matheus.insurance.application.service;

import br.com.matheus.insurance.domain.enums.PolicyRequestStatus;
import br.com.matheus.insurance.domain.model.PolicyRequest;
import br.com.matheus.insurance.domain.port.PolicyRequestRepository;
import br.com.matheus.insurance.infrastructure.messaging.OutboxEventFactory;
import br.com.matheus.insurance.support.PolicyRequestTestFactory;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CancelPolicyRequestServiceTest {

    @Test
    void should_cancel_received_policy_request() {
        PolicyRequestRepository repository = mock(PolicyRequestRepository.class);
        OutboxEventFactory outboxEventFactory = mock(OutboxEventFactory.class);

        CancelPolicyRequestService service = new CancelPolicyRequestService(repository, outboxEventFactory);

        PolicyRequest request = PolicyRequestTestFactory.newPolicyRequest();

        when(repository.findById(request.getId())).thenReturn(Optional.of(request));
        when(repository.save(any(PolicyRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.execute(request.getId());

        ArgumentCaptor<PolicyRequest> captor = ArgumentCaptor.forClass(PolicyRequest.class);
        verify(repository).save(captor.capture());

        PolicyRequest saved = captor.getValue();

        assertEquals(PolicyRequestStatus.CANCELED, saved.getStatus());
        assertNotNull(saved.getFinishedAt());

        verify(outboxEventFactory, times(1))
                .appendPolicyStatusChanged(any(PolicyRequest.class), any());
    }

    @Test
    void should_cancel_pending_policy_request() {
        PolicyRequestRepository repository = mock(PolicyRequestRepository.class);
        OutboxEventFactory outboxEventFactory = mock(OutboxEventFactory.class);

        CancelPolicyRequestService service = new CancelPolicyRequestService(repository, outboxEventFactory);

        PolicyRequest request = PolicyRequestTestFactory.restoredPendingPolicyRequest();

        when(repository.findById(request.getId())).thenReturn(Optional.of(request));
        when(repository.save(any(PolicyRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.execute(request.getId());

        ArgumentCaptor<PolicyRequest> captor = ArgumentCaptor.forClass(PolicyRequest.class);
        verify(repository).save(captor.capture());

        PolicyRequest saved = captor.getValue();

        assertEquals(PolicyRequestStatus.CANCELED, saved.getStatus());
        assertEquals(4, saved.getHistory().size());
        assertEquals(PolicyRequestStatus.CANCELED, saved.getHistory().get(3).status());

        verify(outboxEventFactory, times(1))
                .appendPolicyStatusChanged(any(PolicyRequest.class), any());
    }

    @Test
    void should_fail_when_canceling_approved_policy_request() {
        PolicyRequestRepository repository = mock(PolicyRequestRepository.class);
        OutboxEventFactory outboxEventFactory = mock(OutboxEventFactory.class);

        CancelPolicyRequestService service = new CancelPolicyRequestService(repository, outboxEventFactory);

        PolicyRequest request = PolicyRequestTestFactory.restoredApprovedPolicyRequest();

        when(repository.findById(request.getId())).thenReturn(Optional.of(request));

        assertThrows(IllegalArgumentException.class, () -> service.execute(request.getId()));

        verify(repository, never()).save(any());
        verifyNoInteractions(outboxEventFactory);
    }

    @Test
    void should_throw_when_policy_request_does_not_exist() {
        PolicyRequestRepository repository = mock(PolicyRequestRepository.class);
        OutboxEventFactory outboxEventFactory = mock(OutboxEventFactory.class);

        CancelPolicyRequestService service = new CancelPolicyRequestService(repository, outboxEventFactory);

        UUID id = UUID.randomUUID();

        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.execute(id));

        verify(repository, never()).save(any());
        verifyNoInteractions(outboxEventFactory);
    }
}
