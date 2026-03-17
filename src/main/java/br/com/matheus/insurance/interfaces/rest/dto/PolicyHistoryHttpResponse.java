package br.com.matheus.insurance.interfaces.rest.dto;

import br.com.matheus.insurance.domain.enums.PolicyRequestStatus;

import java.time.Instant;

public record PolicyHistoryHttpResponse(
        PolicyRequestStatus status,
        Instant timestamp
) {
}
