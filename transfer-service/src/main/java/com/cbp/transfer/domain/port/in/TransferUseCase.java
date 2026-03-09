package com.cbp.transfer.domain.port.in;

import com.cbp.transfer.domain.model.Transfer;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Primary inbound port defining all use cases exposed by the Transfer Service.
 *
 * <p>All callers (HTTP adapters, Kafka listeners) must interact with the Transfer Service
 * exclusively through this interface, enforcing the dependency-inversion principle of
 * Hexagonal Architecture and keeping the domain logic independent of infrastructure.
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
public interface TransferUseCase {

    /**
     * Initiates a new money transfer Saga.
     *
     * <p>This operation:
     * <ol>
     *   <li>Validates the {@code idempotencyKey} against Redis — if a previous response
     *       exists it is returned immediately without side effects.</li>
     *   <li>Persists a {@link Transfer} record in {@code PENDING} status within the
     *       same local database transaction as an outbox event.</li>
     *   <li>Publishes a {@code TransferRequestedEvent} to Kafka, initiating the
     *       choreography-based Saga in the Account Service.</li>
     * </ol>
     *
     * @param sourceAccountId UUID of the account to debit
     * @param targetAccountId UUID of the account to credit
     * @param amount          transfer amount; must be positive
     * @param currency        ISO-4217 currency code
     * @param description     optional human-readable description of the transfer
     * @param idempotencyKey  client-supplied unique key (UUID format) for duplicate detection
     * @return the newly created {@link Transfer} in {@code PENDING} status
     * @throws IllegalArgumentException if any required field is invalid
     */
    Transfer initiateTransfer(UUID sourceAccountId, UUID targetAccountId, BigDecimal amount,
                              String currency, String description, String idempotencyKey);

    /**
     * Retrieves a transfer by its unique identifier.
     *
     * @param transferId the UUID of the transfer to retrieve
     * @return the {@link Transfer} with the given identifier
     * @throws com.cbp.transfer.adapter.in.web.exception.TransferNotFoundException
     *         if no transfer exists with the given {@code transferId}
     */
    Transfer getTransferById(UUID transferId);

    /**
     * Retrieves all transfers in the system.
     *
     * @return an unmodifiable list of all transfers, possibly empty
     */
    List<Transfer> getAllTransfers();

    /**
     * Updates a pending transfer to {@code COMPLETED} status.
     *
     * <p>Called by the Kafka consumer when a {@code TransferCompletedEvent} is received
     * from the Account Service, confirming successful debit/credit execution.
     *
     * @param transferId the saga correlation UUID
     */
    void markTransferCompleted(UUID transferId);

    /**
     * Updates a pending transfer to {@code FAILED} status.
     *
     * <p>Called by the Kafka consumer when a {@code TransferFailedEvent} is received
     * from the Account Service.
     *
     * @param transferId    the saga correlation UUID
     * @param failureReason a human-readable reason for the failure
     */
    void markTransferFailed(UUID transferId, String failureReason);
}
