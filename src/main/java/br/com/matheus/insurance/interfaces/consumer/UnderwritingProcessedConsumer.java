package br.com.matheus.insurance.interfaces.consumer;

import br.com.matheus.insurance.domain.model.PolicyRequest;
import br.com.matheus.insurance.domain.port.PolicyRequestRepository;
import br.com.matheus.insurance.infrastructure.messaging.dto.UnderwritingProcessedEvent;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.stereotype.Component;

@Component
public class UnderwritingProcessedConsumer {

    private final PolicyRequestRepository repository;

    public UnderwritingProcessedConsumer(PolicyRequestRepository repository) {
        this.repository = repository;
    }

    @SqsListener("${app.aws.sqs.underwriting-queue}")
    public void consume(UnderwritingProcessedEvent event) {
        PolicyRequest policyRequest = repository.findById(event.requestId())
                .orElseThrow(() -> new IllegalArgumentException("policy request not found: " + event.requestId()));

        if (policyRequest.isFinalStatus()) {
            return;
        }

        String normalizedStatus = event.status().trim().toUpperCase();

        switch (normalizedStatus) {
            case "APPROVED" -> handleApproved(policyRequest, event);
            case "DENIED" -> handleDenied(policyRequest, event);
            default -> throw new IllegalArgumentException("unsupported underwriting status: " + event.status());
        }

        repository.save(policyRequest);
    }

    private void handleApproved(PolicyRequest policyRequest, UnderwritingProcessedEvent event) {
        policyRequest.markUnderwritingApproved();

        if (policyRequest.isPaymentApproved()) {
            policyRequest.markApproved(event.occurredAt());
        } else if (policyRequest.getStatus() != br.com.matheus.insurance.domain.enums.PolicyRequestStatus.PENDING) {
            policyRequest.markPending(event.occurredAt());
        }
    }

    private void handleDenied(PolicyRequest policyRequest, UnderwritingProcessedEvent event) {
        policyRequest.markUnderwritingDenied();
        policyRequest.markRejected(event.occurredAt());
    }
}
