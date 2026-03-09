package com.cbp.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test that verifies the API Gateway application context loads successfully.
 *
 * <p>Uses the {@code test} profile to override infrastructure dependencies
 * (Redis, downstream services) with in-memory or embedded alternatives,
 * ensuring the context can start in a CI environment without external services.
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ApiGatewayApplicationTest {

    /**
     * Asserts that the Spring application context starts without errors.
     * Validates that all beans are wired correctly and no configuration is missing.
     */
    @Test
    void contextLoads() {
        // If the application context fails to start, this test will fail automatically.
    }
}
