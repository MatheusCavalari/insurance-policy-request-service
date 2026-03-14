package br.com.matheus.insurance.domain.model;

import br.com.matheus.insurance.domain.enums.PaymentMethod;
import br.com.matheus.insurance.domain.enums.PolicyCategory;
import br.com.matheus.insurance.domain.enums.PolicyRequestStatus;
import br.com.matheus.insurance.domain.enums.SalesChannel;
import br.com.matheus.insurance.domain.exception.InvalidStateTransitionException;
import br.com.matheus.insurance.domain.valueobject.PolicyHistoryEntry;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

public class PolicyRequest {

    private static final Map<PolicyRequestStatus, Set<PolicyRequestStatus>> ALLOWED_TRANSITIONS = Map.of(
            PolicyRequestStatus.RECEIVED, Set.of(
                    PolicyRequestStatus.VALIDATED,
                    PolicyRequestStatus.REJECTED,
                    PolicyRequestStatus.CANCELED
            ),
            PolicyRequestStatus.VALIDATED, Set.of(
                    PolicyRequestStatus.PENDING
            ),
            PolicyRequestStatus.PENDING, Set.of(
                    PolicyRequestStatus.PENDING,
                    PolicyRequestStatus.APPROVED,
                    PolicyRequestStatus.REJECTED,
                    PolicyRequestStatus.CANCELED
            ),
            PolicyRequestStatus.APPROVED, Set.of(),
            PolicyRequestStatus.REJECTED, Set.of(),
            PolicyRequestStatus.CANCELED, Set.of()
    );

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

    private PolicyRequest() {
    }

    public static PolicyRequest create(
            UUID id,
            UUID customerId,
            Long productId,
            PolicyCategory category,
            SalesChannel salesChannel,
            PaymentMethod paymentMethod,
            BigDecimal totalMonthlyPremiumAmount,
            BigDecimal insuredAmount,
            Map<String, BigDecimal> coverages,
            List<String> assistances,
            Instant createdAt
    ) {
        PolicyRequest request = new PolicyRequest();
        request.id = id;
        request.customerId = customerId;
        request.productId = productId;
        request.category = category;
        request.salesChannel = salesChannel;
        request.paymentMethod = paymentMethod;
        request.totalMonthlyPremiumAmount = totalMonthlyPremiumAmount;
        request.insuredAmount = insuredAmount;
        request.coverages = new LinkedHashMap<>(coverages);
        request.assistances = new ArrayList<>(assistances);
        request.status = PolicyRequestStatus.RECEIVED;
        request.createdAt = createdAt;
        request.history = new ArrayList<>();
        request.history.add(new PolicyHistoryEntry(PolicyRequestStatus.RECEIVED, createdAt));
        return request;
    }

    public static PolicyRequest restore(
            UUID id,
            UUID customerId,
            Long productId,
            PolicyCategory category,
            SalesChannel salesChannel,
            PaymentMethod paymentMethod,
            PolicyRequestStatus status,
            Instant createdAt,
            Instant finishedAt,
            BigDecimal totalMonthlyPremiumAmount,
            BigDecimal insuredAmount,
            Map<String, BigDecimal> coverages,
            List<String> assistances,
            List<PolicyHistoryEntry> history
    ) {
        PolicyRequest request = new PolicyRequest();
        request.id = id;
        request.customerId = customerId;
        request.productId = productId;
        request.category = category;
        request.salesChannel = salesChannel;
        request.paymentMethod = paymentMethod;
        request.status = status;
        request.createdAt = createdAt;
        request.finishedAt = finishedAt;
        request.totalMonthlyPremiumAmount = totalMonthlyPremiumAmount;
        request.insuredAmount = insuredAmount;
        request.coverages = new LinkedHashMap<>(coverages);
        request.assistances = new ArrayList<>(assistances);
        request.history = new ArrayList<>(history);
        return request;
    }

    public void markValidated(Instant now) {
        transitionTo(PolicyRequestStatus.VALIDATED, now);
    }

    public void markPending(Instant now) {
        transitionTo(PolicyRequestStatus.PENDING, now);
    }

    public void markApproved(Instant now) {
        transitionTo(PolicyRequestStatus.APPROVED, now);
        this.finishedAt = now;
    }

    public void markRejected(Instant now) {
        transitionTo(PolicyRequestStatus.REJECTED, now);
        this.finishedAt = now;
    }

    public void cancel(Instant now) {
        transitionTo(PolicyRequestStatus.CANCELED, now);
        this.finishedAt = now;
    }

    private void transitionTo(PolicyRequestStatus newStatus, Instant now) {
        Set<PolicyRequestStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(this.status, Set.of());
        if (!allowed.contains(newStatus)) {
            throw new InvalidStateTransitionException(
                    "Invalid transition from %s to %s".formatted(this.status, newStatus)
            );
        }

        this.status = newStatus;
        this.history.add(new PolicyHistoryEntry(newStatus, now));
    }

    public boolean isFinalStatus() {
        return status == PolicyRequestStatus.APPROVED
                || status == PolicyRequestStatus.REJECTED
                || status == PolicyRequestStatus.CANCELED;
    }

    public UUID getId() { return id; }
    public UUID getCustomerId() { return customerId; }
    public Long getProductId() { return productId; }
    public PolicyCategory getCategory() { return category; }
    public SalesChannel getSalesChannel() { return salesChannel; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public PolicyRequestStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getFinishedAt() { return finishedAt; }
    public BigDecimal getTotalMonthlyPremiumAmount() { return totalMonthlyPremiumAmount; }
    public BigDecimal getInsuredAmount() { return insuredAmount; }
    public Map<String, BigDecimal> getCoverages() { return Collections.unmodifiableMap(coverages); }
    public List<String> getAssistances() { return Collections.unmodifiableList(assistances); }
    public List<PolicyHistoryEntry> getHistory() { return Collections.unmodifiableList(history); }
}