package br.com.matheus.insurance.infrastructure.messaging;

import br.com.matheus.insurance.domain.enums.OutboxEventStatus;
import br.com.matheus.insurance.domain.enums.OutboxEventType;
import br.com.matheus.insurance.domain.model.PolicyRequest;
import br.com.matheus.insurance.domain.port.OutboxEventRepository;
import br.com.matheus.insurance.infrastructure.messaging.dto.PolicyRequestEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class OutboxEventFactory {

    private final ObjectMapper objectMapper;
    private final OutboxEventRepository outboxEventRepository;

    public OutboxEventFactory(ObjectMapper objectMapper, OutboxEventRepository outboxEventRepository) {
        this.objectMapper = objectMapper;
        this.outboxEventRepository = outboxEventRepository;
    }

    public void appendPolicyRequestReceived(PolicyRequest policyRequest, Instant occurredAt) {
        append(policyRequest, OutboxEventType.POLICY_REQUEST_RECEIVED, occurredAt);
    }

    public void appendPolicyStatusChanged(PolicyRequest policyRequest, Instant occurredAt) {
        append(policyRequest, OutboxEventType.POLICY_STATUS_CHANGED, occurredAt);
    }

    private void append(PolicyRequest policyRequest, OutboxEventType type, Instant occurredAt) {
        PolicyRequestEvent event = new PolicyRequestEvent(
                UUID.randomUUID(),
                policyRequest.getId(),
                policyRequest.getCustomerId(),
                policyRequest.getStatus(),
                occurredAt
        );

        try {
            outboxEventRepository.save(
                    UUID.randomUUID(),
                    policyRequest.getId(),
                    type,
                    objectMapper.writeValueAsString(event),
                    OutboxEventStatus.PENDING,
                    occurredAt
            );
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("error serializing outbox payload", e);
        }
    }
}
