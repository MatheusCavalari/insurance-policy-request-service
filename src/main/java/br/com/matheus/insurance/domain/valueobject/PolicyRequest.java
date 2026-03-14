package br.com.matheus.insurance.domain.model;

import br.com.matheus.insurance.domain.enums.PaymentMethod;
import br.com.matheus.insurance.domain.enums.PolicyCategory;
import br.com.matheus.insurance.domain.enums.PolicyRequestStatus;
import br.com.matheus.insurance.domain.enums.SalesChannel;
import br.com.matheus.insurance.domain.valueobject.PolicyHistoryEntry;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PolicyRequest {

    private UUID id;
    private UUID customerId;
    private Long productId;
    private PolicyCategory category;
    private SalesChannel salesChannel;
    private PaymentMethod paymentMethod;
    private PolicyRequestStatus status;
    private Instant createdAt;
    private Instant finishedAt;
    private BigDecimal totalMonthlyPremiumAmount;
    private BigDecimal insuredAmount;
    private Map<String, BigDecimal> coverages;
    private List<String> assistances;
    private List<PolicyHistoryEntry> history;

    public PolicyRequest(
            UUID id,
            UUID customerId,
            Long productId,
            PolicyCategory category,
            SalesChannel salesChannel,
            PaymentMethod paymentMethod,
            BigDecimal totalMonthlyPremiumAmount,
            BigDecimal insuredAmount,
            Map<String, BigDecimal> coverages,
            List<String> assistances
    ) {
        this.id = id;
        this.customerId = customerId;
        this.productId = productId;
        this.category = category;
        this.salesChannel = salesChannel;
        this.paymentMethod = paymentMethod;
        this.totalMonthlyPremiumAmount = totalMonthlyPremiumAmount;
        this.insuredAmount = insuredAmount;
        this.coverages = coverages;
        this.assistances = assistances;
        this.status = PolicyRequestStatus.RECEIVED;
        this.createdAt = Instant.now();
        this.history = new ArrayList<>();
        this.history.add(new PolicyHistoryEntry(PolicyRequestStatus.RECEIVED, this.createdAt));
    }

    public void markValidated() {
        this.status = PolicyRequestStatus.VALIDATED;
        this.history.add(new PolicyHistoryEntry(PolicyRequestStatus.VALIDATED, Instant.now()));
    }

    public void markPending() {
        this.status = PolicyRequestStatus.PENDING;
        this.history.add(new PolicyHistoryEntry(PolicyRequestStatus.PENDING, Instant.now()));
    }

    public void markApproved() {
        this.status = PolicyRequestStatus.APPROVED;
        this.finishedAt = Instant.now();
        this.history.add(new PolicyHistoryEntry(PolicyRequestStatus.APPROVED, this.finishedAt));
    }

    public void markRejected() {
        this.status = PolicyRequestStatus.REJECTED;
        this.finishedAt = Instant.now();
        this.history.add(new PolicyHistoryEntry(PolicyRequestStatus.REJECTED, this.finishedAt));
    }

    public void cancel() {
        this.status = PolicyRequestStatus.CANCELED;
        this.finishedAt = Instant.now();
        this.history.add(new PolicyHistoryEntry(PolicyRequestStatus.CANCELED, this.finishedAt));
    }

    public UUID getId() {
        return id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public Long getProductId() {
        return productId;
    }

    public PolicyCategory getCategory() {
        return category;
    }

    public SalesChannel getSalesChannel() {
        return salesChannel;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public PolicyRequestStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public BigDecimal getTotalMonthlyPremiumAmount() {
        return totalMonthlyPremiumAmount;
    }

    public BigDecimal getInsuredAmount() {
        return insuredAmount;
    }

    public Map<String, BigDecimal> getCoverages() {
        return coverages;
    }

    public List<String> getAssistances() {
        return assistances;
    }

    public List<PolicyHistoryEntry> getHistory() {
        return history;
    }
}