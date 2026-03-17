package br.com.matheus.insurance.application.usecase;

import java.util.UUID;

public interface AnalyzePolicyRequestUseCase {
    void execute(UUID policyRequestId);
}