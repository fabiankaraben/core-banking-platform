package com.cbp.account.domain.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain event emitted by the Account Service when a money transfer has been
 * successfully processed — both the source account debit and target account credit
 * have been applied atomically.
 *
 * <p>This event is the success outcome of the choreography-based Transfer Saga:
 * <ol>
 *   <li>The Transfer Service publishes {@code TransferRequestedEvent}.</li>
 *   <li>The Account Service debits the source and credits the target.</li>
 *   <li>The Account Service publishes <em>this</em> event.</li>
 *   <li>The Transfer Service consumes it and marks the transfer as {@code COMPLETED}.</li>
 *   <li>The Notification Service consumes it and sends a confirmation to the customer.</li>
 * </ol>
 *
 * @param eventId         globally unique identifier for this event occurrence
 * @param transferId      the saga correlation ID linking all events in this transfer
 * @param sourceAccountId UUID of the debited (sender) account
 * @param targetAccountId UUID of the credited (recipient) account
 * @param amount          the transferred amount (always positive, Banker's Rounding applied)
 * @param currency        ISO-4217 currency code
 * @param occurredAt      UTC timestamp of the event creation
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
public record TransferCompletedEvent(
        UUID eventId,
        UUID transferId,
        UUID sourceAccountId,
        UUID targetAccountId,
        BigDecimal amount,
        String currency,
        Instant occurredAt
) implements DomainEvent {

    /**
     * Factory method to create a new {@code TransferCompletedEvent} with
     * auto-generated event ID and current timestamp.
     *
     * @param transferId      the saga correlation identifier
     * @param sourceAccountId the debited account UUID
     * @param targetAccountId the credited account UUID
     * @param amount          the transferred amount
     * @param currency        the ISO-4217 currency code
     * @return a new {@code TransferCompletedEvent}
     */
    public static TransferCompletedEvent of(UUID transferId, UUID sourceAccountId,
                                            UUID targetAccountId, BigDecimal amount,
                                            String currency) {
        return new TransferCompletedEvent(
                UUID.randomUUID(), transferId, sourceAccountId, targetAccountId,
                amount, currency, Instant.now()
        );
    }

    @Override
    public UUID getEventId() { return eventId; }

    @Override
    public String getEventType() { return "TransferCompletedEvent"; }

    @Override
    public Instant getOccurredAt() { return occurredAt; }
}
