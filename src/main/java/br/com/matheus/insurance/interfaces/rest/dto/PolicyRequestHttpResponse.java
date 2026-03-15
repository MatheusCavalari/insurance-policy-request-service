package br.com.matheus.insurance.interfaces.rest.dto;

import br.com.matheus.insurance.domain.enums.PaymentMethod;
import br.com.matheus.insurance.domain.enums.PolicyCategory;
import br.com.matheus.insurance.domain.enums.PolicyRequestStatus;
import br.com.matheus.insurance.domain.enums.SalesChannel;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record PolicyRequestHttpResponse(
        UUID id,
        UUID customerId,
        Long productId,
        PolicyCategory category,
        SalesChannel salesChannel,
        PaymentMethod paymentMethod,
        PolicyRequestStatus status,
        Instant createdAt,
        Instant finishedAt,
        BigDecimal totalMonthlyPremiumAmount,
        BigDecimal insuredAmount,
        Map<String, BigDecimal> coverages,
        List<String> assistances,
        List<PolicyHistoryHttpResponse> history
) {
}
