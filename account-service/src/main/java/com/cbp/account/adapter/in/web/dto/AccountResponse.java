package com.cbp.account.adapter.in.web.dto;

import com.cbp.account.domain.model.Account;
import com.cbp.account.domain.model.AccountStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO representing a bank account returned from the REST API.
 *
 * <p>Maps directly from the {@link Account} domain entity. Using a dedicated
 * response record avoids leaking persistence or domain internals to API consumers.
 *
 * @param id            the account's unique identifier
 * @param accountNumber the human-readable account number
 * @param ownerName     the full name of the account owner
 * @param balance       the current available balance
 * @param currency      the ISO-4217 currency code
 * @param status        the current lifecycle status
 * @param createdAt     the account creation timestamp (UTC)
 * @param updatedAt     the last modification timestamp (UTC)
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
public record AccountResponse(
        UUID id,
        String accountNumber,
        String ownerName,
        BigDecimal balance,
        String currency,
        AccountStatus status,
        Instant createdAt,
        Instant updatedAt
) {

    /**
     * Constructs an {@code AccountResponse} from an {@link Account} domain entity.
     *
     * @param account the source domain entity; must not be null
     * @return a new {@code AccountResponse} populated from the given account
     */
    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getOwnerName(),
                account.getBalance(),
                account.getCurrency(),
                account.getStatus(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}
