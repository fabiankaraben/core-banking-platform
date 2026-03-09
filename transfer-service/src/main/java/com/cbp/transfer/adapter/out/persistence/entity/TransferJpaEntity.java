package com.cbp.transfer.adapter.out.persistence.entity;

import com.cbp.transfer.domain.model.TransferStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA persistence entity representing a transfer record in the {@code transfer_db} database.
 *
 * <p>Intentionally separated from the {@link com.cbp.transfer.domain.model.Transfer} domain
 * entity to maintain Hexagonal Architecture boundaries. Mapping is performed by
 * {@link com.cbp.transfer.adapter.out.persistence.TransferPersistenceAdapter}.
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
@Entity
@Table(name = "transfers", indexes = {
        @Index(name = "idx_transfers_idempotency_key", columnList = "idempotency_key", unique = true),
        @Index(name = "idx_transfers_source_account", columnList = "source_account_id"),
        @Index(name = "idx_transfers_status", columnList = "status")
})
public class TransferJpaEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "source_account_id", nullable = false)
    private UUID sourceAccountId;

    @Column(name = "target_account_id", nullable = false)
    private UUID targetAccountId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransferStatus status;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 255)
    private String idempotencyKey;

    @Column(name = "failure_reason", length = 512)
    private String failureReason;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** Required no-args constructor for JPA. */
    protected TransferJpaEntity() {}

    /**
     * Full constructor used by the persistence adapter.
     *
     * @param id              saga correlation UUID
     * @param sourceAccountId source account UUID
     * @param targetAccountId target account UUID
     * @param amount          transfer amount
     * @param currency        ISO-4217 currency code
     * @param status          current saga status
     * @param description     optional transfer description
     * @param idempotencyKey  client-supplied idempotency key
     * @param failureReason   failure reason if failed
     * @param version         optimistic locking version
     * @param createdAt       creation timestamp
     * @param updatedAt       last-modified timestamp
     */
    public TransferJpaEntity(UUID id, UUID sourceAccountId, UUID targetAccountId,
                              BigDecimal amount, String currency, TransferStatus status,
                              String description, String idempotencyKey, String failureReason,
                              Long version, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.sourceAccountId = sourceAccountId;
        this.targetAccountId = targetAccountId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.description = description;
        this.idempotencyKey = idempotencyKey;
        this.failureReason = failureReason;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public UUID getSourceAccountId() { return sourceAccountId; }
    public UUID getTargetAccountId() { return targetAccountId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public TransferStatus getStatus() { return status; }
    public String getDescription() { return description; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getFailureReason() { return failureReason; }
    public Long getVersion() { return version; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
