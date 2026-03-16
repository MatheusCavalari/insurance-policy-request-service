package br.com.matheus.insurance.infrastructure.messaging;

import br.com.matheus.insurance.domain.enums.OutboxEventStatus;
import br.com.matheus.insurance.domain.enums.OutboxEventType;
import br.com.matheus.insurance.domain.model.PolicyRequest;
import br.com.matheus.insurance.domain.port.OutboxEventRepository;
import br.com.matheus.insurance.support.PolicyRequestTestFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OutboxEventFactoryTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void should_append_policy_request_received_event() {
        OutboxEventRepository repository = mock(OutboxEventRepository.class);
        OutboxEventFactory factory = new OutboxEventFactory(objectMapper, repository);

        PolicyRequest request = PolicyRequestTestFactory.newPolicyRequest();
        Instant occurredAt = Instant.parse("2026-03-15T10:00:00Z");

        factory.appendPolicyRequestReceived(request, occurredAt);

        ArgumentCaptor<UUID> idCaptor = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<UUID> aggregateCaptor = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<OutboxEventType> typeCaptor = ArgumentCaptor.forClass(OutboxEventType.class);
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<OutboxEventStatus> statusCaptor = ArgumentCaptor.forClass(OutboxEventStatus.class);
        ArgumentCaptor<Instant> createdAtCaptor = ArgumentCaptor.forClass(Instant.class);

        verify(repository).save(
                idCaptor.capture(),
                aggregateCaptor.capture(),
                typeCaptor.capture(),
                payloadCaptor.capture(),
                statusCaptor.capture(),
                createdAtCaptor.capture()
        );

        assertNotNull(idCaptor.getValue());
        assertEquals(request.getId(), aggregateCaptor.getValue());
        assertEquals(OutboxEventType.POLICY_REQUEST_RECEIVED, typeCaptor.getValue());
        assertEquals(OutboxEventStatus.PENDING, statusCaptor.getValue());
        assertEquals(occurredAt, createdAtCaptor.getValue());

        String payload = payloadCaptor.getValue();
        assertTrue(payload.contains(request.getId().toString()));
        assertTrue(payload.contains(request.getCustomerId().toString()));
        assertTrue(payload.contains(request.getStatus().name()));
    }

    @Test
    void should_append_policy_status_changed_event() {
        OutboxEventRepository repository = mock(OutboxEventRepository.class);
        OutboxEventFactory factory = new OutboxEventFactory(objectMapper, repository);

        PolicyRequest request = PolicyRequestTestFactory.restoredApprovedPolicyRequest();
        Instant occurredAt = Instant.parse("2026-03-15T10:05:00Z");

        factory.appendPolicyStatusChanged(request, occurredAt);

        ArgumentCaptor<OutboxEventType> typeCaptor = ArgumentCaptor.forClass(OutboxEventType.class);
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);

        verify(repository).save(
                any(UUID.class),
                eq(request.getId()),
                typeCaptor.capture(),
                payloadCaptor.capture(),
                eq(OutboxEventStatus.PENDING),
                eq(occurredAt)
        );

        assertEquals(OutboxEventType.POLICY_STATUS_CHANGED, typeCaptor.getValue());
        assertTrue(payloadCaptor.getValue().contains("APPROVED"));
    }

    @Test
    void should_throw_when_serialization_fails() throws Exception {
        ObjectMapper failingMapper = mock(ObjectMapper.class);
        OutboxEventRepository repository = mock(OutboxEventRepository.class);
        OutboxEventFactory factory = new OutboxEventFactory(failingMapper, repository);

        PolicyRequest request = PolicyRequestTestFactory.newPolicyRequest();
        Instant occurredAt = Instant.parse("2026-03-15T10:00:00Z");

        when(failingMapper.writeValueAsString(any()))
                .thenThrow(new JsonProcessingException("serialization error") {});

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> factory.appendPolicyRequestReceived(request, occurredAt)
        );

        assertEquals("error serializing outbox payload", exception.getMessage());
        assertNotNull(exception.getCause());
        verifyNoInteractions(repository);
    }
}