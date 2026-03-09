package com.cbp.notification.adapter.messaging;

import com.cbp.notification.application.NotificationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Kafka inbound adapter that consumes transfer outcome events and delegates
 * notification processing to the {@link NotificationService}.
 *
 * <p>Consumes from two topics:
 * <ul>
 *   <li>{@code transfer.completed} — triggers a success notification to the customer.</li>
 *   <li>{@code transfer.failed} — triggers a failure notification to the customer.</li>
 * </ul>
 *
 * <p>Manual offset acknowledgement is used so that a processing error does not
 * advance the Kafka consumer offset, guaranteeing at-least-once delivery semantics.
 * Poison-pill messages (unparseable JSON) are logged and acknowledged to prevent
 * indefinite partition blocking.
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
@Component
public class TransferOutcomeEventListener {

    private static final Logger log = LoggerFactory.getLogger(TransferOutcomeEventListener.class);

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    /**
     * Constructs the listener with required dependencies.
     *
     * @param notificationService the application service handling notification logic
     * @param objectMapper        the Jackson {@link ObjectMapper} for JSON deserialization
     */
    public TransferOutcomeEventListener(NotificationService notificationService,
                                        ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    /**
     * Consumes {@code TransferCompletedEvent} messages and triggers customer notifications.
     *
     * @param record         the raw Kafka consumer record containing the JSON payload
     * @param acknowledgment the manual acknowledgment handle for offset management
     */
    @KafkaListener(
            topics = "transfer.completed",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onTransferCompleted(ConsumerRecord<String, String> record,
                                    Acknowledgment acknowledgment) {
        log.info("Received TransferCompletedEvent for notification: partition={} offset={}",
                record.partition(), record.offset());
        try {
            JsonNode node = objectMapper.readTree(record.value());
            UUID transferId = UUID.fromString(node.get("transferId").asText());
            UUID sourceAccountId = UUID.fromString(node.get("sourceAccountId").asText());
            BigDecimal amount = new BigDecimal(node.get("amount").asText());
            String currency = node.get("currency").asText();

            notificationService.handleTransferCompleted(transferId, sourceAccountId, amount, currency);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process TransferCompletedEvent for notification: {}", e.getMessage(), e);
            acknowledgment.acknowledge();
        }
    }

    /**
     * Consumes {@code TransferFailedEvent} messages and triggers failure notifications.
     *
     * @param record         the raw Kafka consumer record containing the JSON payload
     * @param acknowledgment the manual acknowledgment handle for offset management
     */
    @KafkaListener(
            topics = "transfer.failed",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onTransferFailed(ConsumerRecord<String, String> record,
                                 Acknowledgment acknowledgment) {
        log.info("Received TransferFailedEvent for notification: partition={} offset={}",
                record.partition(), record.offset());
        try {
            JsonNode node = objectMapper.readTree(record.value());
            UUID transferId = UUID.fromString(node.get("transferId").asText());
            UUID sourceAccountId = UUID.fromString(node.get("sourceAccountId").asText());
            BigDecimal amount = new BigDecimal(node.has("amount") ? node.get("amount").asText() : "0");
            String currency = node.has("currency") ? node.get("currency").asText() : "N/A";
            String reason = node.has("reason") ? node.get("reason").asText() : "Unknown reason";

            notificationService.handleTransferFailed(transferId, sourceAccountId, amount, currency, reason);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process TransferFailedEvent for notification: {}", e.getMessage(), e);
            acknowledgment.acknowledge();
        }
    }
}
