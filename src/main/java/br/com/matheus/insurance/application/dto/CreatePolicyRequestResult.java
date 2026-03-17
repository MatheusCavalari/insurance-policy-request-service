package br.com.matheus.insurance.application.dto;

import br.com.matheus.insurance.domain.enums.PolicyRequestStatus;

import java.time.Instant;
import java.util.UUID;

public record CreatePolicyRequestResult(
        UUID id,
        PolicyRequestStatus status,
        Instant createdAt
) {
}