package com.cbp.transfer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the Transfer Service microservice.
 *
 * <p>The Transfer Service is the choreography initiator of the distributed
 * money-transfer Saga. Its responsibilities include:
 * <ul>
 *   <li><b>Idempotency enforcement:</b> each inbound transfer request must carry an
 *       {@code Idempotency-Key} HTTP header. The key and its response payload are stored
 *       in Redis for 24 hours, ensuring safe replay of identical requests without
 *       double-processing side effects.</li>
 *   <li><b>Transfer intent persistence:</b> a {@code Transfer} record is persisted
 *       in {@code transfer_db} with status {@code PENDING} before any event is emitted.</li>
 *   <li><b>Saga initiation:</b> a {@code TransferRequestedEvent} is published to the
 *       {@code transfer.requested} Kafka topic, triggering the Account Service to
 *       execute the debit/credit operations.</li>
 *   <li><b>Saga completion handling:</b> the service listens to
 *       {@code transfer.completed} and {@code transfer.failed} Kafka topics and
 *       updates the {@code Transfer} record to {@code COMPLETED} or {@code FAILED}
 *       accordingly.</li>
 *   <li><b>Outbox relay:</b> a scheduled task publishes uncommitted outbox events
 *       to Kafka, implementing the Transactional Outbox Pattern.</li>
 * </ul>
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableKafka
@EnableScheduling
public class TransferServiceApplication {

    /**
     * Application entry point.
     *
     * @param args command-line arguments passed at startup
     */
    public static void main(String[] args) {
        SpringApplication.run(TransferServiceApplication.class, args);
    }
}
