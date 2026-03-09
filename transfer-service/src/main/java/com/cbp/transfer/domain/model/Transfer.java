package com.cbp.transfer.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

/**
 * Core domain entity representing a money transfer intent within the Saga.
 *
 * <p>A {@code Transfer} is the authoritative record of a customer's request to
 * move funds from one account to another. It does not perform balance changes
 * directly — those are delegated to the Account Service via Kafka event choreography.
 *
 * <h2>Financial Precision</h2>
 * <p>All monetary values use {@link BigDecimal} with {@link RoundingMode#HALF_EVEN}
 * (Banker's Rounding) at 2 decimal places.
 *
 * <h2>Idempotency</h2>
 * <p>Each transfer is associated with a client-supplied {@code idempotencyKey}
 * stored in Redis for 24 hours. Duplicate requests with the same key return the
 * cached response without creating a second transfer or re-initiating the Saga.
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
public class Transfer {

    private static final int MONETARY_SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;

    /** Unique saga correlation identifier. */
    private final UUID id;

    /** UUID of the account to be debited. */
    private final UUID sourceAccountId;

    /** UUID of the account to be credited. */
    private final UUID targetAccountId;

    /** Amount to transfer; always positive and scaled to 2 decimal places. */
    private final BigDecimal amount;

    /** ISO-4217 currency code. */
    private final String currency;

    /** Current Saga state. */
    private TransferStatus status;

    /** Human-readable description of the transfer purpose. */
    private final String description;

    /** Client-supplied idempotency key (UUID format recommended). */
    private final String idempotencyKey;

    /** Reason for failure, populated only when status is {@link TransferStatus#FAILED}. */
    private String failureReason;

    /** Optimistic locking version. */
    private Long version;

    /** Timestamp of transfer creation (UTC). */
    private final Instant createdAt;

    /** Timestamp of last status update (UTC). */
    private Instant updatedAt;

    /**
     * Creates a new {@code Transfer} in {@link TransferStatus#PENDING} state.
     *
     * @param sourceAccountId UUID of the source (debit) account
     * @param targetAccountId UUID of the target (credit) account
     * @param amount          the amount to transfer; must be positive
     * @param currency        ISO-4217 currency code
     * @param description     optional human-readable transfer description
     * @param idempotencyKey  client-supplied unique key for duplicate detection
     */
    public Transfer(UUID sourceAccountId, UUID targetAccountId, BigDecimal amount,
                    String currency, String description, String idempotencyKey) {
        this.id = UUID.randomUUID();
        this.sourceAccountId = sourceAccountId;
        this.targetAccountId = targetAccountId;
        this.amount = amount.setScale(MONETARY_SCALE, ROUNDING_MODE);
        this.currency = currency;
        this.status = TransferStatus.PENDING;
        this.description = description;
        this.idempotencyKey = idempotencyKey;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    /**
     * Full constructor used by the persistence adapter to reconstitute a
     * {@code Transfer} from a stored representation.
     *
     * @param id              the saga correlation UUID
     * @param sourceAccountId UUID of the source account
     * @param targetAccountId UUID of the target account
     * @param amount          the transfer amount
     * @param currency        ISO-4217 currency code
     * @param status          current saga status
     * @param description     transfer description
     * @param idempotencyKey  the client-supplied idempotency key
     * @param failureReason   reason for failure if status is {@link TransferStatus#FAILED}
     * @param version         optimistic locking version
     * @param createdAt       creation timestamp
     * @param updatedAt       last-updated timestamp
     */
    public Transfer(UUID id, UUID sourceAccountId, UUID targetAccountId, BigDecimal amount,
                    String currency, TransferStatus status, String description,
                    String idempotencyKey, String failureReason, Long version,
                    Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.sourceAccountId = sourceAccountId;
        this.targetAccountId = targetAccountId;
        this.amount = amount.setScale(MONETARY_SCALE, ROUNDING_MODE);
        this.currency = currency;
        this.status = status;
        this.description = description;
        this.idempotencyKey = idempotencyKey;
        this.failureReason = failureReason;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Marks this transfer as {@link TransferStatus#COMPLETED}.
     *
     * <p>Called when a {@code TransferCompletedEvent} is received from the Account Service,
     * confirming that both debit and credit operations were applied successfully.
     *
     * @throws IllegalStateException if the transfer is not in {@link TransferStatus#PENDING} state
     */
    public void complete() {
        if (this.status != TransferStatus.PENDING) {
            throw new IllegalStateException(
                    "Cannot complete transfer in status: " + this.status);
        }
        this.status = TransferStatus.COMPLETED;
        this.updatedAt = Instant.now();
    }

    /**
     * Marks this transfer as {@link TransferStatus#FAILED} with a descriptive reason.
     *
     * <p>Called when a {@code TransferFailedEvent} is received from the Account Service.
     *
     * @param reason a human-readable description of why the transfer failed
     * @throws IllegalStateException if the transfer is not in {@link TransferStatus#PENDING} state
     */
    public void fail(String reason) {
        if (this.status != TransferStatus.PENDING) {
            throw new IllegalStateException(
                    "Cannot fail transfer in status: " + this.status);
        }
        this.status = TransferStatus.FAILED;
        this.failureReason = reason;
        this.updatedAt = Instant.now();
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /** @return the saga correlation UUID */
    public UUID getId() { return id; }

    /** @return the source account UUID */
    public UUID getSourceAccountId() { return sourceAccountId; }

    /** @return the target account UUID */
    public UUID getTargetAccountId() { return targetAccountId; }

    /** @return the transfer amount */
    public BigDecimal getAmount() { return amount; }

    /** @return the ISO-4217 currency code */
    public String getCurrency() { return currency; }

    /** @return the current saga status */
    public TransferStatus getStatus() { return status; }

    /** @return the optional transfer description */
    public String getDescription() { return description; }

    /** @return the client-supplied idempotency key */
    public String getIdempotencyKey() { return idempotencyKey; }

    /** @return the failure reason, or {@code null} if not failed */
    public String getFailureReason() { return failureReason; }

    /** @return the optimistic locking version */
    public Long getVersion() { return version; }

    /** @return the creation timestamp */
    public Instant getCreatedAt() { return createdAt; }

    /** @return the last-updated timestamp */
    public Instant getUpdatedAt() { return updatedAt; }
}
