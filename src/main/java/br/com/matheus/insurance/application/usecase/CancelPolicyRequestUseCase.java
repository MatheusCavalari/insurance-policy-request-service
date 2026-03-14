package br.com.matheus.insurance.application.usecase;

import java.util.UUID;

public interface CancelPolicyRequestUseCase {
    void execute(UUID policyRequestId);
}