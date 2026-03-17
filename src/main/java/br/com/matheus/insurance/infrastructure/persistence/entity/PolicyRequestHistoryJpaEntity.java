package br.com.matheus.insurance.infrastructure.persistence.entity;

import br.com.matheus.insurance.domain.enums.PolicyRequestStatus;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "policy_request_history")
public class PolicyRequestHistoryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PolicyRequestStatus status;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_request_id", nullable = false)
    private PolicyRequestJpaEntity policyRequest;

    public PolicyRequestHistoryJpaEntity() {
    }

    public PolicyRequestHistoryJpaEntity(PolicyRequestStatus status, Instant changedAt, PolicyRequestJpaEntity policyRequest) {
        this.status = status;
        this.changedAt = changedAt;
        this.policyRequest = policyRequest;
    }

    public Long getId() {
        return id;
    }

    public PolicyRequestStatus getStatus() {
        return status;
    }

    public void setStatus(PolicyRequestStatus status) {
        this.status = status;
    }

    public Instant getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(Instant changedAt) {
        this.changedAt = changedAt;
    }

    public PolicyRequestJpaEntity getPolicyRequest() {
        return policyRequest;
    }

    public void setPolicyRequest(PolicyRequestJpaEntity policyRequest) {
        this.policyRequest = policyRequest;
    }
}