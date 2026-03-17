package br.com.matheus.insurance.infrastructure.persistence.repository;

import br.com.matheus.insurance.infrastructure.persistence.entity.PolicyRequestJpaEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataPolicyRequestRepository extends JpaRepository<PolicyRequestJpaEntity, UUID> {

    @Override
    @EntityGraph(attributePaths = "history")
    Optional<PolicyRequestJpaEntity> findById(UUID id);

    @EntityGraph(attributePaths = "history")
    List<PolicyRequestJpaEntity> findByCustomerId(UUID customerId);
}
