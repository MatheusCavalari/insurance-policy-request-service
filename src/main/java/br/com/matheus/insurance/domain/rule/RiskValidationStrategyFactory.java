package br.com.matheus.insurance.domain.rule;

import br.com.matheus.insurance.domain.enums.RiskClassification;
import br.com.matheus.insurance.domain.exception.ResourceNotFoundException;

import java.util.List;

public class RiskValidationStrategyFactory {

    private final List<RiskValidationStrategy> strategies;

    public RiskValidationStrategyFactory(List<RiskValidationStrategy> strategies) {
        this.strategies = strategies;
    }

    public RiskValidationStrategy get(RiskClassification classification) {
        return strategies.stream()
                .filter(strategy -> strategy.supports() == classification)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No validation strategy found for classification: " + classification
                ));
    }
}