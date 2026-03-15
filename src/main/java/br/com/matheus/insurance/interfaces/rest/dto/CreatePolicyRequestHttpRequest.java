package br.com.matheus.insurance.interfaces.rest.dto;

import br.com.matheus.insurance.domain.enums.PaymentMethod;
import br.com.matheus.insurance.domain.enums.PolicyCategory;
import br.com.matheus.insurance.domain.enums.SalesChannel;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record CreatePolicyRequestHttpRequest(
        @NotNull UUID customerId,
        @NotNull Long productId,
        @NotNull PolicyCategory category,
        @NotNull SalesChannel salesChannel,
        @NotNull PaymentMethod paymentMethod,
        @NotNull @DecimalMin("0.0") BigDecimal totalMonthlyPremiumAmount,
        @NotNull @DecimalMin("0.0") BigDecimal insuredAmount,
        @NotNull @NotEmpty Map<String, BigDecimal> coverages,
        @NotNull List<String> assistances
) {
}
