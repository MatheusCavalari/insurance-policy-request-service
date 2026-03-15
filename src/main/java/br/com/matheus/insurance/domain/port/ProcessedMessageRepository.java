package br.com.matheus.insurance.domain.port;

import java.time.Instant;
import java.util.UUID;

public interface ProcessedMessageRepository {

    boolean exists(String consumerName, UUID eventId);

    void save(String consumerName, UUID eventId, Instant processedAt);
}
