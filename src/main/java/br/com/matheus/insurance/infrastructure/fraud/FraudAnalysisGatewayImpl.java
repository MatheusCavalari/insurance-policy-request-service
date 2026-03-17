package br.com.matheus.insurance.infrastructure.fraud;

import br.com.matheus.insurance.domain.model.FraudAnalysis;
import br.com.matheus.insurance.domain.model.FraudOccurrence;
import br.com.matheus.insurance.domain.model.PolicyRequest;
import br.com.matheus.insurance.domain.port.FraudAnalysisGateway;
import br.com.matheus.insurance.infrastructure.fraud.dto.FraudAnalysisResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class FraudAnalysisGatewayImpl implements FraudAnalysisGateway {

    private final RestClient restClient;

    public FraudAnalysisGatewayImpl(
            @Value("${fraud.base-url}") String baseUrl
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    public FraudAnalysis analyze(PolicyRequest policyRequest) {
        FraudAnalysisResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/fraud-analysis/{policyRequestId}")
                        .queryParam("customerId", policyRequest.getCustomerId())
                        .build(policyRequest.getId()))
                .retrieve()
                .body(FraudAnalysisResponse.class);

        if (response == null) {
            throw new IllegalStateException("fraud analysis response is null");
        }

        return new FraudAnalysis(
                response.orderId(),
                response.customerId(),
                response.analyzedAt(),
                response.classification(),
                response.occurrences().stream()
                        .map(item -> new FraudOccurrence(
                                item.id(),
                                item.productId(),
                                item.type(),
                                item.description(),
                                item.createdAt(),
                                item.updatedAt()
                        ))
                        .toList()
        );
    }
}
