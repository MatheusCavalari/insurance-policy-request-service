package br.com.matheus.insurance.infrastructure.persistence.repository;

import br.com.matheus.insurance.infrastructure.persistence.entity.ProcessedMessageJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataProcessedMessageRepository extends JpaRepository<ProcessedMessageJpaEntity, Long> {

    boolean existsByConsumerNameAndEventId(String consumerName, UUID eventId);
}
