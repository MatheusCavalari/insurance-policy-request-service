package br.com.matheus.insurance.application.service;

import br.com.matheus.insurance.application.usecase.CancelPolicyRequestUseCase;
import br.com.matheus.insurance.domain.enums.PolicyRequestStatus;
import br.com.matheus.insurance.domain.model.PolicyRequest;
import br.com.matheus.insurance.domain.port.PolicyRequestRepository;

import java.time.Instant;
import java.util.UUID;

public class CancelPolicyRequestService implements CancelPolicyRequestUseCase {

    private final PolicyRequestRepository repository;

    public CancelPolicyRequestService(PolicyRequestRepository repository) {
        this.repository = repository;
    }

    @Override
    public void execute(UUID policyRequestId) {
        PolicyRequest policyRequest = repository.findById(policyRequestId)
                .orElseThrow(() -> new IllegalArgumentException("policy request not found"));

        if (policyRequest.getStatus() == PolicyRequestStatus.APPROVED
                || policyRequest.getStatus() == PolicyRequestStatus.REJECTED) {
            throw new IllegalArgumentException("policy request cannot be canceled");
        }

        policyRequest.cancel(Instant.now());
        repository.save(policyRequest);
    }
}