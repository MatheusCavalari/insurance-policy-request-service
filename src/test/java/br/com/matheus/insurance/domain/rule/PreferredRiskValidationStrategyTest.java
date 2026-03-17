package br.com.matheus.insurance.domain.rule;

import br.com.matheus.insurance.domain.enums.PolicyCategory;
import br.com.matheus.insurance.domain.enums.RiskClassification;
import br.com.matheus.insurance.support.PolicyRequestTestFactory;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PreferredRiskValidationStrategyTest {

    private final PreferredRiskValidationStrategy strategy = new PreferredRiskValidationStrategy();

    @Test
    void should_support_preferred() {
        assertEquals(RiskClassification.PREFERRED, strategy.supports());
    }

    @Test
    void should_approve_life_when_less_than_800k() {
        var request = PolicyRequestTestFactory.newPolicyRequest(PolicyCategory.LIFE, new BigDecimal("799999.99"));

        ValidationDecision decision = strategy.validate(request);

        assertTrue(decision.approved());
    }

    @Test
    void should_reject_life_when_equal_to_800k() {
        var request = PolicyRequestTestFactory.newPolicyRequest(PolicyCategory.LIFE, new BigDecimal("800000.00"));

        ValidationDecision decision = strategy.validate(request);

        assertFalse(decision.approved());
    }

    @Test
    void should_approve_auto_when_less_than_450k() {
        var request = PolicyRequestTestFactory.newPolicyRequest(PolicyCategory.AUTO, new BigDecimal("449999.99"));

        ValidationDecision decision = strategy.validate(request);

        assertTrue(decision.approved());
    }

    @Test
    void should_reject_auto_when_equal_to_450k() {
        var request = PolicyRequestTestFactory.newPolicyRequest(PolicyCategory.AUTO, new BigDecimal("450000.00"));

        ValidationDecision decision = strategy.validate(request);

        assertFalse(decision.approved());
    }

    @Test
    void should_approve_residential_when_less_than_450k() {
        var request = PolicyRequestTestFactory.newPolicyRequest(PolicyCategory.RESIDENTIAL, new BigDecimal("449999.99"));

        ValidationDecision decision = strategy.validate(request);

        assertTrue(decision.approved());
    }

    @Test
    void should_approve_other_when_equal_to_375k() {
        var request = PolicyRequestTestFactory.newPolicyRequest(PolicyCategory.BUSINESS, new BigDecimal("375000.00"));

        ValidationDecision decision = strategy.validate(request);

        assertTrue(decision.approved());
    }

    @Test
    void should_reject_other_when_greater_than_375k() {
        var request = PolicyRequestTestFactory.newPolicyRequest(PolicyCategory.BUSINESS, new BigDecimal("375000.01"));

        ValidationDecision decision = strategy.validate(request);

        assertFalse(decision.approved());
    }
}