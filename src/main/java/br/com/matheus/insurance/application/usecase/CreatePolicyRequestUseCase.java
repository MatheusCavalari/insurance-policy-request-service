package br.com.matheus.insurance.application.usecase;

import br.com.matheus.insurance.application.dto.CreatePolicyRequestCommand;
import br.com.matheus.insurance.application.dto.CreatePolicyRequestResult;

public interface CreatePolicyRequestUseCase {
    CreatePolicyRequestResult execute(CreatePolicyRequestCommand command);
}