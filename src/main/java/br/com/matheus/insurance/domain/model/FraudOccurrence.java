package br.com.matheus.insurance.domain.model;

import java.time.Instant;

public record FraudOccurrence(
        String id,
        Long productId,
        String type,
        String description,
        Instant createdAt,
        Instant updatedAt
) {
}