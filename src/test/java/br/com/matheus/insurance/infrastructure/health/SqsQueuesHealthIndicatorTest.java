package br.com.matheus.insurance.infrastructure.health;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SqsQueuesHealthIndicatorTest {

    @Test
    void should_return_up_when_all_queues_are_resolved() {
        SqsAsyncClient sqsAsyncClient = mock(SqsAsyncClient.class);

        when(sqsAsyncClient.getQueueUrl(
                org.mockito.ArgumentMatchers.<Consumer<GetQueueUrlRequest.Builder>>any()
        ))
                .thenReturn(CompletableFuture.completedFuture(GetQueueUrlResponse.builder().queueUrl("url-payment").build()))
                .thenReturn(CompletableFuture.completedFuture(GetQueueUrlResponse.builder().queueUrl("url-underwriting").build()))
                .thenReturn(CompletableFuture.completedFuture(GetQueueUrlResponse.builder().queueUrl("url-received").build()))
                .thenReturn(CompletableFuture.completedFuture(GetQueueUrlResponse.builder().queueUrl("url-status-changed").build()));

        SqsQueuesHealthIndicator indicator = new SqsQueuesHealthIndicator(
                sqsAsyncClient,
                "payment-processed-queue",
                "underwriting-processed-queue",
                "policy-request-received-queue",
                "policy-status-changed-queue"
        );

        var health = indicator.health();

        assertEquals("UP", health.getStatus().getCode());

        @SuppressWarnings("unchecked")
        Map<String, String> queues = (Map<String, String>) health.getDetails().get("queues");

        assertEquals("url-payment", queues.get("payment-processed-queue"));
        assertEquals("url-underwriting", queues.get("underwriting-processed-queue"));
        assertEquals("url-received", queues.get("policy-request-received-queue"));
        assertEquals("url-status-changed", queues.get("policy-status-changed-queue"));
    }

    @Test
    void should_return_down_when_any_queue_resolution_fails() {
        SqsAsyncClient sqsAsyncClient = mock(SqsAsyncClient.class);

        when(sqsAsyncClient.getQueueUrl(
                org.mockito.ArgumentMatchers.<Consumer<GetQueueUrlRequest.Builder>>any()
        ))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("sqs unavailable")));

        SqsQueuesHealthIndicator indicator = new SqsQueuesHealthIndicator(
                sqsAsyncClient,
                "payment-processed-queue",
                "underwriting-processed-queue",
                "policy-request-received-queue",
                "policy-status-changed-queue"
        );

        var health = indicator.health();

        assertEquals("DOWN", health.getStatus().getCode());
        assertNotNull(health.getDetails());
    }
}