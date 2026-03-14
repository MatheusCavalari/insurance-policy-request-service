package br.com.matheus.insurance.domain.rule;

import br.com.matheus.insurance.domain.enums.PolicyCategory;
import br.com.matheus.insurance.domain.enums.RiskClassification;
import br.com.matheus.insurance.support.PolicyRequestTestFactory;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class NoInformationRiskValidationStrategyTest {

    private final NoInformationRiskValidationStrategy strategy = new NoInformationRiskValidationStrategy();

    @Test
    void should_support_no_information() {
        assertEquals(RiskClassification.NO_INFORMATION, strategy.supports());
    }

    @Test
    void should_approve_life_when_equal_to_200k() {
        var request = PolicyRequestTestFactory.newPolicyRequest(PolicyCategory.LIFE, new BigDecimal("200000.00"));

        ValidationDecision decision = strategy.validate(request);

        assertTrue(decision.approved());
    }

    @Test
    void should_reject_life_when_greater_than_200k() {
        var request = PolicyRequestTestFactory.newPolicyRequest(PolicyCategory.LIFE, new BigDecimal("200000.01"));

        ValidationDecision decision = strategy.validate(request);

        assertFalse(decision.approved());
    }

    @Test
    void should_approve_auto_when_equal_to_75k() {
        var request = PolicyRequestTestFactory.newPolicyRequest(PolicyCategory.AUTO, new BigDecimal("75000.00"));

        ValidationDecision decision = strategy.validate(request);

        assertTrue(decision.approved());
    }

    @Test
    void should_reject_auto_when_greater_than_75k() {
        var request = PolicyRequestTestFactory.newPolicyRequest(PolicyCategory.AUTO, new BigDecimal("75000.01"));

        ValidationDecision decision = strategy.validate(request);

        assertFalse(decision.approved());
    }

    @Test
    void should_approve_other_when_equal_to_55k() {
        var request = PolicyRequestTestFactory.newPolicyRequest(PolicyCategory.BUSINESS, new BigDecimal("55000.00"));

        ValidationDecision decision = strategy.validate(request);

        assertTrue(decision.approved());
    }

    @Test
    void should_reject_other_when_greater_than_55k() {
        var request = PolicyRequestTestFactory.newPolicyRequest(PolicyCategory.BUSINESS, new BigDecimal("55000.01"));

        ValidationDecision decision = strategy.validate(request);

        assertFalse(decision.approved());
    }
}