package com.cbp.account.domain.port.out;

import com.cbp.account.domain.event.DomainEvent;

/**
 * Secondary outbound port for publishing domain events to the event bus.
 *
 * <p>The application layer uses this port to emit domain events without having
 * any knowledge of the underlying messaging infrastructure (Kafka, RabbitMQ, etc.).
 * The concrete implementation is provided by the messaging adapter
 * ({@code KafkaEventPublisherAdapter}).
 *
 * <p>In the context of the Transactional Outbox Pattern, implementations of this
 * port may first persist the event to the {@code outbox_events} table within the
 * same local transaction, then relay it asynchronously to Kafka — guaranteeing
 * exactly-once semantic delivery without distributed transaction managers.
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 * @see DomainEvent
 */
public interface EventPublisherPort {

    /**
     * Publishes a domain event to the configured event stream.
     *
     * <p>The event is identified by its {@link DomainEvent#getEventType()} value,
     * which the messaging adapter uses to route it to the correct Kafka topic.
     *
     * @param event the domain event to publish; must not be null
     */
    void publish(DomainEvent event);
}
