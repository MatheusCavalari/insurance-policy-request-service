package br.com.matheus.insurance.domain.rule;

import br.com.matheus.insurance.domain.enums.PolicyCategory;
import br.com.matheus.insurance.support.PolicyRequestTestFactory;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class RegularRiskValidationStrategyTest {

    private final RegularRiskValidationStrategy strategy = new RegularRiskValidationStrategy();

    @Test
    void should_support_regular() {
        assertEquals(br.com.matheus.insurance.domain.enums.RiskClassification.REGULAR, strategy.supports());
    }

    @Test
    void should_approve_life_when_equal_to_500k() {
        var request = PolicyRequestTestFactory.newPolicyRequest(PolicyCategory.LIFE, new BigDecimal("500000.00"));

        ValidationDecision decision = strategy.validate(request);

        assertTrue(decision.approved());
    }

    @Test
    void should_reject_life_when_greater_than_500k() {
        var request = PolicyRequestTestFactory.newPolicyRequest(PolicyCategory.LIFE, new BigDecimal("500000.01"));

        ValidationDecision decision = strategy.validate(request);

        assertFalse(decision.approved());
    }

    @Test
    void should_approve_auto_when_equal_to_350k() {
        var request = PolicyRequestTestFactory.newPolicyRequest(PolicyCategory.AUTO, new BigDecimal("350000.00"));

        ValidationDecision decision = strategy.validate(request);

        assertTrue(decision.approved());
    }

    @Test
    void should_reject_auto_when_greater_than_350k() {
        var request = PolicyRequestTestFactory.newPolicyRequest(PolicyCategory.AUTO, new BigDecimal("350000.01"));

        ValidationDecision decision = strategy.validate(request);

        assertFalse(decision.approved());
    }

    @Test
    void should_approve_other_when_equal_to_255k() {
        var request = PolicyRequestTestFactory.newPolicyRequest(PolicyCategory.BUSINESS, new BigDecimal("255000.00"));

        ValidationDecision decision = strategy.validate(request);

        assertTrue(decision.approved());
    }

    @Test
    void should_reject_other_when_greater_than_255k() {
        var request = PolicyRequestTestFactory.newPolicyRequest(PolicyCategory.BUSINESS, new BigDecimal("255000.01"));

        ValidationDecision decision = strategy.validate(request);

        assertFalse(decision.approved());
    }
}