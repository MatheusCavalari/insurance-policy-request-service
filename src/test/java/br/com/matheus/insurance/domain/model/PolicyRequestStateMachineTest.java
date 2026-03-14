package br.com.matheus.insurance.domain.model;

import br.com.matheus.insurance.domain.enums.PolicyRequestStatus;
import br.com.matheus.insurance.domain.exception.InvalidStateTransitionException;
import br.com.matheus.insurance.support.PolicyRequestTestFactory;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class PolicyRequestStateMachineTest {

    @Test
    void should_start_as_received() {
        PolicyRequest request = PolicyRequestTestFactory.newPolicyRequest();

        assertEquals(PolicyRequestStatus.RECEIVED, request.getStatus());
        assertEquals(1, request.getHistory().size());
        assertEquals(PolicyRequestStatus.RECEIVED, request.getHistory().get(0).status());
        assertNull(request.getFinishedAt());
    }

    @Test
    void should_transition_from_received_to_validated() {
        PolicyRequest request = PolicyRequestTestFactory.newPolicyRequest();
        Instant now = Instant.parse("2026-03-14T10:00:10Z");

        request.markValidated(now);

        assertEquals(PolicyRequestStatus.VALIDATED, request.getStatus());
        assertEquals(2, request.getHistory().size());
        assertEquals(PolicyRequestStatus.VALIDATED, request.getHistory().get(1).status());
    }

    @Test
    void should_transition_from_validated_to_pending() {
        PolicyRequest request = PolicyRequestTestFactory.newPolicyRequest();

        request.markValidated(Instant.parse("2026-03-14T10:00:10Z"));
        request.markPending(Instant.parse("2026-03-14T10:00:20Z"));

        assertEquals(PolicyRequestStatus.PENDING, request.getStatus());
        assertEquals(3, request.getHistory().size());
        assertEquals(PolicyRequestStatus.PENDING, request.getHistory().get(2).status());
    }

    @Test
    void should_transition_from_pending_to_approved() {
        PolicyRequest request = PolicyRequestTestFactory.newPolicyRequest();

        request.markValidated(Instant.parse("2026-03-14T10:00:10Z"));
        request.markPending(Instant.parse("2026-03-14T10:00:20Z"));
        request.markApproved(Instant.parse("2026-03-14T10:00:30Z"));

        assertEquals(PolicyRequestStatus.APPROVED, request.getStatus());
        assertNotNull(request.getFinishedAt());
        assertEquals(4, request.getHistory().size());
        assertEquals(PolicyRequestStatus.APPROVED, request.getHistory().get(3).status());
    }

    @Test
    void should_transition_from_pending_to_rejected() {
        PolicyRequest request = PolicyRequestTestFactory.newPolicyRequest();

        request.markValidated(Instant.parse("2026-03-14T10:00:10Z"));
        request.markPending(Instant.parse("2026-03-14T10:00:20Z"));
        request.markRejected(Instant.parse("2026-03-14T10:00:30Z"));

        assertEquals(PolicyRequestStatus.REJECTED, request.getStatus());
        assertNotNull(request.getFinishedAt());
        assertEquals(4, request.getHistory().size());
        assertEquals(PolicyRequestStatus.REJECTED, request.getHistory().get(3).status());
    }

    @Test
    void should_transition_from_pending_to_canceled() {
        PolicyRequest request = PolicyRequestTestFactory.newPolicyRequest();

        request.markValidated(Instant.parse("2026-03-14T10:00:10Z"));
        request.markPending(Instant.parse("2026-03-14T10:00:20Z"));
        request.cancel(Instant.parse("2026-03-14T10:00:30Z"));

        assertEquals(PolicyRequestStatus.CANCELED, request.getStatus());
        assertNotNull(request.getFinishedAt());
        assertEquals(4, request.getHistory().size());
        assertEquals(PolicyRequestStatus.CANCELED, request.getHistory().get(3).status());
    }

    @Test
    void should_allow_reprocessing_pending_to_pending() {
        PolicyRequest request = PolicyRequestTestFactory.newPolicyRequest();

        request.markValidated(Instant.parse("2026-03-14T10:00:10Z"));
        request.markPending(Instant.parse("2026-03-14T10:00:20Z"));
        request.markPending(Instant.parse("2026-03-14T10:00:30Z"));

        assertEquals(PolicyRequestStatus.PENDING, request.getStatus());
        assertEquals(4, request.getHistory().size());
        assertEquals(PolicyRequestStatus.PENDING, request.getHistory().get(3).status());
    }

    @Test
    void should_not_allow_received_to_approved() {
        PolicyRequest request = PolicyRequestTestFactory.newPolicyRequest();

        assertThrows(
                InvalidStateTransitionException.class,
                () -> request.markApproved(Instant.parse("2026-03-14T10:00:10Z"))
        );
    }

    @Test
    void should_not_allow_cancel_after_approved() {
        PolicyRequest request = PolicyRequestTestFactory.newPolicyRequest();

        request.markValidated(Instant.parse("2026-03-14T10:00:10Z"));
        request.markPending(Instant.parse("2026-03-14T10:00:20Z"));
        request.markApproved(Instant.parse("2026-03-14T10:00:30Z"));

        assertThrows(
                InvalidStateTransitionException.class,
                () -> request.cancel(Instant.parse("2026-03-14T10:00:40Z"))
        );
    }

    @Test
    void should_not_allow_any_transition_after_rejected() {
        PolicyRequest request = PolicyRequestTestFactory.newPolicyRequest();

        request.markRejected(Instant.parse("2026-03-14T10:00:10Z"));

        assertThrows(
                InvalidStateTransitionException.class,
                () -> request.markValidated(Instant.parse("2026-03-14T10:00:20Z"))
        );
    }

    @Test
    void should_not_allow_any_transition_after_canceled() {
        PolicyRequest request = PolicyRequestTestFactory.newPolicyRequest();

        request.cancel(Instant.parse("2026-03-14T10:00:10Z"));

        assertThrows(
                InvalidStateTransitionException.class,
                () -> request.markValidated(Instant.parse("2026-03-14T10:00:20Z"))
        );
    }

    @Test
    void should_identify_final_statuses() {
        PolicyRequest approved = PolicyRequestTestFactory.restoredApprovedPolicyRequest();
        PolicyRequest pending = PolicyRequestTestFactory.restoredPendingPolicyRequest();

        assertTrue(approved.isFinalStatus());
        assertFalse(pending.isFinalStatus());
    }
}