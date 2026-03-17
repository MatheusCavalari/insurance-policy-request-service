package br.com.matheus.insurance.infrastructure.persistence;

import br.com.matheus.insurance.domain.model.PolicyRequest;
import br.com.matheus.insurance.infrastructure.persistence.mapper.PolicyRequestPersistenceMapper;
import br.com.matheus.insurance.support.PolicyRequestTestFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers(disabledWithoutDocker = true)
@DataJpaTest
@Import({PolicyRequestRepositoryImpl.class, PolicyRequestPersistenceMapper.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PolicyRequestRepositoryIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("insurance_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> true);
    }

    @Autowired
    private PolicyRequestRepositoryImpl repository;

    @Test
    void should_save_and_find_policy_request() {
        PolicyRequest request = PolicyRequestTestFactory.restoredPendingWithPaymentApproved();

        repository.save(request);

        Optional<PolicyRequest> found = repository.findById(request.getId());

        assertTrue(found.isPresent());
        assertEquals(request.getId(), found.get().getId());
        assertEquals(request.getStatus(), found.get().getStatus());
        assertEquals(request.getPaymentStatus(), found.get().getPaymentStatus());
        assertEquals(request.getUnderwritingStatus(), found.get().getUnderwritingStatus());
        assertFalse(found.get().getHistory().isEmpty());
    }
}