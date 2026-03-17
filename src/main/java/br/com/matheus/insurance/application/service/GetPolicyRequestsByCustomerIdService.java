package br.com.matheus.insurance.application.service;

import br.com.matheus.insurance.application.usecase.GetPolicyRequestsByCustomerIdUseCase;
import br.com.matheus.insurance.domain.model.PolicyRequest;
import br.com.matheus.insurance.domain.port.PolicyRequestRepository;

import java.util.List;
import java.util.UUID;

public class GetPolicyRequestsByCustomerIdService implements GetPolicyRequestsByCustomerIdUseCase {

    private final PolicyRequestRepository repository;

    public GetPolicyRequestsByCustomerIdService(PolicyRequestRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<PolicyRequest> execute(UUID customerId) {
        return repository.findByCustomerId(customerId);
    }
}