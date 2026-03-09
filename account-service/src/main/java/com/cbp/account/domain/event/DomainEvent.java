package com.cbp.account.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Base interface for all domain events in the Account Service.
 *
 * <p>Domain events represent meaningful state changes that have occurred within
 * the account bounded context. They are published to Kafka topics via the
 * {@link com.cbp.account.domain.port.out.EventPublisherPort} and consumed by
 * other services participating in the choreography-based Saga.
 *
 * <p>All implementations should be immutable and serializable to JSON.
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
public interface DomainEvent {

    /**
     * Returns the globally unique identifier for this specific event occurrence.
     *
     * @return the event's UUID
     */
    UUID getEventId();

    /**
     * Returns the type discriminator used to route the event to the correct
     * Kafka topic and deserialize it on the consumer side.
     *
     * @return the event type string (e.g., {@code "TransferCompletedEvent"})
     */
    String getEventType();

    /**
     * Returns the UTC timestamp at which this event was created.
     *
     * @return the event creation timestamp
     */
    Instant getOccurredAt();
}
