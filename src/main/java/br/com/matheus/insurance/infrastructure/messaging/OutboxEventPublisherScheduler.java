package br.com.matheus.insurance.infrastructure.messaging;

import br.com.matheus.insurance.domain.port.OutboxEventRepository;
import br.com.matheus.insurance.domain.port.PolicyEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class OutboxEventPublisherScheduler {

    private final OutboxEventRepository outboxEventRepository;
    private final PolicyEventPublisher policyEventPublisher;

    public OutboxEventPublisherScheduler(
            OutboxEventRepository outboxEventRepository,
            PolicyEventPublisher policyEventPublisher
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.policyEventPublisher = policyEventPublisher;
    }

    @Scheduled(fixedDelay = 3000)
    public void publishPendingEvents() {
        outboxEventRepository.findPending(50).forEach(event -> {
            try {
                policyEventPublisher.publish(event.eventType(), event.payload()).join();
                outboxEventRepository.markPublished(event.id(), Instant.now());
            } catch (Exception ex) {
                outboxEventRepository.markFailed(event.id(), ex.getMessage());
            }
        });
    }
}
