package com.cbp.transfer.adapter.in.web.dto;

import com.cbp.transfer.domain.model.Transfer;
import com.cbp.transfer.domain.model.TransferStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO representing a transfer returned from the REST API.
 *
 * @param id              the transfer's saga correlation UUID
 * @param sourceAccountId UUID of the debited account
 * @param targetAccountId UUID of the credited account
 * @param amount          the transfer amount
 * @param currency        ISO-4217 currency code
 * @param status          current Saga status
 * @param description     optional transfer description
 * @param failureReason   failure reason if status is {@code FAILED}, otherwise null
 * @param createdAt       transfer creation timestamp (UTC)
 * @param updatedAt       last status update timestamp (UTC)
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
public record TransferResponse(
        UUID id,
        UUID sourceAccountId,
        UUID targetAccountId,
        BigDecimal amount,
        String currency,
        TransferStatus status,
        String description,
        String failureReason,
        Instant createdAt,
        Instant updatedAt
) {

    /**
     * Constructs a {@code TransferResponse} from a {@link Transfer} domain entity.
     *
     * @param transfer the source domain entity; must not be null
     * @return a new {@code TransferResponse} populated from the given transfer
     */
    public static TransferResponse from(Transfer transfer) {
        return new TransferResponse(
                transfer.getId(),
                transfer.getSourceAccountId(),
                transfer.getTargetAccountId(),
                transfer.getAmount(),
                transfer.getCurrency(),
                transfer.getStatus(),
                transfer.getDescription(),
                transfer.getFailureReason(),
                transfer.getCreatedAt(),
                transfer.getUpdatedAt()
        );
    }
}
