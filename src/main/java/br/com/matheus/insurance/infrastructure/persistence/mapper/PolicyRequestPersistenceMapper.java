package br.com.matheus.insurance.infrastructure.persistence.mapper;

import br.com.matheus.insurance.domain.model.PolicyRequest;
import br.com.matheus.insurance.domain.valueobject.PolicyHistoryEntry;
import br.com.matheus.insurance.infrastructure.persistence.entity.PolicyRequestHistoryJpaEntity;
import br.com.matheus.insurance.infrastructure.persistence.entity.PolicyRequestJpaEntity;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class PolicyRequestPersistenceMapper {

    public PolicyRequestJpaEntity toEntity(PolicyRequest domain) {
        PolicyRequestJpaEntity entity = new PolicyRequestJpaEntity();
        entity.setId(domain.getId());
        entity.setCustomerId(domain.getCustomerId());
        entity.setProductId(domain.getProductId());
        entity.setCategory(domain.getCategory());
        entity.setSalesChannel(domain.getSalesChannel());
        entity.setPaymentMethod(domain.getPaymentMethod());
        entity.setStatus(domain.getStatus());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setFinishedAt(domain.getFinishedAt());
        entity.setTotalMonthlyPremiumAmount(domain.getTotalMonthlyPremiumAmount());
        entity.setInsuredAmount(domain.getInsuredAmount());
        entity.setCoverages(domain.getCoverages());
        entity.setAssistances(domain.getAssistances());

        entity.setHistory(
                domain.getHistory().stream()
                        .map(item -> new PolicyRequestHistoryJpaEntity(
                                item.status(),
                                item.timestamp(),
                                entity
                        ))
                        .collect(Collectors.toList())
        );

        return entity;
    }

    public PolicyRequest toDomain(PolicyRequestJpaEntity entity) {
        PolicyRequest domain = new PolicyRequest(
                entity.getId(),
                entity.getCustomerId(),
                entity.getProductId(),
                entity.getCategory(),
                entity.getSalesChannel(),
                entity.getPaymentMethod(),
                entity.getTotalMonthlyPremiumAmount(),
                entity.getInsuredAmount(),
                entity.getCoverages(),
                entity.getAssistances()
        );

        return domain;
    }
}