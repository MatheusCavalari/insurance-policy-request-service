package br.com.matheus.insurance.domain.valueobject;

import br.com.matheus.insurance.domain.enums.PolicyRequestStatus;

import java.time.Instant;

public record PolicyHistoryEntry(
        PolicyRequestStatus status,
        Instant timestamp
) {
}