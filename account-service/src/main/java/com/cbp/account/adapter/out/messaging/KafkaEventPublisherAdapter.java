package com.cbp.account.adapter.out.messaging;

import com.cbp.account.domain.event.DomainEvent;
import com.cbp.account.domain.event.TransferCompletedEvent;
import com.cbp.account.domain.event.TransferFailedEvent;
import com.cbp.account.domain.port.out.EventPublisherPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * Kafka-based implementation of the {@link EventPublisherPort} outbound port.
 *
 * <p>Routes each {@link DomainEvent} to the appropriate Kafka topic based on
 * its {@link DomainEvent#getEventType()} discriminator. The event payload is
 * serialized to JSON and the {@code eventType} is set as a Kafka message header
 * to allow consumers to deserialize the correct type without inspecting the body.
 *
 * <p>Uses {@link KafkaTemplate} for fire-and-forget publishing with internal
 * async acknowledgement logging. Production deployments should configure a
 * {@code DeliverySemantics} policy (idempotent producer, acks=all) in the Kafka
 * producer configuration.
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
@Component
public class KafkaEventPublisherAdapter implements EventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisherAdapter.class);

    /** Kafka topic for successful transfer completion events. */
    public static final String TOPIC_TRANSFER_COMPLETED = "transfer.completed";

    /** Kafka topic for transfer failure events. */
    public static final String TOPIC_TRANSFER_FAILED = "transfer.failed";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Constructs the Kafka event publisher adapter.
     *
     * @param kafkaTemplate the Spring Kafka template for producing messages
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
     * <p>Routes the event to the correct topic and serializes it to JSON.
     * The Kafka message key is set to the {@code transferId} to ensure all
     * events for the same transfer land on the same partition, preserving order.
     *
     * @throws IllegalArgumentException if the event type is unknown or serialization fails
     */
    @Override
    public void publish(DomainEvent event) {
        String topic = resolveTopic(event);
        String key = resolveKey(event);

        try {
            String payload = objectMapper.writeValueAsString(event);
            Message<String> message = MessageBuilder
                    .withPayload(payload)
                    .setHeader(KafkaHeaders.TOPIC, topic)
                    .setHeader(KafkaHeaders.KEY, key)
                    .setHeader("eventType", event.getEventType())
                    .build();

            kafkaTemplate.send(message).whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish event [{}] to topic [{}]: {}",
                            event.getEventType(), topic, ex.getMessage(), ex);
                } else {
                    log.info("Published event [{}] id={} to topic [{}] partition={} offset={}",
                            event.getEventType(), event.getEventId(), topic,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                }
            });
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(
                    "Failed to serialize event: " + event.getEventType(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private String resolveTopic(DomainEvent event) {
        return switch (event) {
            case TransferCompletedEvent ignored -> TOPIC_TRANSFER_COMPLETED;
            case TransferFailedEvent ignored -> TOPIC_TRANSFER_FAILED;
            default -> throw new IllegalArgumentException(
                    "Unknown event type: " + event.getEventType());
        };
    }

    private String resolveKey(DomainEvent event) {
        return switch (event) {
            case TransferCompletedEvent e -> e.transferId().toString();
            case TransferFailedEvent e -> e.transferId().toString();
            default -> event.getEventId().toString();
        };
    }
}
