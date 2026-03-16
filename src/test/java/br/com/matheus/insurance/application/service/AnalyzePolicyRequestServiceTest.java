package br.com.matheus.insurance.application.service;

import br.com.matheus.insurance.domain.enums.PolicyCategory;
import br.com.matheus.insurance.domain.enums.PolicyRequestStatus;
import br.com.matheus.insurance.domain.enums.RiskClassification;
import br.com.matheus.insurance.domain.exception.ResourceNotFoundException;
import br.com.matheus.insurance.domain.model.FraudAnalysis;
import br.com.matheus.insurance.domain.model.PolicyRequest;
import br.com.matheus.insurance.domain.port.FraudAnalysisGateway;
import br.com.matheus.insurance.domain.port.PolicyRequestRepository;
import br.com.matheus.insurance.domain.rule.*;
import br.com.matheus.insurance.infrastructure.messaging.OutboxEventFactory;
import br.com.matheus.insurance.support.PolicyRequestTestFactory;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AnalyzePolicyRequestServiceTest {

    @Test
    void should_validate_and_move_to_pending_when_rule_is_approved() {
        PolicyRequestRepository repository = mock(PolicyRequestRepository.class);
        FraudAnalysisGateway fraudAnalysisGateway = mock(FraudAnalysisGateway.class);
        OutboxEventFactory outboxEventFactory = mock(OutboxEventFactory.class);

        RiskValidationStrategyFactory factory = new RiskValidationStrategyFactory(
                List.of(
                        new RegularRiskValidationStrategy(),
                        new HighRiskValidationStrategy(),
                        new PreferredRiskValidationStrategy(),
                        new NoInformationRiskValidationStrategy()
                )
        );

        AnalyzePolicyRequestService service =
                new AnalyzePolicyRequestService(repository, fraudAnalysisGateway, factory, outboxEventFactory);

        PolicyRequest request = PolicyRequestTestFactory.newPolicyRequest();

        when(repository.findById(request.getId())).thenReturn(Optional.of(request));
        when(repository.save(any(PolicyRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(fraudAnalysisGateway.analyze(request)).thenReturn(
                new FraudAnalysis(
                        request.getId(),
                        request.getCustomerId(),
                        Instant.parse("2026-03-14T10:00:10Z"),
                        RiskClassification.REGULAR,
                        List.of()
                )
        );

        service.execute(request.getId());

        ArgumentCaptor<PolicyRequest> captor = ArgumentCaptor.forClass(PolicyRequest.class);
        verify(repository).save(captor.capture());

        PolicyRequest saved = captor.getValue();

        assertEquals(PolicyRequestStatus.PENDING, saved.getStatus());
        assertEquals(3, saved.getHistory().size());
        assertEquals(PolicyRequestStatus.RECEIVED, saved.getHistory().get(0).status());
        assertEquals(PolicyRequestStatus.VALIDATED, saved.getHistory().get(1).status());
        assertEquals(PolicyRequestStatus.PENDING, saved.getHistory().get(2).status());
        assertNull(saved.getFinishedAt());

        verify(outboxEventFactory, times(2))
                .appendPolicyStatusChanged(any(PolicyRequest.class), any(Instant.class));
    }

    @Test
    void should_reject_when_rule_is_not_approved() {
        PolicyRequestRepository repository = mock(PolicyRequestRepository.class);
        FraudAnalysisGateway fraudAnalysisGateway = mock(FraudAnalysisGateway.class);
        OutboxEventFactory outboxEventFactory = mock(OutboxEventFactory.class);

        RiskValidationStrategyFactory factory = new RiskValidationStrategyFactory(
                List.of(
                        new RegularRiskValidationStrategy(),
                        new HighRiskValidationStrategy(),
                        new PreferredRiskValidationStrategy(),
                        new NoInformationRiskValidationStrategy()
                )
        );

        AnalyzePolicyRequestService service =
                new AnalyzePolicyRequestService(repository, fraudAnalysisGateway, factory, outboxEventFactory);

        PolicyRequest request = PolicyRequestTestFactory.newPolicyRequest(
                PolicyCategory.AUTO,
                new BigDecimal("100000.00")
        );

        when(repository.findById(request.getId())).thenReturn(Optional.of(request));
        when(repository.save(any(PolicyRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(fraudAnalysisGateway.analyze(request)).thenReturn(
                new FraudAnalysis(
                        request.getId(),
                        request.getCustomerId(),
                        Instant.parse("2026-03-14T10:00:10Z"),
                        RiskClassification.NO_INFORMATION,
                        List.of()
                )
        );

        service.execute(request.getId());

        ArgumentCaptor<PolicyRequest> captor = ArgumentCaptor.forClass(PolicyRequest.class);
        verify(repository).save(captor.capture());

        PolicyRequest saved = captor.getValue();

        assertEquals(PolicyRequestStatus.REJECTED, saved.getStatus());
        assertEquals(2, saved.getHistory().size());
        assertEquals(PolicyRequestStatus.REJECTED, saved.getHistory().get(1).status());
        assertNotNull(saved.getFinishedAt());

        verify(outboxEventFactory, times(1))
                .appendPolicyStatusChanged(any(PolicyRequest.class), any(Instant.class));
    }

    @Test
    void should_throw_when_policy_request_does_not_exist() {
        PolicyRequestRepository repository = mock(PolicyRequestRepository.class);
        FraudAnalysisGateway fraudAnalysisGateway = mock(FraudAnalysisGateway.class);
        OutboxEventFactory outboxEventFactory = mock(OutboxEventFactory.class);

        RiskValidationStrategyFactory factory = new RiskValidationStrategyFactory(
                List.of(new RegularRiskValidationStrategy())
        );

        AnalyzePolicyRequestService service =
                new AnalyzePolicyRequestService(repository, fraudAnalysisGateway, factory, outboxEventFactory);

        UUID id = UUID.randomUUID();

        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.execute(id));

        verify(repository, never()).save(any());
        verifyNoInteractions(fraudAnalysisGateway);
        verifyNoInteractions(outboxEventFactory);
    }
}
