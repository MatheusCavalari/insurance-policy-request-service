package br.com.matheus.insurance.domain.port;

import br.com.matheus.insurance.domain.model.PolicyRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PolicyRequestRepository {

    PolicyRequest save(PolicyRequest policyRequest);

    Optional<PolicyRequest> findById(UUID id);

    List<PolicyRequest> findByCustomerId(UUID customerId);
}