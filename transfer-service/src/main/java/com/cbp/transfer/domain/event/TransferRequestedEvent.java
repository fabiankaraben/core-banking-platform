package com.cbp.transfer.domain.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain event published by the Transfer Service to initiate the Transfer Saga.
 *
 * <p>Published to the {@code transfer.requested} Kafka topic immediately after the
 * {@link com.cbp.transfer.domain.model.Transfer} record is persisted in the local database.
 * The Account Service consumes this event to execute the debit and credit operations.
 *
 * @param eventId         globally unique identifier for this event occurrence
 * @param transferId      the saga correlation identifier (maps to {@code Transfer.id})
 * @param sourceAccountId UUID of the account to debit
 * @param targetAccountId UUID of the account to credit
 * @param amount          the transfer amount
 * @param currency        ISO-4217 currency code
 * @param occurredAt      UTC timestamp of event creation
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
public record TransferRequestedEvent(
        UUID eventId,
        UUID transferId,
        UUID sourceAccountId,
        UUID targetAccountId,
        BigDecimal amount,
        String currency,
        Instant occurredAt
) implements DomainEvent {

    /**
     * Factory method to create a new {@code TransferRequestedEvent} with
     * auto-generated event ID and current timestamp.
     *
     * @param transferId      the saga correlation identifier
     * @param sourceAccountId the account to debit
     * @param targetAccountId the account to credit
     * @param amount          the transfer amount
     * @param currency        the ISO-4217 currency code
     * @return a new {@code TransferRequestedEvent}
     */
    public static TransferRequestedEvent of(UUID transferId, UUID sourceAccountId,
                                            UUID targetAccountId, BigDecimal amount,
                                            String currency) {
        return new TransferRequestedEvent(
                UUID.randomUUID(), transferId, sourceAccountId, targetAccountId,
                amount, currency, Instant.now()
        );
    }

    @Override
    public UUID getEventId() { return eventId; }

    @Override
    public String getEventType() { return "TransferRequestedEvent"; }

    @Override
    public Instant getOccurredAt() { return occurredAt; }
}
