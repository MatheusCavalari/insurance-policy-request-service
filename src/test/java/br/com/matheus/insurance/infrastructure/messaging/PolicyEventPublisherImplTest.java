package br.com.matheus.insurance.infrastructure.messaging;

import br.com.matheus.insurance.domain.enums.OutboxEventType;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class PolicyEventPublisherImplTest {

    @Test
    void should_publish_to_policy_request_received_queue() {
        SqsAsyncClient sqsAsyncClient = mock(SqsAsyncClient.class);

        when(sqsAsyncClient.getQueueUrl(
                org.mockito.ArgumentMatchers.<Consumer<GetQueueUrlRequest.Builder>>any()
        )).thenReturn(CompletableFuture.completedFuture(
                GetQueueUrlResponse.builder().queueUrl("http://queue/received").build()
        ));

        when(sqsAsyncClient.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(SendMessageResponse.builder().messageId("1").build()));

        PolicyEventPublisherImpl publisher = new PolicyEventPublisherImpl(
                sqsAsyncClient,
                "policy-request-received-queue",
                "policy-status-changed-queue"
        );

        publisher.publish(OutboxEventType.POLICY_REQUEST_RECEIVED, "{\"hello\":\"world\"}").join();

        verify(sqsAsyncClient).sendMessage(argThat((SendMessageRequest request) ->
                request.queueUrl().equals("http://queue/received")
                        && request.messageBody().equals("{\"hello\":\"world\"}")
        ));
    }

    @Test
    void should_publish_to_policy_status_changed_queue() {
        SqsAsyncClient sqsAsyncClient = mock(SqsAsyncClient.class);

        when(sqsAsyncClient.getQueueUrl(
                org.mockito.ArgumentMatchers.<Consumer<GetQueueUrlRequest.Builder>>any()
        )).thenReturn(CompletableFuture.completedFuture(
                GetQueueUrlResponse.builder().queueUrl("http://queue/status").build()
        ));

        when(sqsAsyncClient.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(SendMessageResponse.builder().messageId("2").build()));

        PolicyEventPublisherImpl publisher = new PolicyEventPublisherImpl(
                sqsAsyncClient,
                "policy-request-received-queue",
                "policy-status-changed-queue"
        );

        publisher.publish(OutboxEventType.POLICY_STATUS_CHANGED, "{\"status\":\"APPROVED\"}").join();

        verify(sqsAsyncClient).sendMessage(argThat((SendMessageRequest request) ->
                request.queueUrl().equals("http://queue/status")
                        && request.messageBody().equals("{\"status\":\"APPROVED\"}")
        ));
    }
}