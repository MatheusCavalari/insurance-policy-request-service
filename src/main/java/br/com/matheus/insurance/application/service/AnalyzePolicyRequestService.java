package br.com.matheus.insurance.application.service;

import br.com.matheus.insurance.application.usecase.AnalyzePolicyRequestUseCase;
import br.com.matheus.insurance.domain.model.FraudAnalysis;
import br.com.matheus.insurance.domain.model.PolicyRequest;
import br.com.matheus.insurance.domain.port.FraudAnalysisGateway;
import br.com.matheus.insurance.domain.port.PolicyRequestRepository;
import br.com.matheus.insurance.domain.rule.RiskValidationStrategy;
import br.com.matheus.insurance.domain.rule.RiskValidationStrategyFactory;
import br.com.matheus.insurance.domain.rule.ValidationDecision;
import br.com.matheus.insurance.infrastructure.messaging.OutboxEventFactory;

import java.time.Instant;
import java.util.UUID;

public class AnalyzePolicyRequestService implements AnalyzePolicyRequestUseCase {

    private final PolicyRequestRepository repository;
    private final FraudAnalysisGateway fraudAnalysisGateway;
    private final RiskValidationStrategyFactory strategyFactory;
    private final OutboxEventFactory outboxEventFactory;

    public AnalyzePolicyRequestService(
            PolicyRequestRepository repository,
            FraudAnalysisGateway fraudAnalysisGateway,
            RiskValidationStrategyFactory strategyFactory,
            OutboxEventFactory outboxEventFactory
    ) {
        this.repository = repository;
        this.fraudAnalysisGateway = fraudAnalysisGateway;
        this.strategyFactory = strategyFactory;
        this.outboxEventFactory = outboxEventFactory;
    }

    @Override
    public void execute(UUID policyRequestId) {
        PolicyRequest policyRequest = repository.findById(policyRequestId)
                .orElseThrow(() -> new IllegalArgumentException("policy request not found"));

        if (policyRequest.isFinalStatus()) {
            return;
        }

        FraudAnalysis fraudAnalysis = fraudAnalysisGateway.analyze(policyRequest);

        RiskValidationStrategy strategy = strategyFactory.get(fraudAnalysis.classification());
        ValidationDecision decision = strategy.validate(policyRequest);

        Instant now = fraudAnalysis.analyzedAt();

        if (decision.approved()) {
            policyRequest.markValidated(now);
            outboxEventFactory.appendPolicyStatusChanged(policyRequest, now);

            policyRequest.markPending(now.plusMillis(1));
            outboxEventFactory.appendPolicyStatusChanged(policyRequest, now.plusMillis(1));
        } else {
            policyRequest.markRejected(now);
            outboxEventFactory.appendPolicyStatusChanged(policyRequest, now);
        }

        repository.save(policyRequest);
    }
}
