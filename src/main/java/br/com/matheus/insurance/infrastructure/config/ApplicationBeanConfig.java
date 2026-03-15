package br.com.matheus.insurance.infrastructure.config;

import br.com.matheus.insurance.application.service.*;
import br.com.matheus.insurance.domain.port.FraudAnalysisGateway;
import br.com.matheus.insurance.domain.port.PolicyRequestRepository;
import br.com.matheus.insurance.domain.rule.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ApplicationBeanConfig {

    @Bean
    public RiskValidationStrategyFactory riskValidationStrategyFactory() {
        return new RiskValidationStrategyFactory(List.of(
                new RegularRiskValidationStrategy(),
                new HighRiskValidationStrategy(),
                new PreferredRiskValidationStrategy(),
                new NoInformationRiskValidationStrategy()
        ));
    }

    @Bean
    public CreatePolicyRequestService createPolicyRequestService(PolicyRequestRepository repository) {
        return new CreatePolicyRequestService(repository);
    }

    @Bean
    public GetPolicyRequestByIdService getPolicyRequestByIdService(PolicyRequestRepository repository) {
        return new GetPolicyRequestByIdService(repository);
    }

    @Bean
    public GetPolicyRequestsByCustomerIdService getPolicyRequestsByCustomerIdService(PolicyRequestRepository repository) {
        return new GetPolicyRequestsByCustomerIdService(repository);
    }

    @Bean
    public CancelPolicyRequestService cancelPolicyRequestService(PolicyRequestRepository repository) {
        return new CancelPolicyRequestService(repository);
    }

    @Bean
    public AnalyzePolicyRequestService analyzePolicyRequestService(
            PolicyRequestRepository repository,
            FraudAnalysisGateway fraudAnalysisGateway,
            RiskValidationStrategyFactory strategyFactory
    ) {
        return new AnalyzePolicyRequestService(repository, fraudAnalysisGateway, strategyFactory);
    }
}
