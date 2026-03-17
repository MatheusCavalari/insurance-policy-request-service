package br.com.matheus.insurance.application.usecase;

import br.com.matheus.insurance.domain.model.PolicyRequest;

import java.util.List;
import java.util.UUID;

public interface GetPolicyRequestsByCustomerIdUseCase {
    List<PolicyRequest> execute(UUID customerId);
}