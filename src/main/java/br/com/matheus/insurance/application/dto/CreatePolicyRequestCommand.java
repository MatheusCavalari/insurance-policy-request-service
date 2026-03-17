package br.com.matheus.insurance.application.dto;

import br.com.matheus.insurance.domain.enums.PaymentMethod;
import br.com.matheus.insurance.domain.enums.PolicyCategory;
import br.com.matheus.insurance.domain.enums.SalesChannel;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record CreatePolicyRequestCommand(
        UUID customerId,
        Long productId,
        PolicyCategory category,
        SalesChannel salesChannel,
        PaymentMethod paymentMethod,
        BigDecimal totalMonthlyPremiumAmount,
        BigDecimal insuredAmount,
        Map<String, BigDecimal> coverages,
        List<String> assistances
) {
}