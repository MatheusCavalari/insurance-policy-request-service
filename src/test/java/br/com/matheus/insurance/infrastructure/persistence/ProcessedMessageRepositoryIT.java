package br.com.matheus.insurance.infrastructure.persistence;

import br.com.matheus.insurance.support.AbstractPostgresContainerSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers(disabledWithoutDocker = true)
@DataJpaTest
@Import(ProcessedMessageRepositoryImpl.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProcessedMessageRepositoryIT extends AbstractPostgresContainerSupport {

    @Autowired
    private ProcessedMessageRepositoryImpl repository;

    @Test
    void should_save_processed_message_and_return_true_on_exists() {
        String consumerName = "payment-processed-consumer";
        UUID eventId = UUID.randomUUID();
        Instant processedAt = Instant.parse("2026-03-15T11:00:00Z");

        repository.save(consumerName, eventId, processedAt);

        boolean exists = repository.exists(consumerName, eventId);

        assertTrue(exists);
    }

    @Test
    void should_return_false_when_processed_message_does_not_exist() {
        boolean exists = repository.exists("payment-processed-consumer", UUID.randomUUID());

        assertFalse(exists);
    }

    @Test
    void should_distinguish_messages_by_consumer_name() {
        UUID eventId = UUID.randomUUID();
        Instant processedAt = Instant.parse("2026-03-15T11:00:00Z");

        repository.save("payment-processed-consumer", eventId, processedAt);

        assertTrue(repository.exists("payment-processed-consumer", eventId));
        assertFalse(repository.exists("underwriting-processed-consumer", eventId));
    }
}