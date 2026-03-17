package br.com.matheus.insurance.application.usecase;

import br.com.matheus.insurance.domain.model.PolicyRequest;

import java.util.UUID;

public interface GetPolicyRequestByIdUseCase {
    PolicyRequest execute(UUID id);
}