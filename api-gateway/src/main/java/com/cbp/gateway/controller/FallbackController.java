package com.cbp.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

/**
 * Circuit-breaker fallback controller for downstream microservices.
 *
 * <p>When a downstream service (account-service, transfer-service) becomes unavailable
 * or exceeds the circuit-breaker failure threshold, Spring Cloud Gateway forwards the
 * request to the corresponding endpoint in this controller instead of propagating
 * a raw connection error to the client.
 *
 * <p>Each fallback endpoint returns a structured JSON error body containing:
 * <ul>
 *   <li>A machine-readable {@code error} code.</li>
 *   <li>A human-readable {@code message}.</li>
 *   <li>The {@code timestamp} of the fallback invocation (ISO-8601).</li>
 * </ul>
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    /**
     * Fallback endpoint for the Account Service circuit breaker.
     *
     * <p>Returns HTTP {@code 503 Service Unavailable} with a descriptive JSON body
     * when the account-service cannot be reached.
     *
     * @return a {@link Mono} emitting a {@link ResponseEntity} with a 503 status
     *         and a structured error payload
     */
    @GetMapping("/account")
    public Mono<ResponseEntity<Map<String, Object>>> accountFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "ACCOUNT_SERVICE_UNAVAILABLE",
                        "message", "The Account Service is temporarily unavailable. Please try again later.",
                        "timestamp", Instant.now().toString()
                )));
    }

    /**
     * Fallback endpoint for the Transfer Service circuit breaker.
     *
     * <p>Returns HTTP {@code 503 Service Unavailable} with a descriptive JSON body
     * when the transfer-service cannot be reached.
     *
     * @return a {@link Mono} emitting a {@link ResponseEntity} with a 503 status
     *         and a structured error payload
     */
    @GetMapping("/transfer")
    public Mono<ResponseEntity<Map<String, Object>>> transferFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "TRANSFER_SERVICE_UNAVAILABLE",
                        "message", "The Transfer Service is temporarily unavailable. Please try again later.",
                        "timestamp", Instant.now().toString()
                )));
    }
}
