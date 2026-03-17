package br.com.matheus.insurance.domain.port;

import br.com.matheus.insurance.domain.enums.OutboxEventType;

import java.util.concurrent.CompletableFuture;

public interface PolicyEventPublisher {
    CompletableFuture<Void> publish(OutboxEventType eventType, String payload);
}
