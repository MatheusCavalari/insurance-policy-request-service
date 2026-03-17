package br.com.matheus.insurance.infrastructure.health;

import br.com.matheus.insurance.domain.enums.OutboxEventStatus;
import br.com.matheus.insurance.infrastructure.persistence.repository.SpringDataOutboxEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OutboxHealthIndicatorTest {

    @Test
    void should_return_up_when_pending_and_failed_are_under_control() {
        SpringDataOutboxEventRepository repository = mock(SpringDataOutboxEventRepository.class);
        when(repository.countByStatus(OutboxEventStatus.PENDING)).thenReturn(10L);
        when(repository.countByStatus(OutboxEventStatus.FAILED)).thenReturn(0L);

        OutboxHealthIndicator indicator = new OutboxHealthIndicator(repository, 100L);

        Health health = indicator.health();

        assertEquals("UP", health.getStatus().getCode());
        assertEquals(10L, health.getDetails().get("pending"));
        assertEquals(0L, health.getDetails().get("failed"));
    }

    @Test
    void should_return_down_when_failed_events_exist() {
        SpringDataOutboxEventRepository repository = mock(SpringDataOutboxEventRepository.class);
        when(repository.countByStatus(OutboxEventStatus.PENDING)).thenReturn(1L);
        when(repository.countByStatus(OutboxEventStatus.FAILED)).thenReturn(2L);

        OutboxHealthIndicator indicator = new OutboxHealthIndicator(repository, 100L);

        Health health = indicator.health();

        assertEquals("DOWN", health.getStatus().getCode());
        assertEquals(2L, health.getDetails().get("failed"));
    }

    @Test
    void should_return_down_when_pending_exceeds_threshold() {
        SpringDataOutboxEventRepository repository = mock(SpringDataOutboxEventRepository.class);
        when(repository.countByStatus(OutboxEventStatus.PENDING)).thenReturn(101L);
        when(repository.countByStatus(OutboxEventStatus.FAILED)).thenReturn(0L);

        OutboxHealthIndicator indicator = new OutboxHealthIndicator(repository, 100L);

        Health health = indicator.health();

        assertEquals("DOWN", health.getStatus().getCode());
        assertEquals(101L, health.getDetails().get("pending"));
        assertEquals(100L, health.getDetails().get("pendingThreshold"));
    }
}