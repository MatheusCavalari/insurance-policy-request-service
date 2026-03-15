package br.com.matheus.insurance.interfaces.consumer;

import br.com.matheus.insurance.domain.enums.PolicyRequestStatus;
import br.com.matheus.insurance.domain.model.PolicyRequest;
import br.com.matheus.insurance.domain.port.PolicyRequestRepository;
import br.com.matheus.insurance.domain.port.ProcessedMessageRepository;
import br.com.matheus.insurance.infrastructure.messaging.OutboxEventFactory;
import br.com.matheus.insurance.infrastructure.messaging.dto.PaymentProcessedEvent;
import br.com.matheus.insurance.support.PolicyRequestTestFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentProcessedConsumerTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void should_approve_policy_when_payment_is_approved_and_underwriting_already_approved() throws Exception {
        PolicyRequestRepository repository = mock(PolicyRequestRepository.class);
        ProcessedMessageRepository processedMessageRepository = mock(ProcessedMessageRepository.class);
        OutboxEventFactory outboxEventFactory = mock(OutboxEventFactory.class);

        when(processedMessageRepository.exists(anyString(), any(UUID.class))).thenReturn(false);

        PaymentProcessedConsumer consumer =
                new PaymentProcessedConsumer(repository, processedMessageRepository, outboxEventFactory, objectMapper);

        PolicyRequest request = PolicyRequestTestFactory.restoredPendingWithUnderwritingApproved();
        UUID eventId = UUID.randomUUID();
        Instant occurredAt = Instant.parse("2026-03-14T12:00:00Z");

        when(repository.findById(request.getId())).thenReturn(Optional.of(request));

        String rawMessage = toJson(new PaymentProcessedEvent(
                eventId,
                request.getId(),
                "APPROVED",
                occurredAt
        ));

        consumer.consume(rawMessage);

        ArgumentCaptor<PolicyRequest> captor = ArgumentCaptor.forClass(PolicyRequest.class);
        verify(repository).save(captor.capture());

        PolicyRequest saved = captor.getValue();
        assertEquals(PolicyRequestStatus.APPROVED, saved.getStatus());
        assertNotNull(saved.getFinishedAt());

        verify(outboxEventFactory, times(1))
                .appendPolicyStatusChanged(any(PolicyRequest.class), eq(occurredAt));
        verify(processedMessageRepository, times(1))
                .save(anyString(), eq(eventId), eq(occurredAt));
    }

    @Test
    void should_reject_policy_when_payment_is_denied() throws Exception {
        PolicyRequestRepository repository = mock(PolicyRequestRepository.class);
        ProcessedMessageRepository processedMessageRepository = mock(ProcessedMessageRepository.class);
        OutboxEventFactory outboxEventFactory = mock(OutboxEventFactory.class);

        when(processedMessageRepository.exists(anyString(), any(UUID.class))).thenReturn(false);

        PaymentProcessedConsumer consumer =
                new PaymentProcessedConsumer(repository, processedMessageRepository, outboxEventFactory, objectMapper);

        PolicyRequest request = PolicyRequestTestFactory.restoredPendingPolicyRequest();
        UUID eventId = UUID.randomUUID();
        Instant occurredAt = Instant.parse("2026-03-14T12:00:00Z");

        when(repository.findById(request.getId())).thenReturn(Optional.of(request));

        String rawMessage = toJson(new PaymentProcessedEvent(
                eventId,
                request.getId(),
                "DENIED",
                occurredAt
        ));

        consumer.consume(rawMessage);

        ArgumentCaptor<PolicyRequest> captor = ArgumentCaptor.forClass(PolicyRequest.class);
        verify(repository).save(captor.capture());

        PolicyRequest saved = captor.getValue();
        assertEquals(PolicyRequestStatus.REJECTED, saved.getStatus());

        verify(outboxEventFactory, times(1))
                .appendPolicyStatusChanged(any(PolicyRequest.class), eq(occurredAt));
        verify(processedMessageRepository, times(1))
                .save(anyString(), eq(eventId), eq(occurredAt));
    }

    @Test
    void should_ignore_when_event_was_already_processed() {
        PolicyRequestRepository repository = mock(PolicyRequestRepository.class);
        ProcessedMessageRepository processedMessageRepository = mock(ProcessedMessageRepository.class);
        OutboxEventFactory outboxEventFactory = mock(OutboxEventFactory.class);

        when(processedMessageRepository.exists(anyString(), any(UUID.class))).thenReturn(true);

        PaymentProcessedConsumer consumer =
                new PaymentProcessedConsumer(repository, processedMessageRepository, outboxEventFactory, objectMapper);

        consumer.consume("""
                {
                  "eventId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
                  "requestId": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
                  "status": "APPROVED",
                  "occurredAt": "2026-03-14T12:00:00Z"
                }
                """);

        verify(repository, never()).findById(any());
        verify(repository, never()).save(any());
        verifyNoInteractions(outboxEventFactory);
        verify(processedMessageRepository, never()).save(anyString(), any(UUID.class), any());
    }

    @Test
    void should_ignore_business_transition_when_request_is_already_final_but_still_register_processed_message() throws Exception {
        PolicyRequestRepository repository = mock(PolicyRequestRepository.class);
        ProcessedMessageRepository processedMessageRepository = mock(ProcessedMessageRepository.class);
        OutboxEventFactory outboxEventFactory = mock(OutboxEventFactory.class);

        when(processedMessageRepository.exists(anyString(), any(UUID.class))).thenReturn(false);

        PaymentProcessedConsumer consumer =
                new PaymentProcessedConsumer(repository, processedMessageRepository, outboxEventFactory, objectMapper);

        PolicyRequest request = PolicyRequestTestFactory.restoredApprovedPolicyRequest();
        UUID eventId = UUID.randomUUID();
        Instant occurredAt = Instant.parse("2026-03-14T12:00:00Z");

        when(repository.findById(request.getId())).thenReturn(Optional.of(request));

        String rawMessage = toJson(new PaymentProcessedEvent(
                eventId,
                request.getId(),
                "APPROVED",
                occurredAt
        ));

        consumer.consume(rawMessage);

        verify(repository, never()).save(any());
        verifyNoInteractions(outboxEventFactory);
        verify(processedMessageRepository, times(1))
                .save(anyString(), eq(eventId), eq(occurredAt));
    }

    @Test
    void should_throw_runtime_exception_when_request_not_found() throws Exception {
        PolicyRequestRepository repository = mock(PolicyRequestRepository.class);
        ProcessedMessageRepository processedMessageRepository = mock(ProcessedMessageRepository.class);
        OutboxEventFactory outboxEventFactory = mock(OutboxEventFactory.class);

        when(processedMessageRepository.exists(anyString(), any(UUID.class))).thenReturn(false);

        PaymentProcessedConsumer consumer =
                new PaymentProcessedConsumer(repository, processedMessageRepository, outboxEventFactory, objectMapper);

        UUID requestId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        Instant occurredAt = Instant.parse("2026-03-14T12:00:00Z");

        when(repository.findById(requestId)).thenReturn(Optional.empty());

        String rawMessage = toJson(new PaymentProcessedEvent(
                eventId,
                requestId,
                "APPROVED",
                occurredAt
        ));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> consumer.consume(rawMessage));
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause() instanceof IllegalArgumentException);

        verify(repository, never()).save(any());
        verifyNoInteractions(outboxEventFactory);
        verify(processedMessageRepository, never()).save(anyString(), any(UUID.class), any());
    }

    private String toJson(PaymentProcessedEvent event) throws JsonProcessingException {
        return objectMapper.writeValueAsString(event);
    }
}
