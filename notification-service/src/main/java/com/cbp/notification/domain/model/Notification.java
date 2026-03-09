package com.cbp.notification.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain entity representing an immutable audit record of a customer notification.
 *
 * <p>A {@code Notification} is created whenever the Notification Service processes a
 * transfer outcome event from Kafka. It captures the full context of what was
 * communicated to the customer and whether dispatch succeeded.
 *
 * <p>Notifications are append-only — once persisted they are never modified.
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
public class Notification {

    /** Unique notification record identifier. */
    private final UUID id;

    /** The saga correlation ID of the transfer that triggered this notification. */
    private final UUID transferId;

    /** UUID of the account owner receiving the notification. */
    private final UUID accountId;

    /** The type of event that triggered this notification. */
    private final NotificationType type;

    /** The transfer amount, included in the notification message. */
    private final BigDecimal amount;

    /** ISO-4217 currency code. */
    private final String currency;

    /** Delivery status of this notification attempt. */
    private final NotificationStatus status;

    /** The rendered notification message content. */
    private final String message;

    /** UTC timestamp of the notification creation. */
    private final Instant createdAt;

    /**
     * Constructs a new {@code Notification} audit record.
     *
     * @param transferId the saga correlation ID
     * @param accountId  the account owner to notify
     * @param type       the notification event type
     * @param amount     the transfer amount
     * @param currency   ISO-4217 currency code
     * @param status     the dispatch status
     * @param message    the rendered message content
     */
    public Notification(UUID transferId, UUID accountId, NotificationType type,
                        BigDecimal amount, String currency,
                        NotificationStatus status, String message) {
        this.id = UUID.randomUUID();
        this.transferId = transferId;
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.message = message;
        this.createdAt = Instant.now();
    }

    /**
     * Full constructor used by the persistence adapter to reconstitute a
     * {@code Notification} from a stored representation.
     *
     * @param id         the notification UUID
     * @param transferId the saga correlation ID
     * @param accountId  the account owner UUID
     * @param type       the notification type
     * @param amount     the transfer amount
     * @param currency   ISO-4217 currency code
     * @param status     the dispatch status
     * @param message    the rendered message content
     * @param createdAt  the creation timestamp
     */
    public Notification(UUID id, UUID transferId, UUID accountId, NotificationType type,
                        BigDecimal amount, String currency, NotificationStatus status,
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

    /** @return the notification unique identifier */
    public UUID getId() { return id; }

    /** @return the transfer saga correlation ID */
    public UUID getTransferId() { return transferId; }

    /** @return the account owner UUID */
    public UUID getAccountId() { return accountId; }

    /** @return the notification event type */
    public NotificationType getType() { return type; }

    /** @return the transfer amount */
    public BigDecimal getAmount() { return amount; }

    /** @return the ISO-4217 currency code */
    public String getCurrency() { return currency; }

    /** @return the dispatch status */
    public NotificationStatus getStatus() { return status; }

    /** @return the rendered notification message */
    public String getMessage() { return message; }

    /** @return the notification creation timestamp */
    public Instant getCreatedAt() { return createdAt; }
}
