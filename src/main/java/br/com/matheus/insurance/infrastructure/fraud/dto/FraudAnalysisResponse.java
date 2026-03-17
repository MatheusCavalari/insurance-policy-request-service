package br.com.matheus.insurance.infrastructure.fraud.dto;

import br.com.matheus.insurance.domain.enums.RiskClassification;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record FraudAnalysisResponse(
        UUID orderId,
        UUID customerId,
        Instant analyzedAt,
        RiskClassification classification,
        List<FraudOccurrenceResponse> occurrences
) {
}
