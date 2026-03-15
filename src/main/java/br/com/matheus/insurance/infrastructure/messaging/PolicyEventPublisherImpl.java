package br.com.matheus.insurance.infrastructure.messaging;

import br.com.matheus.insurance.domain.model.PolicyRequest;
import br.com.matheus.insurance.domain.port.PolicyEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class PolicyEventPublisherImpl implements PolicyEventPublisher {

    @Override
    public void publishReceived(PolicyRequest policyRequest) {
        // publicar no tópico/queue
    }

    @Override
    public void publishStatusChanged(PolicyRequest policyRequest) {
        // publicar no tópico/queue
    }
}
