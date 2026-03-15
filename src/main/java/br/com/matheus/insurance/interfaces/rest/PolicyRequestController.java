package br.com.matheus.insurance.interfaces.rest;

import br.com.matheus.insurance.application.dto.CreatePolicyRequestResult;
import br.com.matheus.insurance.application.usecase.CancelPolicyRequestUseCase;
import br.com.matheus.insurance.application.usecase.CreatePolicyRequestUseCase;
import br.com.matheus.insurance.application.usecase.GetPolicyRequestByIdUseCase;
import br.com.matheus.insurance.application.usecase.GetPolicyRequestsByCustomerIdUseCase;
import br.com.matheus.insurance.domain.model.PolicyRequest;
import br.com.matheus.insurance.interfaces.rest.dto.CreatePolicyRequestHttpRequest;
import br.com.matheus.insurance.interfaces.rest.dto.PolicyRequestHttpResponse;
import br.com.matheus.insurance.interfaces.rest.mapper.PolicyRequestHttpMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/policy-requests")
public class PolicyRequestController {

    private final CreatePolicyRequestUseCase createPolicyRequestUseCase;
    private final GetPolicyRequestByIdUseCase getPolicyRequestByIdUseCase;
    private final GetPolicyRequestsByCustomerIdUseCase getPolicyRequestsByCustomerIdUseCase;
    private final CancelPolicyRequestUseCase cancelPolicyRequestUseCase;
    private final PolicyRequestHttpMapper mapper;

    public PolicyRequestController(
            CreatePolicyRequestUseCase createPolicyRequestUseCase,
            GetPolicyRequestByIdUseCase getPolicyRequestByIdUseCase,
            GetPolicyRequestsByCustomerIdUseCase getPolicyRequestsByCustomerIdUseCase,
            CancelPolicyRequestUseCase cancelPolicyRequestUseCase,
            PolicyRequestHttpMapper mapper
    ) {
        this.createPolicyRequestUseCase = createPolicyRequestUseCase;
        this.getPolicyRequestByIdUseCase = getPolicyRequestByIdUseCase;
        this.getPolicyRequestsByCustomerIdUseCase = getPolicyRequestsByCustomerIdUseCase;
        this.cancelPolicyRequestUseCase = cancelPolicyRequestUseCase;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<CreatePolicyRequestResult> create(@Valid @RequestBody CreatePolicyRequestHttpRequest request) {
        CreatePolicyRequestResult result = createPolicyRequestUseCase.execute(mapper.toCommand(request));
        return ResponseEntity.created(URI.create("/policy-requests/" + result.id())).body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PolicyRequestHttpResponse> getById(@PathVariable UUID id) {
        PolicyRequest policyRequest = getPolicyRequestByIdUseCase.execute(id);
        return ResponseEntity.ok(mapper.toResponse(policyRequest));
    }

    @GetMapping
    public ResponseEntity<List<PolicyRequestHttpResponse>> getByCustomerId(@RequestParam UUID customerId) {
        List<PolicyRequestHttpResponse> response = getPolicyRequestsByCustomerIdUseCase.execute(customerId)
                .stream()
                .map(mapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable UUID id) {
        cancelPolicyRequestUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
