package com.cbp.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the API Gateway microservice.
 *
 * <p>The API Gateway serves as the single entry point for all external HTTP traffic in the
 * Core Banking Platform. It is responsible for:
 * <ul>
 *   <li>Routing incoming requests to the appropriate downstream microservices.</li>
 *   <li>Enforcing global rate limiting using a Redis-backed token-bucket algorithm.</li>
 *   <li>Injecting distributed tracing headers ({@code traceId}, {@code spanId}) into
 *       every forwarded request, enabling end-to-end observability across the platform.</li>
 *   <li>Exposing aggregated health and Prometheus metrics endpoints.</li>
 * </ul>
 *
 * <p>Built on top of Spring Cloud Gateway (reactive, non-blocking), this service handles
 * high-throughput traffic without blocking threads, making it suitable for production
 * financial workloads.
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
@SpringBootApplication
public class ApiGatewayApplication {

    /**
     * Application entry point.
     *
     * @param args command-line arguments passed at startup
     */
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
