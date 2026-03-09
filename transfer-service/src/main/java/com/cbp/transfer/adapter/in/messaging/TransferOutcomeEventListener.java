package com.cbp.transfer.adapter.in.messaging;

import com.cbp.transfer.domain.port.in.TransferUseCase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Kafka inbound adapter that listens for transfer outcome events published by the Account Service.
 *
 * <p>Consumes from two topics:
 * <ul>
 *   <li>{@code transfer.completed} — {@code TransferCompletedEvent}: marks the transfer
 *       as {@code COMPLETED} in the local database.</li>
 *   <li>{@code transfer.failed} — {@code TransferFailedEvent}: marks the transfer as
 *       {@code FAILED} and records the failure reason.</li>
 * </ul>
 *
 * <p>Manual acknowledgement is used to avoid advancing the Kafka offset on processing errors,
 * ensuring at-least-once delivery guarantees across service restarts.
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
@Component
public class TransferOutcomeEventListener {

    private static final Logger log = LoggerFactory.getLogger(TransferOutcomeEventListener.class);

    private final TransferUseCase transferUseCase;
    private final ObjectMapper objectMapper;

    /**
     * Constructs the listener with required dependencies.
     *
     * @param transferUseCase the inbound port for updating transfer status
     * @param objectMapper    the Jackson {@link ObjectMapper} for JSON deserialization
     */
    public TransferOutcomeEventListener(TransferUseCase transferUseCase,
                                        ObjectMapper objectMapper) {
        this.transferUseCase = transferUseCase;
        this.objectMapper = objectMapper;
    }

    /**
     * Consumes a {@code TransferCompletedEvent} from the {@code transfer.completed} topic
     * and marks the corresponding transfer as {@code COMPLETED}.
     *
     * @param record         the raw Kafka consumer record
     * @param acknowledgment the manual acknowledgment handle
     */
    @KafkaListener(
            topics = "transfer.completed",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onTransferCompleted(ConsumerRecord<String, String> record,
                                    Acknowledgment acknowledgment) {
        log.info("Received TransferCompletedEvent from partition={} offset={}",
                record.partition(), record.offset());
        try {
            JsonNode node = objectMapper.readTree(record.value());
            UUID transferId = UUID.fromString(node.get("transferId").asText());
            transferUseCase.markTransferCompleted(transferId);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process TransferCompletedEvent: {}", e.getMessage(), e);
            acknowledgment.acknowledge();
        }
    }

    /**
     * Consumes a {@code TransferFailedEvent} from the {@code transfer.failed} topic
     * and marks the corresponding transfer as {@code FAILED}.
     *
     * @param record         the raw Kafka consumer record
     * @param acknowledgment the manual acknowledgment handle
     */
    @KafkaListener(
            topics = "transfer.failed",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onTransferFailed(ConsumerRecord<String, String> record,
                                 Acknowledgment acknowledgment) {
        log.info("Received TransferFailedEvent from partition={} offset={}",
                record.partition(), record.offset());
        try {
            JsonNode node = objectMapper.readTree(record.value());
            UUID transferId = UUID.fromString(node.get("transferId").asText());
            String reason = node.has("reason") ? node.get("reason").asText() : "Unknown reason";
            transferUseCase.markTransferFailed(transferId, reason);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process TransferFailedEvent: {}", e.getMessage(), e);
            acknowledgment.acknowledge();
        }
    }
}
