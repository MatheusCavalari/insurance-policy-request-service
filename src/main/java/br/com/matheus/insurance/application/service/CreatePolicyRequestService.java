package br.com.matheus.insurance.application.service;

import br.com.matheus.insurance.application.dto.CreatePolicyRequestCommand;
import br.com.matheus.insurance.application.dto.CreatePolicyRequestResult;
import br.com.matheus.insurance.application.usecase.CreatePolicyRequestUseCase;
import br.com.matheus.insurance.domain.model.PolicyRequest;
import br.com.matheus.insurance.domain.port.PolicyRequestRepository;

import java.time.Instant;
import java.util.UUID;

public class CreatePolicyRequestService implements CreatePolicyRequestUseCase {

    private final PolicyRequestRepository repository;

    public CreatePolicyRequestService(PolicyRequestRepository repository) {
        this.repository = repository;
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

        return new CreatePolicyRequestResult(
                saved.getId(),
                saved.getStatus(),
                saved.getCreatedAt()
        );
    }
}