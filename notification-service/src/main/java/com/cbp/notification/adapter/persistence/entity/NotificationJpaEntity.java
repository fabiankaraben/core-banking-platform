package com.cbp.notification.adapter.persistence.entity;

import com.cbp.notification.domain.model.NotificationStatus;
import com.cbp.notification.domain.model.NotificationType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA persistence entity for notification audit records in the {@code notification_db}.
 *
 * <p>This table is append-only — records are inserted once and never updated,
 * ensuring an immutable, tamper-evident audit trail of all notification attempts.
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notifications_transfer_id", columnList = "transfer_id"),
        @Index(name = "idx_notifications_account_id", columnList = "account_id"),
        @Index(name = "idx_notifications_type", columnList = "type")
})
public class NotificationJpaEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "transfer_id", nullable = false)
    private UUID transferId;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private NotificationType type;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private NotificationStatus status;

    @Column(name = "message", nullable = false, length = 1024)
    private String message;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Required no-args constructor for JPA. */
    protected NotificationJpaEntity() {}

    /**
     * Full constructor used by the persistence adapter.
     *
     * @param id         notification UUID
     * @param transferId saga correlation ID
     * @param accountId  account owner UUID
     * @param type       notification type
     * @param amount     transfer amount
     * @param currency   ISO-4217 currency
     * @param status     dispatch status
     * @param message    rendered message content
     * @param createdAt  creation timestamp
     */
    public NotificationJpaEntity(UUID id, UUID transferId, UUID accountId,
                                  NotificationType type, BigDecimal amount,
                                  String currency, NotificationStatus status,
                                  String message, Instant createdAt) {
        this.id = id;
        this.transferId = transferId;
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.message = message;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getTransferId() { return transferId; }
    public UUID getAccountId() { return accountId; }
    public NotificationType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public NotificationStatus getStatus() { return status; }
    public String getMessage() { return message; }
    public Instant getCreatedAt() { return createdAt; }
}
