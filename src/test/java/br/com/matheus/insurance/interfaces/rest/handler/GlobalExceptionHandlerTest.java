package br.com.matheus.insurance.interfaces.rest.handler;

import br.com.matheus.insurance.domain.exception.InvalidStateTransitionException;
import br.com.matheus.insurance.domain.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new DummyController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void should_return_404_when_resource_not_found() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("policy request not found"))
                .andExpect(jsonPath("$.path").value("/test/not-found"));
    }

    @Test
    void should_return_400_when_illegal_argument() throws Exception {
        mockMvc.perform(get("/test/bad-request"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("invalid input"));
    }

    @Test
    void should_return_409_when_invalid_state_transition() throws Exception {
        mockMvc.perform(get("/test/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("invalid transition"));
    }

    @Test
    void should_return_400_when_validation_fails() throws Exception {
        mockMvc.perform(post("/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors.name").exists());
    }

    @Test
    void should_return_500_when_unexpected_error_happens() throws Exception {
        mockMvc.perform(get("/test/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Unexpected internal error"));
    }

    @RestController
    @RequestMapping("/test")
    static class DummyController {

        @GetMapping("/not-found")
        String notFound() {
            throw new ResourceNotFoundException("policy request not found");
        }

        @GetMapping("/bad-request")
        String badRequest() {
            throw new IllegalArgumentException("invalid input");
        }

        @GetMapping("/conflict")
        String conflict() {
            throw new InvalidStateTransitionException("invalid transition");
        }

        @GetMapping("/unexpected")
        String unexpected() {
            throw new RuntimeException("boom");
        }

        @PostMapping("/validate")
        String validate(@Valid @RequestBody DummyRequest request) {
            return "ok";
        }
    }

    static class DummyRequest {
        @NotBlank
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}