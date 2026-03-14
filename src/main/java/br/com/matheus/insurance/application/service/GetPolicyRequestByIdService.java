package br.com.matheus.insurance.application.service;

import br.com.matheus.insurance.application.usecase.GetPolicyRequestByIdUseCase;
import br.com.matheus.insurance.domain.model.PolicyRequest;
import br.com.matheus.insurance.domain.port.PolicyRequestRepository;

import java.util.UUID;

public class GetPolicyRequestByIdService implements GetPolicyRequestByIdUseCase {

    private final PolicyRequestRepository repository;

    public GetPolicyRequestByIdService(PolicyRequestRepository repository) {
        this.repository = repository;
    }

    @Override
    public PolicyRequest execute(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("policy request not found"));
    }
}