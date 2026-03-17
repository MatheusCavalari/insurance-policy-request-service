package br.com.matheus.insurance.infrastructure.config;

import br.com.matheus.insurance.application.service.*;
import br.com.matheus.insurance.domain.port.FraudAnalysisGateway;
import br.com.matheus.insurance.domain.port.PolicyRequestRepository;
import br.com.matheus.insurance.domain.rule.*;
import br.com.matheus.insurance.infrastructure.messaging.OutboxEventFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

@Configuration
@EnableScheduling
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
    public CreatePolicyRequestService createPolicyRequestService(
            PolicyRequestRepository repository,
            OutboxEventFactory outboxEventFactory
    ) {
        return new CreatePolicyRequestService(repository, outboxEventFactory);
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
    public CancelPolicyRequestService cancelPolicyRequestService(
            PolicyRequestRepository repository,
            OutboxEventFactory outboxEventFactory
    ) {
        return new CancelPolicyRequestService(repository, outboxEventFactory);
    }

    @Bean
    public AnalyzePolicyRequestService analyzePolicyRequestService(
            PolicyRequestRepository repository,
            FraudAnalysisGateway fraudAnalysisGateway,
            RiskValidationStrategyFactory strategyFactory,
            OutboxEventFactory outboxEventFactory
    ) {
        return new AnalyzePolicyRequestService(repository, fraudAnalysisGateway, strategyFactory, outboxEventFactory);
    }
}
