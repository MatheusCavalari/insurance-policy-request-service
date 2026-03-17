package br.com.matheus.insurance.domain.rule;

import br.com.matheus.insurance.domain.enums.PolicyCategory;
import br.com.matheus.insurance.domain.enums.RiskClassification;
import br.com.matheus.insurance.support.PolicyRequestTestFactory;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class HighRiskValidationStrategyTest {

    private final HighRiskValidationStrategy strategy = new HighRiskValidationStrategy();

    @Test
    void should_support_high_risk() {
        assertEquals(RiskClassification.HIGH_RISK, strategy.supports());
    }

    @Test
    void should_approve_auto_when_equal_to_250k() {
        var request = PolicyRequestTestFactory.newPolicyRequest(PolicyCategory.AUTO, new BigDecimal("250000.00"));

        ValidationDecision decision = strategy.validate(request);

        assertTrue(decision.approved());
    }

    @Test
    void should_reject_auto_when_greater_than_250k() {
        var request = PolicyRequestTestFactory.newPolicyRequest(PolicyCategory.AUTO, new BigDecimal("250000.01"));

        ValidationDecision decision = strategy.validate(request);

        assertFalse(decision.approved());
    }

    @Test
    void should_approve_residential_when_equal_to_150k() {
        var request = PolicyRequestTestFactory.newPolicyRequest(PolicyCategory.RESIDENTIAL, new BigDecimal("150000.00"));

        ValidationDecision decision = strategy.validate(request);

        assertTrue(decision.approved());
    }

    @Test
    void should_reject_residential_when_greater_than_150k() {
        var request = PolicyRequestTestFactory.newPolicyRequest(PolicyCategory.RESIDENTIAL, new BigDecimal("150000.01"));

        ValidationDecision decision = strategy.validate(request);

        assertFalse(decision.approved());
    }

    @Test
    void should_approve_other_when_equal_to_125k() {
        var request = PolicyRequestTestFactory.newPolicyRequest(PolicyCategory.BUSINESS, new BigDecimal("125000.00"));

        ValidationDecision decision = strategy.validate(request);

        assertTrue(decision.approved());
    }

    @Test
    void should_reject_other_when_greater_than_125k() {
        var request = PolicyRequestTestFactory.newPolicyRequest(PolicyCategory.BUSINESS, new BigDecimal("125000.01"));

        ValidationDecision decision = strategy.validate(request);

        assertFalse(decision.approved());
    }
}