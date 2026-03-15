package br.com.matheus.insurance.domain.port;

import br.com.matheus.insurance.domain.enums.OutboxEventStatus;
import br.com.matheus.insurance.domain.enums.OutboxEventType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository {

    void save(
            UUID id,
            UUID aggregateId,
            OutboxEventType eventType,
            String payload,
            OutboxEventStatus status,
            Instant createdAt
    );

    List<OutboxEventRecord> findPending(int limit);

    void markPublished(UUID id, Instant publishedAt);

    void markFailed(UUID id, String errorMessage);

    record OutboxEventRecord(
            UUID id,
            UUID aggregateId,
            OutboxEventType eventType,
            String payload,
            OutboxEventStatus status,
            Integer retries,
            Instant createdAt,
            Instant publishedAt,
            String errorMessage
    ) {
    }
}
