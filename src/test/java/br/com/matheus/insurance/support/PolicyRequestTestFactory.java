package br.com.matheus.insurance.support;

import br.com.matheus.insurance.application.dto.CreatePolicyRequestCommand;
import br.com.matheus.insurance.domain.enums.PaymentMethod;
import br.com.matheus.insurance.domain.enums.PolicyCategory;
import br.com.matheus.insurance.domain.enums.PolicyRequestStatus;
import br.com.matheus.insurance.domain.enums.SalesChannel;
import br.com.matheus.insurance.domain.model.PolicyRequest;
import br.com.matheus.insurance.domain.valueobject.PolicyHistoryEntry;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class PolicyRequestTestFactory {

    private PolicyRequestTestFactory() {
    }

    public static PolicyRequest newPolicyRequest() {
        return PolicyRequest.create(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                123L,
                PolicyCategory.AUTO,
                SalesChannel.MOBILE,
                PaymentMethod.CREDIT_CARD,
                new BigDecimal("75.25"),
                new BigDecimal("200000.00"),
                Map.of(
                        "Roubo", new BigDecimal("100000.00"),
                        "Perda Total", new BigDecimal("100000.00")
                ),
                List.of("Guincho 24h", "Chaveiro"),
                Instant.parse("2026-03-14T10:00:00Z")
        );
    }

    public static PolicyRequest newPolicyRequest(PolicyCategory category, BigDecimal insuredAmount) {
        return PolicyRequest.create(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                123L,
                category,
                SalesChannel.MOBILE,
                PaymentMethod.CREDIT_CARD,
                new BigDecimal("75.25"),
                insuredAmount,
                Map.of(
                        "Cobertura Básica", insuredAmount
                ),
                List.of("Guincho 24h"),
                Instant.parse("2026-03-14T10:00:00Z")
        );
    }

    public static PolicyRequest restoredPendingPolicyRequest() {
        return PolicyRequest.restore(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                123L,
                PolicyCategory.AUTO,
                SalesChannel.MOBILE,
                PaymentMethod.CREDIT_CARD,
                PolicyRequestStatus.PENDING,
                Instant.parse("2026-03-14T10:00:00Z"),
                null,
                new BigDecimal("75.25"),
                new BigDecimal("200000.00"),
                Map.of("Cobertura Básica", new BigDecimal("200000.00")),
                List.of("Guincho 24h"),
                List.of(
                        new PolicyHistoryEntry(PolicyRequestStatus.RECEIVED, Instant.parse("2026-03-14T10:00:00Z")),
                        new PolicyHistoryEntry(PolicyRequestStatus.VALIDATED, Instant.parse("2026-03-14T10:00:10Z")),
                        new PolicyHistoryEntry(PolicyRequestStatus.PENDING, Instant.parse("2026-03-14T10:00:20Z"))
                )
        );
    }

    public static PolicyRequest restoredApprovedPolicyRequest() {
        return PolicyRequest.restore(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                123L,
                PolicyCategory.AUTO,
                SalesChannel.MOBILE,
                PaymentMethod.CREDIT_CARD,
                PolicyRequestStatus.APPROVED,
                Instant.parse("2026-03-14T10:00:00Z"),
                Instant.parse("2026-03-14T10:01:00Z"),
                new BigDecimal("75.25"),
                new BigDecimal("200000.00"),
                Map.of("Cobertura Básica", new BigDecimal("200000.00")),
                List.of("Guincho 24h"),
                List.of(
                        new PolicyHistoryEntry(PolicyRequestStatus.RECEIVED, Instant.parse("2026-03-14T10:00:00Z")),
                        new PolicyHistoryEntry(PolicyRequestStatus.VALIDATED, Instant.parse("2026-03-14T10:00:10Z")),
                        new PolicyHistoryEntry(PolicyRequestStatus.PENDING, Instant.parse("2026-03-14T10:00:20Z")),
                        new PolicyHistoryEntry(PolicyRequestStatus.APPROVED, Instant.parse("2026-03-14T10:01:00Z"))
                )
        );
    }

    public static CreatePolicyRequestCommand newCreateCommand() {
        return new CreatePolicyRequestCommand(
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                123L,
                PolicyCategory.AUTO,
                SalesChannel.MOBILE,
                PaymentMethod.CREDIT_CARD,
                new BigDecimal("75.25"),
                new BigDecimal("200000.00"),
                Map.of(
                        "Roubo", new BigDecimal("100000.00"),
                        "Perda Total", new BigDecimal("100000.00")
                ),
                List.of("Guincho 24h", "Chaveiro")
        );
    }
}