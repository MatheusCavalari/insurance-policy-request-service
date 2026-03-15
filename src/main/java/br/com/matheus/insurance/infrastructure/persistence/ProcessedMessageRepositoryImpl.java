package br.com.matheus.insurance.infrastructure.persistence;

import br.com.matheus.insurance.domain.port.ProcessedMessageRepository;
import br.com.matheus.insurance.infrastructure.persistence.entity.ProcessedMessageJpaEntity;
import br.com.matheus.insurance.infrastructure.persistence.repository.SpringDataProcessedMessageRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Repository
@Transactional
public class ProcessedMessageRepositoryImpl implements ProcessedMessageRepository {

    private final SpringDataProcessedMessageRepository repository;

    public ProcessedMessageRepositoryImpl(SpringDataProcessedMessageRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(String consumerName, UUID eventId) {
        return repository.existsByConsumerNameAndEventId(consumerName, eventId);
    }

    @Override
    public void save(String consumerName, UUID eventId, Instant processedAt) {
        ProcessedMessageJpaEntity entity = new ProcessedMessageJpaEntity();
        entity.setConsumerName(consumerName);
        entity.setEventId(eventId);
        entity.setProcessedAt(processedAt);
        repository.save(entity);
    }
}
