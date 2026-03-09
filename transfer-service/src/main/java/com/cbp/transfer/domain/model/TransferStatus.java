package com.cbp.transfer.domain.model;

/**
 * Represents the lifecycle state of a money transfer within the Saga.
 *
 * <p>State transitions follow a strict finite-state machine driven by
 * Kafka events from the Account Service:
 * <pre>
 *   PENDING → COMPLETED
 *           ↘ FAILED
 * </pre>
 *
 * <ul>
 *   <li>{@link #PENDING} — Transfer intent has been recorded and the
 *       {@code TransferRequestedEvent} has been published to Kafka. The
 *       Account Service is processing the debit/credit operations.</li>
 *   <li>{@link #COMPLETED} — The Account Service confirmed that both the
 *       source account debit and target account credit were applied successfully.</li>
 *   <li>{@link #FAILED} — The Account Service reported a failure (e.g., insufficient
 *       funds, frozen account). No funds were moved.</li>
 * </ul>
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
public enum TransferStatus {

    /**
     * Transfer has been registered and the Saga has been initiated.
     * Awaiting outcome from the Account Service via Kafka.
     */
    PENDING,

    /**
     * Transfer was executed successfully. Funds have moved from the
     * source account to the target account.
     */
    COMPLETED,

    /**
     * Transfer could not be completed. No funds were debited or credited.
     * The {@link Transfer#getFailureReason()} field contains the reason.
     */
    FAILED
}
