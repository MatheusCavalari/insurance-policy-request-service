package br.com.matheus.insurance.interfaces.consumer;

import br.com.matheus.insurance.application.usecase.AnalyzePolicyRequestUseCase;
import br.com.matheus.insurance.domain.enums.PolicyRequestStatus;
import br.com.matheus.insurance.infrastructure.messaging.dto.PolicyRequestEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.*;

class PolicyRequestReceivedConsumerTest {

    @Test
    void should_trigger_analysis_with_request_id() {
        AnalyzePolicyRequestUseCase useCase = mock(AnalyzePolicyRequestUseCase.class);

        PolicyRequestReceivedConsumer consumer = new PolicyRequestReceivedConsumer(useCase);

        UUID requestId = UUID.randomUUID();

        PolicyRequestEvent event = new PolicyRequestEvent(
                UUID.randomUUID(),
                requestId,
                UUID.randomUUID(),
                PolicyRequestStatus.RECEIVED,
                Instant.parse("2026-03-15T10:00:00Z")
        );

        consumer.consume(event);

        verify(useCase).execute(requestId);
        verifyNoMoreInteractions(useCase);
    }
}