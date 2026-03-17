package br.com.matheus.insurance.interfaces.consumer;

import br.com.matheus.insurance.domain.exception.ResourceNotFoundException;
import br.com.matheus.insurance.domain.model.PolicyRequest;
import br.com.matheus.insurance.domain.port.PolicyRequestRepository;
import br.com.matheus.insurance.domain.port.ProcessedMessageRepository;
import br.com.matheus.insurance.infrastructure.messaging.OutboxEventFactory;
import br.com.matheus.insurance.infrastructure.messaging.SqsEventJsonReader;
import br.com.matheus.insurance.infrastructure.messaging.dto.PaymentProcessedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaymentProcessedConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentProcessedConsumer.class);
    private static final String CONSUMER_NAME = "payment-processed-consumer";

    private final PolicyRequestRepository repository;
    private final ProcessedMessageRepository processedMessageRepository;
    private final OutboxEventFactory outboxEventFactory;
    private final SqsEventJsonReader jsonReader;

    public PaymentProcessedConsumer(
            PolicyRequestRepository repository,
            ProcessedMessageRepository processedMessageRepository,
            OutboxEventFactory outboxEventFactory,
            SqsEventJsonReader jsonReader
    ) {
        this.repository = repository;
        this.processedMessageRepository = processedMessageRepository;
        this.outboxEventFactory = outboxEventFactory;
        this.jsonReader = jsonReader;
    }

    @Transactional
    @SqsListener("${app.aws.sqs.payment-queue}")
    public void consume(String rawMessage) {
        log.info("Received raw payment message: {}", rawMessage);

        try {
            PaymentProcessedEvent event = jsonReader.read(rawMessage, PaymentProcessedEvent.class);
            log.info("Parsed payment event: {}", event);

            if (processedMessageRepository.exists(CONSUMER_NAME, event.eventId())) {
                log.info("Payment event already processed. eventId={}", event.eventId());
                return;
            }

            PolicyRequest policyRequest = repository.findById(event.requestId())
                    .orElseThrow(() -> new ResourceNotFoundException("policy request not found: " + event.requestId()));

            if (!policyRequest.isFinalStatus()) {
                String normalizedStatus = event.status().trim().toUpperCase();

                switch (normalizedStatus) {
                    case "APPROVED" -> {
                        policyRequest.markPaymentApproved();
                        policyRequest.reconcileAsyncStatus(event.occurredAt());

                        if (policyRequest.isFinalStatus()) {
                            outboxEventFactory.appendPolicyStatusChanged(policyRequest, event.occurredAt());
                        }
                    }
                    case "DENIED" -> {
                        policyRequest.markPaymentDenied();
                        policyRequest.reconcileAsyncStatus(event.occurredAt());
                        outboxEventFactory.appendPolicyStatusChanged(policyRequest, event.occurredAt());
                    }
                    default -> throw new IllegalArgumentException("unsupported payment status: " + event.status());
                }

                repository.save(policyRequest);
                log.info("Payment event applied successfully. requestId={}, status={}",
                        policyRequest.getId(), policyRequest.getStatus());
            }

            processedMessageRepository.save(CONSUMER_NAME, event.eventId(), event.occurredAt());
            log.info("Payment event registered as processed. eventId={}", event.eventId());

        } catch (Exception ex) {
            log.error("Error processing payment message: {}", rawMessage, ex);
            throw new RuntimeException(ex);
        }
    }
}
