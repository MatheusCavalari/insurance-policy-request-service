package br.com.matheus.insurance.infrastructure.messaging;

import br.com.matheus.insurance.domain.enums.OutboxEventType;
import br.com.matheus.insurance.domain.port.OutboxEventRepository;
import br.com.matheus.insurance.domain.port.PolicyEventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.concurrent.CompletableFuture;

@Component
public class PolicyEventPublisherImpl implements PolicyEventPublisher {

    private final SqsAsyncClient sqsAsyncClient;
    private final String receivedQueue;
    private final String statusChangedQueue;

    public PolicyEventPublisherImpl(
            SqsAsyncClient sqsAsyncClient,
            @Value("${app.aws.sqs.policy-request-received-queue}") String receivedQueue,
            @Value("${app.aws.sqs.policy-status-changed-queue}") String statusChangedQueue
    ) {
        this.sqsAsyncClient = sqsAsyncClient;
        this.receivedQueue = receivedQueue;
        this.statusChangedQueue = statusChangedQueue;
    }

    public CompletableFuture<Void> publish(OutboxEventType eventType, String payload) {
        String queueName = switch (eventType) {
            case POLICY_REQUEST_RECEIVED -> receivedQueue;
            case POLICY_STATUS_CHANGED -> statusChangedQueue;
        };

        return sqsAsyncClient.getQueueUrl(builder -> builder.queueName(queueName))
                .thenCompose(queueUrlResponse ->
                        sqsAsyncClient.sendMessage(
                                SendMessageRequest.builder()
                                        .queueUrl(queueUrlResponse.queueUrl())
                                        .messageBody(payload)
                                        .build()
                        )
                )
                .thenApply(response -> null);
    }
}
