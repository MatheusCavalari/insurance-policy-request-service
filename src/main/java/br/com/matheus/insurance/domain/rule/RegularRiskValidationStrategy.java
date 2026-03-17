package br.com.matheus.insurance.domain.rule;

import br.com.matheus.insurance.domain.enums.PolicyCategory;
import br.com.matheus.insurance.domain.enums.RiskClassification;
import br.com.matheus.insurance.domain.model.PolicyRequest;

import java.math.BigDecimal;

public class RegularRiskValidationStrategy implements RiskValidationStrategy {

    @Override
    public RiskClassification supports() {
        return RiskClassification.REGULAR;
    }

    @Override
    public ValidationDecision validate(PolicyRequest policyRequest) {
        BigDecimal insuredAmount = policyRequest.getInsuredAmount();
        PolicyCategory category = policyRequest.getCategory();

        if (category == PolicyCategory.LIFE || category == PolicyCategory.RESIDENTIAL) {
            return insuredAmount.compareTo(new BigDecimal("500000.00")) <= 0
                    ? ValidationDecision.accept()
                    : ValidationDecision.reject("regular customer over life/residential limit");
        }

        if (category == PolicyCategory.AUTO) {
            return insuredAmount.compareTo(new BigDecimal("350000.00")) <= 0
                    ? ValidationDecision.accept()
                    : ValidationDecision.reject("regular customer over auto limit");
        }

        return insuredAmount.compareTo(new BigDecimal("255000.00")) <= 0
                ? ValidationDecision.accept()
                : ValidationDecision.reject("regular customer over other categories limit");
    }
}