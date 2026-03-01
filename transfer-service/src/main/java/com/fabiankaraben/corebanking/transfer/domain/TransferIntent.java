package com.fabiankaraben.corebanking.transfer.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class TransferIntent {

    private final UUID id;
    private final UUID sourceAccountId;
    private final UUID targetAccountId;
    private final Money amount;
    private TransferStatus status;
    private String failureReason;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TransferIntent(UUID id, UUID sourceAccountId, UUID targetAccountId, Money amount, TransferStatus status,
            String failureReason, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.sourceAccountId = sourceAccountId;
        this.targetAccountId = targetAccountId;
        this.amount = amount;
        this.status = status;
        this.failureReason = failureReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static TransferIntent create(UUID sourceAccountId, UUID targetAccountId, Money amount) {
        return new TransferIntent(UUID.randomUUID(), sourceAccountId, targetAccountId, amount, TransferStatus.PENDING,
                null, LocalDateTime.now(), LocalDateTime.now());
    }

    public void markCompleted() {
        this.status = TransferStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }

    public void markFailed(String reason) {
        this.status = TransferStatus.FAILED;
        this.failureReason = reason;
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getSourceAccountId() {
        return sourceAccountId;
    }

    public UUID getTargetAccountId() {
        return targetAccountId;
    }

    public Money getAmount() {
        return amount;
    }

    public TransferStatus getStatus() {
        return status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
