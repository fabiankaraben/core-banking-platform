package com.cbp.transfer.adapter.out.messaging;

import com.cbp.transfer.domain.event.DomainEvent;
import com.cbp.transfer.domain.event.TransferRequestedEvent;
import com.cbp.transfer.domain.port.out.EventPublisherPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * Kafka-based implementation of the {@link EventPublisherPort} outbound port.
 *
 * <p>Publishes {@link TransferRequestedEvent} messages to the {@code transfer.requested}
 * Kafka topic. The message key is set to the {@code transferId} to ensure all events
 * for a given Saga land on the same partition, preserving order for downstream consumers.
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
@Component
public class KafkaEventPublisherAdapter implements EventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisherAdapter.class);

    /** Kafka topic consumed by the Account Service to execute debit/credit operations. */
    public static final String TOPIC_TRANSFER_REQUESTED = "transfer.requested";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Constructs the adapter with required dependencies.
     *
     * @param kafkaTemplate the Spring Kafka template for sending messages
     * @param objectMapper  the Jackson {@link ObjectMapper} for JSON serialization
     */
    public KafkaEventPublisherAdapter(KafkaTemplate<String, String> kafkaTemplate,
                                      ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Serializes the event to JSON, sets the {@code transferId} as the Kafka message
     * key for partition affinity, and sends to the {@code transfer.requested} topic.
     */
    @Override
    public void publish(DomainEvent event) {
        String topic = resolveTopic(event);
        String key = resolveKey(event);

        try {
            String payload = objectMapper.writeValueAsString(event);
            var message = MessageBuilder
                    .withPayload(payload)
                    .setHeader(KafkaHeaders.TOPIC, topic)
                    .setHeader(KafkaHeaders.KEY, key)
                    .setHeader("eventType", event.getEventType())
                    .build();

            kafkaTemplate.send(message).whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish [{}] to [{}]: {}", event.getEventType(), topic, ex.getMessage(), ex);
                } else {
                    log.info("Published [{}] id={} to [{}] partition={} offset={}",
                            event.getEventType(), event.getEventId(), topic,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                }
            });
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize event: " + event.getEventType(), e);
        }
    }

    private String resolveTopic(DomainEvent event) {
        return switch (event) {
            case TransferRequestedEvent ignored -> TOPIC_TRANSFER_REQUESTED;
            default -> throw new IllegalArgumentException("Unknown event type: " + event.getEventType());
        };
    }

    private String resolveKey(DomainEvent event) {
        return switch (event) {
            case TransferRequestedEvent e -> e.transferId().toString();
            default -> event.getEventId().toString();
        };
    }
}
