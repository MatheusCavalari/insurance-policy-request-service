package br.com.matheus.insurance.interfaces.consumer;

import br.com.matheus.insurance.domain.enums.PolicyRequestStatus;
import br.com.matheus.insurance.domain.model.PolicyRequest;
import br.com.matheus.insurance.domain.port.PolicyRequestRepository;
import br.com.matheus.insurance.infrastructure.messaging.dto.UnderwritingProcessedEvent;
import br.com.matheus.insurance.support.PolicyRequestTestFactory;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UnderwritingProcessedConsumerTest {

    @Test
    void should_approve_policy_when_underwriting_is_approved_and_payment_already_approved() {
        PolicyRequestRepository repository = mock(PolicyRequestRepository.class);
        UnderwritingProcessedConsumer consumer = new UnderwritingProcessedConsumer(repository);

        PolicyRequest request = PolicyRequestTestFactory.restoredPendingWithPaymentApproved();

        when(repository.findById(request.getId())).thenReturn(Optional.of(request));

        consumer.consume(new UnderwritingProcessedEvent(
                request.getId(),
                "APPROVED",
                Instant.parse("2026-03-14T12:00:00Z")
        ));

        ArgumentCaptor<PolicyRequest> captor = ArgumentCaptor.forClass(PolicyRequest.class);
        verify(repository).save(captor.capture());

        PolicyRequest saved = captor.getValue();
        assertEquals(PolicyRequestStatus.APPROVED, saved.getStatus());
        assertNotNull(saved.getFinishedAt());
    }

    @Test
    void should_reject_policy_when_underwriting_is_denied() {
        PolicyRequestRepository repository = mock(PolicyRequestRepository.class);
        UnderwritingProcessedConsumer consumer = new UnderwritingProcessedConsumer(repository);

        PolicyRequest request = PolicyRequestTestFactory.restoredPendingPolicyRequest();

        when(repository.findById(request.getId())).thenReturn(Optional.of(request));

        consumer.consume(new UnderwritingProcessedEvent(
                request.getId(),
                "DENIED",
                Instant.parse("2026-03-14T12:00:00Z")
        ));

        ArgumentCaptor<PolicyRequest> captor = ArgumentCaptor.forClass(PolicyRequest.class);
        verify(repository).save(captor.capture());

        PolicyRequest saved = captor.getValue();
        assertEquals(PolicyRequestStatus.REJECTED, saved.getStatus());
    }

    @Test
    void should_ignore_when_request_is_already_final() {
        PolicyRequestRepository repository = mock(PolicyRequestRepository.class);
        UnderwritingProcessedConsumer consumer = new UnderwritingProcessedConsumer(repository);

        PolicyRequest request = PolicyRequestTestFactory.restoredApprovedPolicyRequest();

        when(repository.findById(request.getId())).thenReturn(Optional.of(request));

        consumer.consume(new UnderwritingProcessedEvent(
                request.getId(),
                "APPROVED",
                Instant.parse("2026-03-14T12:00:00Z")
        ));

        verify(repository, never()).save(any());
    }

    @Test
    void should_throw_when_request_not_found() {
        PolicyRequestRepository repository = mock(PolicyRequestRepository.class);
        UnderwritingProcessedConsumer consumer = new UnderwritingProcessedConsumer(repository);

        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                consumer.consume(new UnderwritingProcessedEvent(
                        id,
                        "APPROVED",
                        Instant.parse("2026-03-14T12:00:00Z")
                ))
        );
    }
}