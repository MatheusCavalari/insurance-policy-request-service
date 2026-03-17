package br.com.matheus.insurance.domain.rule;

import br.com.matheus.insurance.domain.enums.PolicyCategory;
import br.com.matheus.insurance.domain.enums.RiskClassification;
import br.com.matheus.insurance.domain.model.PolicyRequest;

import java.math.BigDecimal;

public class HighRiskValidationStrategy implements RiskValidationStrategy {

    private static final BigDecimal AUTO_LIMIT = new BigDecimal("250000.00");
    private static final BigDecimal RESIDENTIAL_LIMIT = new BigDecimal("150000.00");
    private static final BigDecimal OTHER_LIMIT = new BigDecimal("125000.00");

    @Override
    public RiskClassification supports() {
        return RiskClassification.HIGH_RISK;
    }

    @Override
    public ValidationDecision validate(PolicyRequest policyRequest) {
        BigDecimal insuredAmount = policyRequest.getInsuredAmount();
        PolicyCategory category = policyRequest.getCategory();

        if (category == PolicyCategory.AUTO) {
            return insuredAmount.compareTo(AUTO_LIMIT) <= 0
                    ? ValidationDecision.accept()
                    : ValidationDecision.reject("high risk customer over auto limit");
        }

        if (category == PolicyCategory.RESIDENTIAL) {
            return insuredAmount.compareTo(RESIDENTIAL_LIMIT) <= 0
                    ? ValidationDecision.accept()
                    : ValidationDecision.reject("high risk customer over residential limit");
        }

        return insuredAmount.compareTo(OTHER_LIMIT) <= 0
                ? ValidationDecision.accept()
                : ValidationDecision.reject("high risk customer over other categories limit");
    }
}