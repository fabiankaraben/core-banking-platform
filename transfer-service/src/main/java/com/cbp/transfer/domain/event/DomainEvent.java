package com.cbp.transfer.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Base interface for all domain events emitted by the Transfer Service.
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
public interface DomainEvent {

    /**
     * Returns the globally unique identifier for this event occurrence.
     *
     * @return the event UUID
     */
    UUID getEventId();

    /**
     * Returns the event type discriminator used for Kafka topic routing.
     *
     * @return the event type string (e.g., {@code "TransferRequestedEvent"})
     */
    String getEventType();

    /**
     * Returns the UTC timestamp at which this event was created.
     *
     * @return the event creation timestamp
     */
    Instant getOccurredAt();
}
