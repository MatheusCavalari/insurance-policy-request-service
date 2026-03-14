package br.com.matheus.insurance.domain.rule;

import br.com.matheus.insurance.domain.enums.PolicyCategory;
import br.com.matheus.insurance.domain.enums.RiskClassification;
import br.com.matheus.insurance.domain.model.PolicyRequest;

import java.math.BigDecimal;

public class PreferredRiskValidationStrategy implements RiskValidationStrategy {

    @Override
    public RiskClassification supports() {
        return RiskClassification.PREFERRED;
    }

    @Override
    public ValidationDecision validate(PolicyRequest policyRequest) {
        BigDecimal insuredAmount = policyRequest.getInsuredAmount();
        PolicyCategory category = policyRequest.getCategory();

        if (category == PolicyCategory.LIFE) {
            return insuredAmount.compareTo(new BigDecimal("800000.00")) < 0
                    ? ValidationDecision.accept()
                    : ValidationDecision.reject("preferred customer over life limit");
        }

        if (category == PolicyCategory.AUTO || category == PolicyCategory.RESIDENTIAL) {
            return insuredAmount.compareTo(new BigDecimal("450000.00")) < 0
                    ? ValidationDecision.accept()
                    : ValidationDecision.reject("preferred customer over auto/residential limit");
        }

        return insuredAmount.compareTo(new BigDecimal("375000.00")) <= 0
                ? ValidationDecision.accept()
                : ValidationDecision.reject("preferred customer over other categories limit");
    }
}