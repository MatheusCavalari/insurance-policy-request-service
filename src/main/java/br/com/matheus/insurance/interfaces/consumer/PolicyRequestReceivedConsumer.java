package br.com.matheus.insurance.interfaces.consumer;

import br.com.matheus.insurance.application.usecase.AnalyzePolicyRequestUseCase;
import br.com.matheus.insurance.infrastructure.messaging.dto.PolicyRequestEvent;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.stereotype.Component;

@Component
public class PolicyRequestReceivedConsumer {

    private final AnalyzePolicyRequestUseCase analyzePolicyRequestUseCase;

    public PolicyRequestReceivedConsumer(AnalyzePolicyRequestUseCase analyzePolicyRequestUseCase) {
        this.analyzePolicyRequestUseCase = analyzePolicyRequestUseCase;
    }

    @SqsListener("${app.aws.sqs.policy-request-received-queue}")
    public void consume(PolicyRequestEvent event) {
        analyzePolicyRequestUseCase.execute(event.requestId());
    }
}
