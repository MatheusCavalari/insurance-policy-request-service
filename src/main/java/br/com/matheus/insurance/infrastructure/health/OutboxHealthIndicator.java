package br.com.matheus.insurance.infrastructure.health;

import br.com.matheus.insurance.domain.enums.OutboxEventStatus;
import br.com.matheus.insurance.infrastructure.persistence.repository.SpringDataOutboxEventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("outbox")
public class OutboxHealthIndicator implements HealthIndicator {

    private final SpringDataOutboxEventRepository repository;
    private final long pendingThreshold;

    public OutboxHealthIndicator(
            SpringDataOutboxEventRepository repository,
            @Value("${app.health.outbox.pending-threshold:100}") long pendingThreshold
    ) {
        this.repository = repository;
        this.pendingThreshold = pendingThreshold;
    }

    @Override
    public Health health() {
        long pending = repository.countByStatus(OutboxEventStatus.PENDING);
        long failed = repository.countByStatus(OutboxEventStatus.FAILED);

        if (failed > 0 || pending > pendingThreshold) {
            return Health.down()
                    .withDetail("pending", pending)
                    .withDetail("failed", failed)
                    .withDetail("pendingThreshold", pendingThreshold)
                    .build();
        }

        return Health.up()
                .withDetail("pending", pending)
                .withDetail("failed", failed)
                .build();
    }
}