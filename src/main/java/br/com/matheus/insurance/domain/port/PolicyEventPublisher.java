package br.com.matheus.insurance.domain.port;

import br.com.matheus.insurance.domain.model.PolicyRequest;

public interface PolicyEventPublisher {
    void publishReceived(PolicyRequest policyRequest);
    void publishStatusChanged(PolicyRequest policyRequest);
}
