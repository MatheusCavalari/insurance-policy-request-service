package br.com.matheus.insurance.infrastructure.persistence.mapper;

import br.com.matheus.insurance.domain.enums.ExternalProcessStatus;
import br.com.matheus.insurance.domain.enums.PolicyRequestStatus;
import br.com.matheus.insurance.domain.model.PolicyRequest;
import br.com.matheus.insurance.infrastructure.persistence.entity.PolicyRequestJpaEntity;
import br.com.matheus.insurance.support.PolicyRequestTestFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PolicyRequestPersistenceMapperTest {

    private final PolicyRequestPersistenceMapper mapper = new PolicyRequestPersistenceMapper();

    @Test
    void should_map_domain_to_entity() {
        PolicyRequest domain = PolicyRequestTestFactory.restoredPendingWithPaymentApproved();

        PolicyRequestJpaEntity entity = mapper.toEntity(domain);

        assertNotNull(entity);
        assertEquals(domain.getId(), entity.getId());
        assertEquals(domain.getCustomerId(), entity.getCustomerId());
        assertEquals(domain.getProductId(), entity.getProductId());
        assertEquals(domain.getCategory(), entity.getCategory());
        assertEquals(domain.getSalesChannel(), entity.getSalesChannel());
        assertEquals(domain.getPaymentMethod(), entity.getPaymentMethod());
        assertEquals(domain.getStatus(), entity.getStatus());
        assertEquals(domain.getCreatedAt(), entity.getCreatedAt());
        assertEquals(domain.getFinishedAt(), entity.getFinishedAt());
        assertEquals(domain.getTotalMonthlyPremiumAmount(), entity.getTotalMonthlyPremiumAmount());
        assertEquals(domain.getInsuredAmount(), entity.getInsuredAmount());
        assertEquals(domain.getCoverages(), entity.getCoverages());
        assertEquals(domain.getAssistances(), entity.getAssistances());
        assertEquals(domain.getPaymentStatus(), entity.getPaymentStatus());
        assertEquals(domain.getUnderwritingStatus(), entity.getUnderwritingStatus());
        assertEquals(domain.getHistory().size(), entity.getHistory().size());
    }

    @Test
    void should_map_entity_to_domain() {
        PolicyRequest original = PolicyRequestTestFactory.restoredPendingWithPaymentApproved();

        PolicyRequestJpaEntity entity = mapper.toEntity(original);

        PolicyRequest domain = mapper.toDomain(entity);

        assertNotNull(domain);
        assertEquals(original.getId(), domain.getId());
        assertEquals(original.getCustomerId(), domain.getCustomerId());
        assertEquals(original.getProductId(), domain.getProductId());
        assertEquals(original.getCategory(), domain.getCategory());
        assertEquals(original.getSalesChannel(), domain.getSalesChannel());
        assertEquals(original.getPaymentMethod(), domain.getPaymentMethod());
        assertEquals(original.getStatus(), domain.getStatus());
        assertEquals(original.getCreatedAt(), domain.getCreatedAt());
        assertEquals(original.getFinishedAt(), domain.getFinishedAt());
        assertEquals(original.getTotalMonthlyPremiumAmount(), domain.getTotalMonthlyPremiumAmount());
        assertEquals(original.getInsuredAmount(), domain.getInsuredAmount());
        assertEquals(original.getCoverages(), domain.getCoverages());
        assertEquals(original.getAssistances(), domain.getAssistances());
        assertEquals(original.getPaymentStatus(), domain.getPaymentStatus());
        assertEquals(original.getUnderwritingStatus(), domain.getUnderwritingStatus());
        assertEquals(original.getHistory().size(), domain.getHistory().size());
    }

    @Test
    void should_preserve_history_and_external_statuses() {
        PolicyRequest original = PolicyRequestTestFactory.restoredApprovedPolicyRequest();

        PolicyRequestJpaEntity entity = mapper.toEntity(original);
        PolicyRequest restored = mapper.toDomain(entity);

        assertEquals(PolicyRequestStatus.APPROVED, restored.getStatus());
        assertEquals(ExternalProcessStatus.APPROVED, restored.getPaymentStatus());
        assertEquals(ExternalProcessStatus.APPROVED, restored.getUnderwritingStatus());
        assertEquals(4, restored.getHistory().size());
        assertEquals(PolicyRequestStatus.RECEIVED, restored.getHistory().get(0).status());
        assertEquals(PolicyRequestStatus.APPROVED, restored.getHistory().get(3).status());
    }
}
