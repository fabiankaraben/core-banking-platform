package com.cbp.account.adapter.in.messaging;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Inbound event record representing a transfer request published by the Transfer Service.
 *
 * <p>Consumed from the {@code transfer.requested} Kafka topic by
 * {@link TransferRequestedEventListener}. This record is the trigger for the
 * Account Service's participation in the choreography-based Transfer Saga.
 *
 * @param eventId         globally unique identifier of the source event
 * @param transferId      the saga correlation identifier (maps to {@code Transfer.id})
 * @param sourceAccountId UUID of the account to be debited
 * @param targetAccountId UUID of the account to be credited
 * @param amount          the amount to transfer
 * @param currency        the ISO-4217 currency code
 * @param occurredAt      UTC timestamp when the event was originally produced
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
) {
}
