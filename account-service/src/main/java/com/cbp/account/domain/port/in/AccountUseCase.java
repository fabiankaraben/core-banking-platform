package com.cbp.account.domain.port.in;

import com.cbp.account.domain.model.Account;

import java.util.List;
import java.util.UUID;

/**
 * Primary inbound port defining all use cases exposed by the Account Service.
 *
 * <p>This interface is the boundary between the outside world (HTTP adapters,
 * Kafka listeners) and the core domain logic. Any caller wishing to interact
 * with accounts must do so exclusively through this contract, enforcing the
 * dependency-inversion principle central to Hexagonal Architecture.
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
public interface AccountUseCase {

    /**
     * Creates a new bank account and transitions it immediately to
     * {@link com.cbp.account.domain.model.AccountStatus#ACTIVE}.
     *
     * <p>The account number is auto-generated and guaranteed to be globally unique.
     *
     * @param ownerName full name of the account owner; must not be blank
     * @param currency  ISO-4217 currency code (e.g., {@code "USD"}); must not be blank
     * @return the newly created and persisted {@link Account}
     * @throws IllegalArgumentException if {@code ownerName} or {@code currency} is blank
     */
    Account createAccount(String ownerName, String currency);

    /**
     * Retrieves an account by its internal unique identifier.
     *
     * @param accountId the UUID of the account to retrieve
     * @return the {@link Account} with the given identifier
     * @throws com.cbp.account.adapter.in.web.exception.AccountNotFoundException
     *         if no account exists with the given {@code accountId}
     */
    Account getAccountById(UUID accountId);

    /**
     * Retrieves all accounts persisted in the system.
     *
     * <p>In a production environment this endpoint would be paginated; for this
     * portfolio demonstration it returns all records.
     *
     * @return an unmodifiable list of all accounts, possibly empty
     */
    List<Account> getAllAccounts();

    /**
     * Credits (deposits) a specified amount into an account.
     *
     * @param accountId the UUID of the account to credit
     * @param amount    the amount to deposit as a decimal string (e.g., {@code "100.00"})
     * @return the updated {@link Account} after the credit has been applied
     * @throws com.cbp.account.adapter.in.web.exception.AccountNotFoundException
     *         if no account exists with the given {@code accountId}
     * @throws com.cbp.account.domain.model.InsufficientFundsException
     *         if the requested amount is invalid
     * @throws IllegalStateException if the account is not
     *         {@link com.cbp.account.domain.model.AccountStatus#ACTIVE}
     */
    Account deposit(UUID accountId, java.math.BigDecimal amount);

    /**
     * Processes the debit side of a transfer.
     *
     * <p>Called internally by the Saga Kafka consumer when a
     * {@code TransferRequestedEvent} is received. Attempts to debit the source
     * account and publishes a result event to indicate success or failure.
     *
     * @param transferId      the unique transfer saga identifier
     * @param sourceAccountId the UUID of the account to debit
     * @param targetAccountId the UUID of the account to credit
     * @param amount          the transfer amount
     */
    void processTransferRequest(UUID transferId, UUID sourceAccountId,
                                UUID targetAccountId, java.math.BigDecimal amount);
}
