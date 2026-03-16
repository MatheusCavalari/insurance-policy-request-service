package br.com.matheus.insurance.infrastructure.persistence;

import br.com.matheus.insurance.domain.enums.OutboxEventStatus;
import br.com.matheus.insurance.domain.enums.OutboxEventType;
import br.com.matheus.insurance.domain.exception.ResourceNotFoundException;
import br.com.matheus.insurance.domain.port.OutboxEventRepository;
import br.com.matheus.insurance.support.AbstractPostgresContainerSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers(disabledWithoutDocker = true)
@DataJpaTest
@Import(OutboxEventRepositoryImpl.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OutboxEventRepositoryIT extends AbstractPostgresContainerSupport {

    @Autowired
    private OutboxEventRepositoryImpl repository;

    @Test
    void should_save_and_find_pending_events() {
        UUID aggregateId1 = UUID.randomUUID();
        UUID aggregateId2 = UUID.randomUUID();

        repository.save(
                UUID.randomUUID(),
                aggregateId1,
                OutboxEventType.POLICY_REQUEST_RECEIVED,
                "{\"event\":\"received-1\"}",
                OutboxEventStatus.PENDING,
                Instant.parse("2026-03-15T10:00:00Z")
        );

        repository.save(
                UUID.randomUUID(),
                aggregateId2,
                OutboxEventType.POLICY_STATUS_CHANGED,
                "{\"event\":\"status-2\"}",
                OutboxEventStatus.PENDING,
                Instant.parse("2026-03-15T10:01:00Z")
        );

        List<OutboxEventRepository.OutboxEventRecord> pending = repository.findPending(50);

        assertEquals(2, pending.size());
        assertEquals(aggregateId1, pending.get(0).aggregateId());
        assertEquals(aggregateId2, pending.get(1).aggregateId());
        assertEquals(OutboxEventStatus.PENDING, pending.get(0).status());
        assertEquals(OutboxEventStatus.PENDING, pending.get(1).status());
    }

    @Test
    void should_respect_limit_when_finding_pending_events() {
        repository.save(
                UUID.randomUUID(),
                UUID.randomUUID(),
                OutboxEventType.POLICY_REQUEST_RECEIVED,
                "{\"event\":\"1\"}",
                OutboxEventStatus.PENDING,
                Instant.parse("2026-03-15T10:00:00Z")
        );

        repository.save(
                UUID.randomUUID(),
                UUID.randomUUID(),
                OutboxEventType.POLICY_REQUEST_RECEIVED,
                "{\"event\":\"2\"}",
                OutboxEventStatus.PENDING,
                Instant.parse("2026-03-15T10:01:00Z")
        );

        repository.save(
                UUID.randomUUID(),
                UUID.randomUUID(),
                OutboxEventType.POLICY_STATUS_CHANGED,
                "{\"event\":\"3\"}",
                OutboxEventStatus.PENDING,
                Instant.parse("2026-03-15T10:02:00Z")
        );

        List<OutboxEventRepository.OutboxEventRecord> pending = repository.findPending(2);

        assertEquals(2, pending.size());
        assertEquals("{\"event\":\"1\"}", pending.get(0).payload());
        assertEquals("{\"event\":\"2\"}", pending.get(1).payload());
    }

    @Test
    void should_mark_event_as_published() {
        UUID eventId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-03-15T10:00:00Z");
        Instant publishedAt = Instant.parse("2026-03-15T10:05:00Z");

        repository.save(
                eventId,
                UUID.randomUUID(),
                OutboxEventType.POLICY_REQUEST_RECEIVED,
                "{\"event\":\"publish\"}",
                OutboxEventStatus.PENDING,
                createdAt
        );

        repository.markPublished(eventId, publishedAt);

        List<OutboxEventRepository.OutboxEventRecord> pending = repository.findPending(50);
        assertTrue(pending.isEmpty());
    }

    @Test
    void should_mark_event_as_failed_and_increment_retries() {
        UUID eventId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-03-15T10:00:00Z");

        repository.save(
                eventId,
                UUID.randomUUID(),
                OutboxEventType.POLICY_STATUS_CHANGED,
                "{\"event\":\"fail\"}",
                OutboxEventStatus.PENDING,
                createdAt
        );

        repository.markFailed(eventId, "broker unavailable");

        List<OutboxEventRepository.OutboxEventRecord> pending = repository.findPending(50);
        assertTrue(pending.isEmpty());
    }

    @Test
    void should_throw_when_marking_published_for_non_existing_event() {
        UUID unknownId = UUID.randomUUID();

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> repository.markPublished(unknownId, Instant.parse("2026-03-15T10:05:00Z"))
        );

        assertTrue(exception.getMessage().contains("outbox event not found"));
    }

    @Test
    void should_throw_when_marking_failed_for_non_existing_event() {
        UUID unknownId = UUID.randomUUID();

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> repository.markFailed(unknownId, "error")
        );

        assertTrue(exception.getMessage().contains("outbox event not found"));
    }
}