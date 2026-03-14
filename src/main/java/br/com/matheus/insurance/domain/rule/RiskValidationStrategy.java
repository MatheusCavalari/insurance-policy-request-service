package br.com.matheus.insurance.domain.rule;

import br.com.matheus.insurance.domain.enums.RiskClassification;
import br.com.matheus.insurance.domain.model.PolicyRequest;

public interface RiskValidationStrategy {
    RiskClassification supports();
    ValidationDecision validate(PolicyRequest policyRequest);
}