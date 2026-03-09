package com.cbp.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the Account Service microservice.
 *
 * <p>The Account Service owns the {@code account_db} bounded context and is the
 * authoritative source of truth for all account lifecycle operations:
 * <ul>
 *   <li>Account creation and status management (ACTIVE, FROZEN, CLOSED).</li>
 *   <li>Balance enquiry and transaction history.</li>
 *   <li>Participating in distributed money-transfer Sagas by consuming
 *       {@code TransferRequestedEvent} events from Kafka, applying debit/credit
 *       operations atomically, and publishing the resulting outcome events.</li>
 *   <li>Outbox event relay — a scheduled task reads uncommitted domain events
 *       from the {@code outbox_events} table and publishes them to Kafka,
 *       guaranteeing at-least-once delivery without dual-write risks.</li>
 * </ul>
 *
 * <p>The service is structured following <em>Hexagonal Architecture</em> (Ports &amp; Adapters):
 * all business logic lives in the {@code domain} and {@code application} layers, completely
 * isolated from infrastructure concerns (HTTP, JPA, Kafka).
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 * @see <a href="https://alistair.cockburn.us/hexagonal-architecture/">Hexagonal Architecture</a>
 */
@SpringBootApplication
@EnableKafka
@EnableScheduling
public class AccountServiceApplication {

    /**
     * Application entry point.
     *
     * @param args command-line arguments passed at startup
     */
    public static void main(String[] args) {
        SpringApplication.run(AccountServiceApplication.class, args);
    }
}
