package br.com.matheus.insurance.domain.model;

import br.com.matheus.insurance.domain.enums.RiskClassification;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record FraudAnalysis(
        UUID orderId,
        UUID customerId,
        Instant analyzedAt,
        RiskClassification classification,
        List<FraudOccurrence> occurrences
) {
}