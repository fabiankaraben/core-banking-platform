package com.cbp.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Entry point for the Notification Service microservice.
 *
 * <p>The Notification Service is a pure event-consumer in the Core Banking Platform.
 * It listens to transfer outcome events published by the Account Service and:
 * <ul>
 *   <li>Simulates dispatching customer notifications (email / SMS) for completed
 *       and failed transfers.</li>
 *   <li>Persists every notification attempt as an immutable audit record in the
 *       dedicated {@code notification_db} PostgreSQL database, enabling compliance
 *       and support teams to verify notification delivery history.</li>
 * </ul>
 *
 * <p>This service has no inbound HTTP API for business operations — its only
 * inbound interface is Kafka. The sole HTTP surface is the Spring Boot Actuator
 * used for health probes and Prometheus metrics scraping.
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableKafka
public class NotificationServiceApplication {

    /**
     * Application entry point.
     *
     * @param args command-line arguments passed at startup
     */
    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
