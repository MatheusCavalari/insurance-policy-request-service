package br.com.matheus.insurance.infrastructure.messaging;

import br.com.matheus.insurance.domain.enums.OutboxEventStatus;
import br.com.matheus.insurance.domain.enums.OutboxEventType;
import br.com.matheus.insurance.domain.port.OutboxEventRepository;
import br.com.matheus.insurance.domain.port.PolicyEventPublisher;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

class OutboxEventPublisherSchedulerTest {

    @Test
    void should_mark_event_as_published_when_publish_succeeds() {
        OutboxEventRepository repository = mock(OutboxEventRepository.class);
        PolicyEventPublisher publisher = mock(PolicyEventPublisher.class);

        OutboxEventPublisherScheduler scheduler = new OutboxEventPublisherScheduler(repository, publisher);

        OutboxEventRepository.OutboxEventRecord event =
                new OutboxEventRepository.OutboxEventRecord(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        OutboxEventType.POLICY_REQUEST_RECEIVED,
                        "{\"any\":\"payload\"}",
                        OutboxEventStatus.PENDING,
                        0,
                        Instant.parse("2026-03-15T10:00:00Z"),
                        null,
                        null
                );

        when(repository.findPending(50)).thenReturn(List.of(event));
        when(publisher.publish(event.eventType(), event.payload()))
                .thenReturn(CompletableFuture.completedFuture(null));

        scheduler.publishPendingEvents();

        verify(publisher).publish(event.eventType(), event.payload());
        verify(repository).markPublished(eq(event.id()), any(Instant.class));
        verify(repository, never()).markFailed(any(), anyString());
    }

    @Test
    void should_mark_event_as_failed_when_publish_throws() {
        OutboxEventRepository repository = mock(OutboxEventRepository.class);
        PolicyEventPublisher publisher = mock(PolicyEventPublisher.class);

        OutboxEventPublisherScheduler scheduler = new OutboxEventPublisherScheduler(repository, publisher);

        OutboxEventRepository.OutboxEventRecord event =
                new OutboxEventRepository.OutboxEventRecord(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        OutboxEventType.POLICY_STATUS_CHANGED,
                        "{\"any\":\"payload\"}",
                        OutboxEventStatus.PENDING,
                        0,
                        Instant.parse("2026-03-15T10:00:00Z"),
                        null,
                        null
                );

        when(repository.findPending(50)).thenReturn(List.of(event));
        when(publisher.publish(event.eventType(), event.payload()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("broker error")));

        scheduler.publishPendingEvents();

        verify(publisher).publish(event.eventType(), event.payload());
        verify(repository).markFailed(eq(event.id()), contains("broker error"));
        verify(repository, never()).markPublished(any(), any());
    }

    @Test
    void should_process_multiple_pending_events() {
        OutboxEventRepository repository = mock(OutboxEventRepository.class);
        PolicyEventPublisher publisher = mock(PolicyEventPublisher.class);

        OutboxEventPublisherScheduler scheduler = new OutboxEventPublisherScheduler(repository, publisher);

        OutboxEventRepository.OutboxEventRecord event1 =
                new OutboxEventRepository.OutboxEventRecord(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        OutboxEventType.POLICY_REQUEST_RECEIVED,
                        "{\"payload\":1}",
                        OutboxEventStatus.PENDING,
                        0,
                        Instant.now(),
                        null,
                        null
                );

        OutboxEventRepository.OutboxEventRecord event2 =
                new OutboxEventRepository.OutboxEventRecord(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        OutboxEventType.POLICY_STATUS_CHANGED,
                        "{\"payload\":2}",
                        OutboxEventStatus.PENDING,
                        0,
                        Instant.now(),
                        null,
                        null
                );

        when(repository.findPending(50)).thenReturn(List.of(event1, event2));
        when(publisher.publish(any(), any())).thenReturn(CompletableFuture.completedFuture(null));

        scheduler.publishPendingEvents();

        verify(publisher, times(2)).publish(any(), any());
        verify(repository, times(2)).markPublished(any(), any());
        verify(repository, never()).markFailed(any(), anyString());
    }
}