package br.com.matheus.insurance.infrastructure.messaging.dto;

import br.com.matheus.insurance.domain.enums.PolicyRequestStatus;

import java.time.Instant;
import java.util.UUID;

public record PolicyRequestEvent(
        UUID eventId,
        UUID requestId,
        UUID customerId,
        PolicyRequestStatus status,
        Instant occurredAt
) {
}
