package br.com.matheus.insurance.interfaces.rest.mapper;

import br.com.matheus.insurance.application.dto.CreatePolicyRequestCommand;
import br.com.matheus.insurance.domain.model.PolicyRequest;
import br.com.matheus.insurance.interfaces.rest.dto.CreatePolicyRequestHttpRequest;
import br.com.matheus.insurance.interfaces.rest.dto.PolicyHistoryHttpResponse;
import br.com.matheus.insurance.interfaces.rest.dto.PolicyRequestHttpResponse;
import org.springframework.stereotype.Component;

@Component
public class PolicyRequestHttpMapper {

    public CreatePolicyRequestCommand toCommand(CreatePolicyRequestHttpRequest request) {
        return new CreatePolicyRequestCommand(
                request.customerId(),
                request.productId(),
                request.category(),
                request.salesChannel(),
                request.paymentMethod(),
                request.totalMonthlyPremiumAmount(),
                request.insuredAmount(),
                request.coverages(),
                request.assistances()
        );
    }

    public PolicyRequestHttpResponse toResponse(PolicyRequest policyRequest) {
        return new PolicyRequestHttpResponse(
                policyRequest.getId(),
                policyRequest.getCustomerId(),
                policyRequest.getProductId(),
                policyRequest.getCategory(),
                policyRequest.getSalesChannel(),
                policyRequest.getPaymentMethod(),
                policyRequest.getStatus(),
                policyRequest.getCreatedAt(),
                policyRequest.getFinishedAt(),
                policyRequest.getTotalMonthlyPremiumAmount(),
                policyRequest.getInsuredAmount(),
                policyRequest.getCoverages(),
                policyRequest.getAssistances(),
                policyRequest.getHistory().stream()
                        .map(item -> new PolicyHistoryHttpResponse(item.status(), item.timestamp()))
                        .toList()
        );
    }
}
