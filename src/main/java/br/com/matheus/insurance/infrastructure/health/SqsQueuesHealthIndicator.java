package br.com.matheus.insurance.infrastructure.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import java.util.LinkedHashMap;
import java.util.Map;

@Component("sqsQueues")
public class SqsQueuesHealthIndicator implements HealthIndicator {

    private final SqsAsyncClient sqsAsyncClient;
    private final String paymentQueue;
    private final String underwritingQueue;
    private final String policyRequestReceivedQueue;
    private final String policyStatusChangedQueue;

    public SqsQueuesHealthIndicator(
            SqsAsyncClient sqsAsyncClient,
            @Value("${app.aws.sqs.payment-queue}") String paymentQueue,
            @Value("${app.aws.sqs.underwriting-queue}") String underwritingQueue,
            @Value("${app.aws.sqs.policy-request-received-queue}") String policyRequestReceivedQueue,
            @Value("${app.aws.sqs.policy-status-changed-queue}") String policyStatusChangedQueue
    ) {
        this.sqsAsyncClient = sqsAsyncClient;
        this.paymentQueue = paymentQueue;
        this.underwritingQueue = underwritingQueue;
        this.policyRequestReceivedQueue = policyRequestReceivedQueue;
        this.policyStatusChangedQueue = policyStatusChangedQueue;
    }

    @Override
    public Health health() {
        try {
            Map<String, String> queues = new LinkedHashMap<>();
            queues.put(paymentQueue, resolveQueueUrl(paymentQueue));
            queues.put(underwritingQueue, resolveQueueUrl(underwritingQueue));
            queues.put(policyRequestReceivedQueue, resolveQueueUrl(policyRequestReceivedQueue));
            queues.put(policyStatusChangedQueue, resolveQueueUrl(policyStatusChangedQueue));

            return Health.up()
                    .withDetail("queues", queues)
                    .build();
        } catch (Exception ex) {
            return Health.down(ex).build();
        }
    }

    private String resolveQueueUrl(String queueName) {
        return sqsAsyncClient.getQueueUrl(builder -> builder.queueName(queueName))
                .join()
                .queueUrl();
    }
}