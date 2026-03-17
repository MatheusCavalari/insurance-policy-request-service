package br.com.matheus.insurance.application.service;

import br.com.matheus.insurance.application.usecase.CancelPolicyRequestUseCase;
import br.com.matheus.insurance.domain.enums.PolicyRequestStatus;
import br.com.matheus.insurance.domain.exception.ResourceNotFoundException;
import br.com.matheus.insurance.domain.model.PolicyRequest;
import br.com.matheus.insurance.domain.port.PolicyRequestRepository;
import br.com.matheus.insurance.infrastructure.messaging.OutboxEventFactory;

import java.time.Instant;
import java.util.UUID;

public class CancelPolicyRequestService implements CancelPolicyRequestUseCase {

    private final PolicyRequestRepository repository;
    private final OutboxEventFactory outboxEventFactory;

    public CancelPolicyRequestService(
            PolicyRequestRepository repository,
            OutboxEventFactory outboxEventFactory
    ) {
        this.repository = repository;
        this.outboxEventFactory = outboxEventFactory;
    }

    @Override
    public void execute(UUID policyRequestId) {
        PolicyRequest policyRequest = repository.findById(policyRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("policy request not found"));

        if (policyRequest.getStatus() == PolicyRequestStatus.APPROVED
                || policyRequest.getStatus() == PolicyRequestStatus.REJECTED) {
            throw new IllegalArgumentException("policy request cannot be canceled");
        }

        Instant now = Instant.now();
        policyRequest.cancel(now);
        repository.save(policyRequest);
        outboxEventFactory.appendPolicyStatusChanged(policyRequest, now);
    }
}
