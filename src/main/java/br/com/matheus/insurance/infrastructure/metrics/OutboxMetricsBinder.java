package br.com.matheus.insurance.infrastructure.metrics;

import br.com.matheus.insurance.domain.enums.OutboxEventStatus;
import br.com.matheus.insurance.infrastructure.persistence.repository.SpringDataOutboxEventRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.stereotype.Component;

@Component
public class OutboxMetricsBinder implements MeterBinder {

    private final SpringDataOutboxEventRepository repository;

    public OutboxMetricsBinder(SpringDataOutboxEventRepository repository) {
        this.repository = repository;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        Gauge.builder("insurance.outbox.pending", repository,
                        repo -> repo.countByStatus(OutboxEventStatus.PENDING))
                .description("Number of pending outbox events")
                .register(registry);

        Gauge.builder("insurance.outbox.failed", repository,
                        repo -> repo.countByStatus(OutboxEventStatus.FAILED))
                .description("Number of failed outbox events")
                .register(registry);
    }
}