package br.com.matheus.insurance.domain.port;

import br.com.matheus.insurance.domain.model.FraudAnalysis;
import br.com.matheus.insurance.domain.model.PolicyRequest;

public interface FraudAnalysisGateway {

    FraudAnalysis analyze(PolicyRequest policyRequest);
}