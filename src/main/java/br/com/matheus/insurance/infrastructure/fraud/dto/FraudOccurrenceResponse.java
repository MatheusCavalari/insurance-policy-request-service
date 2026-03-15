package br.com.matheus.insurance.infrastructure.fraud.dto;

import java.time.Instant;

public record FraudOccurrenceResponse(
        String id,
        Long productId,
        String type,
        String description,
        Instant createdAt,
        Instant updatedAt
) {
}
