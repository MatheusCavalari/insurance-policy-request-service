package br.com.matheus.insurance.infrastructure.persistence;

import br.com.matheus.insurance.domain.enums.OutboxEventStatus;
import br.com.matheus.insurance.domain.enums.OutboxEventType;
import br.com.matheus.insurance.domain.port.OutboxEventRepository;
import br.com.matheus.insurance.infrastructure.persistence.entity.OutboxEventJpaEntity;
import br.com.matheus.insurance.infrastructure.persistence.repository.SpringDataOutboxEventRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
@Transactional
public class OutboxEventRepositoryImpl implements OutboxEventRepository {

    private final SpringDataOutboxEventRepository repository;

    public OutboxEventRepositoryImpl(SpringDataOutboxEventRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(UUID id, UUID aggregateId, OutboxEventType eventType, String payload, OutboxEventStatus status, Instant createdAt) {
        OutboxEventJpaEntity entity = new OutboxEventJpaEntity();
        entity.setId(id);
        entity.setAggregateId(aggregateId);
        entity.setEventType(eventType);
        entity.setPayload(payload);
        entity.setStatus(status);
        entity.setRetries(0);
        entity.setCreatedAt(createdAt);
        repository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutboxEventRecord> findPending(int limit) {
        return repository.findTop50ByStatusOrderByCreatedAtAsc(OutboxEventStatus.PENDING)
                .stream()
                .limit(limit)
                .map(entity -> new OutboxEventRecord(
                        entity.getId(),
                        entity.getAggregateId(),
                        entity.getEventType(),
                        entity.getPayload(),
                        entity.getStatus(),
                        entity.getRetries(),
                        entity.getCreatedAt(),
                        entity.getPublishedAt(),
                        entity.getErrorMessage()
                ))
                .toList();
    }

    @Override
    public void markPublished(UUID id, Instant publishedAt) {
        OutboxEventJpaEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("outbox event not found: " + id));
        entity.setStatus(OutboxEventStatus.PUBLISHED);
        entity.setPublishedAt(publishedAt);
        entity.setErrorMessage(null);
        repository.save(entity);
    }

    @Override
    public void markFailed(UUID id, String errorMessage) {
        OutboxEventJpaEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("outbox event not found: " + id));
        entity.setStatus(OutboxEventStatus.FAILED);
        entity.setErrorMessage(errorMessage);
        entity.setRetries(entity.getRetries() + 1);
        repository.save(entity);
    }
}
