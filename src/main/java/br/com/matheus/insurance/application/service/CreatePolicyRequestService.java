package br.com.matheus.insurance.application.service;

import br.com.matheus.insurance.application.dto.CreatePolicyRequestCommand;
import br.com.matheus.insurance.application.dto.CreatePolicyRequestResult;
import br.com.matheus.insurance.application.usecase.CreatePolicyRequestUseCase;
import br.com.matheus.insurance.domain.model.PolicyRequest;
import br.com.matheus.insurance.domain.port.PolicyRequestRepository;
import br.com.matheus.insurance.infrastructure.messaging.OutboxEventFactory;

import java.time.Instant;
import java.util.UUID;

public class CreatePolicyRequestService implements CreatePolicyRequestUseCase {

    private final PolicyRequestRepository repository;
    private final OutboxEventFactory outboxEventFactory;

    public CreatePolicyRequestService(
            PolicyRequestRepository repository,
            OutboxEventFactory outboxEventFactory
    ) {
        this.repository = repository;
        this.outboxEventFactory = outboxEventFactory;
    }

    @Override
    public CreatePolicyRequestResult execute(CreatePolicyRequestCommand command) {
        Instant now = Instant.now();

        PolicyRequest policyRequest = PolicyRequest.create(
                UUID.randomUUID(),
                command.customerId(),
                command.productId(),
                command.category(),
                command.salesChannel(),
                command.paymentMethod(),
                command.totalMonthlyPremiumAmount(),
                command.insuredAmount(),
                command.coverages(),
                command.assistances(),
                now
        );

        PolicyRequest saved = repository.save(policyRequest);
        outboxEventFactory.appendPolicyRequestReceived(saved, now);

        return new CreatePolicyRequestResult(
                saved.getId(),
                saved.getStatus(),
                saved.getCreatedAt()
        );
    }
}
