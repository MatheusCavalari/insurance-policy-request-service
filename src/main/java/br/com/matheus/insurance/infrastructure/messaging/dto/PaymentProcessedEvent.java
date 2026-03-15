package br.com.matheus.insurance.infrastructure.messaging.dto;

import java.time.Instant;
import java.util.UUID;

public record PaymentProcessedEvent(
        UUID requestId,
        String status,
        Instant occurredAt
) {
}
