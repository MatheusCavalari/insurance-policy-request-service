package br.com.matheus.insurance.infrastructure.fraud;

import br.com.matheus.insurance.domain.enums.RiskClassification;
import br.com.matheus.insurance.domain.model.FraudAnalysis;
import br.com.matheus.insurance.domain.model.PolicyRequest;
import br.com.matheus.insurance.support.PolicyRequestTestFactory;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;

class FraudAnalysisGatewayImplIT {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @Test
    void should_return_regular_fraud_analysis() {
        PolicyRequest request = PolicyRequestTestFactory.newPolicyRequest();

        wireMock.stubFor(get(urlPathMatching("/fraud-analysis/.*"))
                .withQueryParam("customerId", equalTo(request.getCustomerId().toString()))
                .willReturn(okJson("""
                        {
                          "orderId": "%s",
                          "customerId": "%s",
                          "analyzedAt": "2026-03-14T12:00:00Z",
                          "classification": "REGULAR",
                          "occurrences": []
                        }
                        """.formatted(request.getId(), request.getCustomerId()))));

        FraudAnalysisGatewayImpl gateway =
                new FraudAnalysisGatewayImpl(wireMock.getRuntimeInfo().getHttpBaseUrl());

        FraudAnalysis result = gateway.analyze(request);

        assertNotNull(result);
        assertEquals(RiskClassification.REGULAR, result.classification());
        assertTrue(result.occurrences().isEmpty());
    }

    @Test
    void should_return_high_risk_fraud_analysis_with_occurrence() {
        PolicyRequest request = PolicyRequestTestFactory.newPolicyRequest();

        wireMock.stubFor(get(urlPathMatching("/fraud-analysis/.*"))
                .withQueryParam("customerId", equalTo(request.getCustomerId().toString()))
                .willReturn(okJson("""
                        {
                          "orderId": "%s",
                          "customerId": "%s",
                          "analyzedAt": "2026-03-14T12:05:00Z",
                          "classification": "HIGH_RISK",
                          "occurrences": [
                            {
                              "id": "occ-001",
                              "productId": 123,
                              "type": "SUSPICIOUS_CLAIM_HISTORY",
                              "description": "Customer has suspicious claim history",
                              "createdAt": "2026-03-10T10:00:00Z",
                              "updatedAt": "2026-03-12T15:00:00Z"
                            }
                          ]
                        }
                        """.formatted(request.getId(), request.getCustomerId()))));

        FraudAnalysisGatewayImpl gateway =
                new FraudAnalysisGatewayImpl(wireMock.getRuntimeInfo().getHttpBaseUrl());

        FraudAnalysis result = gateway.analyze(request);

        assertEquals(RiskClassification.HIGH_RISK, result.classification());
        assertEquals(1, result.occurrences().size());
        assertEquals("occ-001", result.occurrences().get(0).id());
    }

    @Test
    void should_throw_exception_when_wiremock_returns_404() {
        PolicyRequest request = PolicyRequestTestFactory.newPolicyRequest();

        wireMock.stubFor(get(urlPathMatching("/fraud-analysis/.*"))
                .withQueryParam("customerId", equalTo(request.getCustomerId().toString()))
                .willReturn(aResponse().withStatus(404)));

        FraudAnalysisGatewayImpl gateway =
                new FraudAnalysisGatewayImpl(wireMock.getRuntimeInfo().getHttpBaseUrl());

        assertThrows(Exception.class, () -> gateway.analyze(request));
    }
}