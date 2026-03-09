package com.cbp.account.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event emitted by the Account Service when a transfer cannot be completed
 * due to a business rule violation (e.g., insufficient funds, frozen account).
 *
 * <p>This event represents the compensating signal in the choreography-based
 * Transfer Saga, allowing downstream consumers to react appropriately:
 * <ul>
 *   <li>The Transfer Service marks the transfer record as {@code FAILED}.</li>
 *   <li>The Notification Service sends a failure notification to the customer.</li>
 * </ul>
 *
 * @param eventId         globally unique identifier for this event occurrence
 * @param transferId      the saga correlation ID linking all events in this transfer
 * @param sourceAccountId UUID of the account from which the debit was attempted
 * @param targetAccountId UUID of the intended recipient account
 * @param reason          a human-readable description of why the transfer failed
 * @param occurredAt      UTC timestamp of the event creation
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
public record TransferFailedEvent(
        UUID eventId,
        UUID transferId,
        UUID sourceAccountId,
        UUID targetAccountId,
        String reason,
        Instant occurredAt
) implements DomainEvent {

    /**
     * Factory method to create a new {@code TransferFailedEvent} with
     * auto-generated event ID and current timestamp.
     *
     * @param transferId      the saga correlation identifier
     * @param sourceAccountId the UUID of the source account
     * @param targetAccountId the UUID of the target account
     * @param reason          a human-readable failure reason
     * @return a new {@code TransferFailedEvent}
     */
    public static TransferFailedEvent of(UUID transferId, UUID sourceAccountId,
                                         UUID targetAccountId, String reason) {
        return new TransferFailedEvent(
                UUID.randomUUID(), transferId, sourceAccountId, targetAccountId,
                reason, Instant.now()
        );
    }

    @Override
    public UUID getEventId() { return eventId; }

    @Override
    public String getEventType() { return "TransferFailedEvent"; }

    @Override
    public Instant getOccurredAt() { return occurredAt; }
}
