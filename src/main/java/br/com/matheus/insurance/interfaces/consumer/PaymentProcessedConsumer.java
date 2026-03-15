package br.com.matheus.insurance.interfaces.consumer;

import br.com.matheus.insurance.domain.model.PolicyRequest;
import br.com.matheus.insurance.domain.port.PolicyRequestRepository;
import br.com.matheus.insurance.infrastructure.messaging.dto.PaymentProcessedEvent;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentProcessedConsumer {

    private final PolicyRequestRepository repository;

    public PaymentProcessedConsumer(PolicyRequestRepository repository) {
        this.repository = repository;
    }

    @SqsListener("${app.aws.sqs.payment-queue}")
    public void consume(PaymentProcessedEvent event) {
        PolicyRequest policyRequest = repository.findById(event.requestId())
                .orElseThrow(() -> new IllegalArgumentException("policy request not found: " + event.requestId()));

        if (policyRequest.isFinalStatus()) {
            return;
        }

        String normalizedStatus = event.status().trim().toUpperCase();

        switch (normalizedStatus) {
            case "APPROVED" -> handleApproved(policyRequest, event);
            case "DENIED" -> handleDenied(policyRequest, event);
            default -> throw new IllegalArgumentException("unsupported payment status: " + event.status());
        }

        repository.save(policyRequest);
    }

    private void handleApproved(PolicyRequest policyRequest, PaymentProcessedEvent event) {
        policyRequest.markPaymentApproved();

        if (policyRequest.isUnderwritingApproved()) {
            policyRequest.markApproved(event.occurredAt());
        } else if (policyRequest.getStatus() != br.com.matheus.insurance.domain.enums.PolicyRequestStatus.PENDING) {
            policyRequest.markPending(event.occurredAt());
        }
    }

    private void handleDenied(PolicyRequest policyRequest, PaymentProcessedEvent event) {
        policyRequest.markPaymentDenied();
        policyRequest.markRejected(event.occurredAt());
    }
}
