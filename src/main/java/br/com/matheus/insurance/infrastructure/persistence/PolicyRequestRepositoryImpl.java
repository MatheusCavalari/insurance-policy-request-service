package br.com.matheus.insurance.infrastructure.persistence;

import br.com.matheus.insurance.domain.model.PolicyRequest;
import br.com.matheus.insurance.domain.port.PolicyRequestRepository;
import br.com.matheus.insurance.infrastructure.persistence.mapper.PolicyRequestPersistenceMapper;
import br.com.matheus.insurance.infrastructure.persistence.repository.SpringDataPolicyRequestRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public class PolicyRequestRepositoryImpl implements PolicyRequestRepository {

    private final SpringDataPolicyRequestRepository repository;
    private final PolicyRequestPersistenceMapper mapper;

    public PolicyRequestRepositoryImpl(
            SpringDataPolicyRequestRepository repository,
            PolicyRequestPersistenceMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public PolicyRequest save(PolicyRequest policyRequest) {
        var entity = mapper.toEntity(policyRequest);
        var saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<PolicyRequest> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<PolicyRequest> findByCustomerId(UUID customerId) {
        return repository.findByCustomerId(customerId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
