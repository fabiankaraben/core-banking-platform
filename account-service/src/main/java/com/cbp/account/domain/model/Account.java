package com.cbp.account.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

/**
 * Core domain entity representing a bank account.
 *
 * <p>This class is a pure domain object — it contains no JPA annotations, no
 * framework dependencies, and no infrastructure concerns. All business invariants
 * are enforced here, following the <em>Rich Domain Model</em> principle.
 *
 * <h2>Financial Precision</h2>
 * <p>All monetary arithmetic uses {@link BigDecimal} with {@link RoundingMode#HALF_EVEN}
 * (Banker's Rounding) and a scale of 2 decimal places to guarantee deterministic
 * financial computations free from floating-point rounding errors.
 *
 * <h2>Concurrency Safety</h2>
 * <p>Concurrent modifications (e.g., two simultaneous transfers debiting the same
 * account) are guarded by an optimistic locking {@code version} field, which maps
 * to a {@code @Version} column in the JPA persistence adapter.
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
public class Account {

    private static final int MONETARY_SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;

    /** Unique identifier for this account (surrogate key). */
    private final UUID id;

    /** Unique account number visible to the customer (e.g., IBAN-style). */
    private final String accountNumber;

    /** Full name of the account owner. */
    private final String ownerName;

    /** Current available balance, always non-negative. */
    private BigDecimal balance;

    /** Currency code (ISO-4217), e.g., {@code USD}, {@code EUR}. */
    private final String currency;

    /** Lifecycle state of the account. */
    private AccountStatus status;

    /** Optimistic locking version counter. */
    private Long version;

    /** Timestamp of account creation (UTC). */
    private final Instant createdAt;

    /** Timestamp of the last modification (UTC). */
    private Instant updatedAt;

    /**
     * Constructs a new {@code Account} with an initial zero balance and
     * {@link AccountStatus#PENDING_ACTIVATION} status.
     *
     * <p>Use this constructor when creating a brand-new account from a customer request.
     *
     * @param accountNumber unique account number assigned to this account
     * @param ownerName     full name of the account owner
     * @param currency      ISO-4217 currency code (e.g., {@code "USD"})
     */
    public Account(String accountNumber, String ownerName, String currency) {
        this.id = UUID.randomUUID();
        this.accountNumber = accountNumber;
        this.ownerName = ownerName;
        this.balance = BigDecimal.ZERO.setScale(MONETARY_SCALE, ROUNDING_MODE);
        this.currency = currency;
        this.status = AccountStatus.PENDING_ACTIVATION;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    /**
     * Full constructor used by the persistence adapter to reconstruct an
     * {@code Account} from a stored representation.
     *
     * @param id            unique account identifier
     * @param accountNumber unique account number
     * @param ownerName     full name of the account owner
     * @param balance       current balance
     * @param currency      ISO-4217 currency code
     * @param status        current lifecycle status
     * @param version       optimistic locking version
     * @param createdAt     creation timestamp
     * @param updatedAt     last-update timestamp
     */
    public Account(UUID id, String accountNumber, String ownerName, BigDecimal balance,
                   String currency, AccountStatus status, Long version,
                   Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.ownerName = ownerName;
        this.balance = balance.setScale(MONETARY_SCALE, ROUNDING_MODE);
        this.currency = currency;
        this.status = status;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Activates the account, transitioning its status from
     * {@link AccountStatus#PENDING_ACTIVATION} to {@link AccountStatus#ACTIVE}.
     *
     * @throws IllegalStateException if the account is not in
     *                               {@link AccountStatus#PENDING_ACTIVATION} state
     */
    public void activate() {
        if (this.status != AccountStatus.PENDING_ACTIVATION) {
            throw new IllegalStateException(
                    "Cannot activate account in status: " + this.status);
        }
        this.status = AccountStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    /**
     * Credits the account with the specified amount.
     *
     * <p>Applies Banker's Rounding ({@link RoundingMode#HALF_EVEN}) to the amount
     * before adding it to the balance.
     *
     * @param amount the positive amount to credit; must be greater than zero
     * @throws IllegalArgumentException if {@code amount} is null, zero, or negative
     * @throws IllegalStateException    if the account is not {@link AccountStatus#ACTIVE}
     */
    public void credit(BigDecimal amount) {
        validateOperationalState();
        validatePositiveAmount(amount);
        this.balance = this.balance
                .add(amount.setScale(MONETARY_SCALE, ROUNDING_MODE))
                .setScale(MONETARY_SCALE, ROUNDING_MODE);
        this.updatedAt = Instant.now();
    }

    /**
     * Debits the account by the specified amount.
     *
     * <p>Applies Banker's Rounding ({@link RoundingMode#HALF_EVEN}) to the amount
     * before subtracting it from the balance.
     *
     * @param amount the positive amount to debit; must be greater than zero
     * @throws IllegalArgumentException  if {@code amount} is null, zero, or negative
     * @throws IllegalStateException     if the account is not {@link AccountStatus#ACTIVE}
     * @throws InsufficientFundsException if the current balance is less than {@code amount}
     */
    public void debit(BigDecimal amount) {
        validateOperationalState();
        validatePositiveAmount(amount);
        BigDecimal scaledAmount = amount.setScale(MONETARY_SCALE, ROUNDING_MODE);
        if (this.balance.compareTo(scaledAmount) < 0) {
            throw new InsufficientFundsException(
                    "Insufficient funds. Available: " + this.balance
                            + " " + this.currency + ", Requested: " + scaledAmount);
        }
        this.balance = this.balance.subtract(scaledAmount).setScale(MONETARY_SCALE, ROUNDING_MODE);
        this.updatedAt = Instant.now();
    }

    /**
     * Freezes the account, preventing any further debit or credit operations.
     *
     * @throws IllegalStateException if the account is not {@link AccountStatus#ACTIVE}
     */
    public void freeze() {
        if (this.status != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Only ACTIVE accounts can be frozen.");
        }
        this.status = AccountStatus.FROZEN;
        this.updatedAt = Instant.now();
    }

    /**
     * Closes the account permanently. This is a terminal state transition.
     *
     * @throws IllegalStateException if the account is already {@link AccountStatus#CLOSED}
     */
    public void close() {
        if (this.status == AccountStatus.CLOSED) {
            throw new IllegalStateException("Account is already closed.");
        }
        this.status = AccountStatus.CLOSED;
        this.updatedAt = Instant.now();
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void validateOperationalState() {
        if (this.status != AccountStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Operation not permitted. Account status is: " + this.status);
        }
    }

    private void validatePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive, got: " + amount);
        }
    }

    // -------------------------------------------------------------------------
    // Getters (no setters — domain state is changed only via behaviour methods)
    // -------------------------------------------------------------------------

    /** @return the unique identifier of this account */
    public UUID getId() { return id; }

    /** @return the unique account number */
    public String getAccountNumber() { return accountNumber; }

    /** @return the full name of the account owner */
    public String getOwnerName() { return ownerName; }

    /** @return the current available balance */
    public BigDecimal getBalance() { return balance; }

    /** @return the ISO-4217 currency code */
    public String getCurrency() { return currency; }

    /** @return the current lifecycle status */
    public AccountStatus getStatus() { return status; }

    /** @return the optimistic locking version counter */
    public Long getVersion() { return version; }

    /** @return the account creation timestamp (UTC) */
    public Instant getCreatedAt() { return createdAt; }

    /** @return the last modification timestamp (UTC) */
    public Instant getUpdatedAt() { return updatedAt; }
}
