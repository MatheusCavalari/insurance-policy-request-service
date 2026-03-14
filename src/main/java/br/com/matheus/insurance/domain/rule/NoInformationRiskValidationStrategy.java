package br.com.matheus.insurance.domain.rule;

import br.com.matheus.insurance.domain.enums.PolicyCategory;
import br.com.matheus.insurance.domain.enums.RiskClassification;
import br.com.matheus.insurance.domain.model.PolicyRequest;

import java.math.BigDecimal;

public class NoInformationRiskValidationStrategy implements RiskValidationStrategy {

    private static final BigDecimal LIFE_OR_RESIDENTIAL_LIMIT = new BigDecimal("200000.00");
    private static final BigDecimal AUTO_LIMIT = new BigDecimal("75000.00");
    private static final BigDecimal OTHER_LIMIT = new BigDecimal("55000.00");

    @Override
    public RiskClassification supports() {
        return RiskClassification.NO_INFORMATION;
    }

    @Override
    public ValidationDecision validate(PolicyRequest policyRequest) {
        BigDecimal insuredAmount = policyRequest.getInsuredAmount();
        PolicyCategory category = policyRequest.getCategory();

        if (category == PolicyCategory.LIFE || category == PolicyCategory.RESIDENTIAL) {
            return insuredAmount.compareTo(LIFE_OR_RESIDENTIAL_LIMIT) <= 0
                    ? ValidationDecision.accept()
                    : ValidationDecision.reject("no information customer over life/residential limit");
        }

        if (category == PolicyCategory.AUTO) {
            return insuredAmount.compareTo(AUTO_LIMIT) <= 0
                    ? ValidationDecision.accept()
                    : ValidationDecision.reject("no information customer over auto limit");
        }

        return insuredAmount.compareTo(OTHER_LIMIT) <= 0
                ? ValidationDecision.accept()
                : ValidationDecision.reject("no information customer over other categories limit");
    }
}