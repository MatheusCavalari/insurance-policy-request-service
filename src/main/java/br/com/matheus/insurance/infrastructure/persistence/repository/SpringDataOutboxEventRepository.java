package br.com.matheus.insurance.infrastructure.persistence.repository;

import br.com.matheus.insurance.domain.enums.OutboxEventStatus;
import br.com.matheus.insurance.infrastructure.persistence.entity.OutboxEventJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataOutboxEventRepository extends JpaRepository<OutboxEventJpaEntity, UUID> {

    List<OutboxEventJpaEntity> findTop50ByStatusOrderByCreatedAtAsc(OutboxEventStatus status);
    long countByStatus(OutboxEventStatus status);
}
