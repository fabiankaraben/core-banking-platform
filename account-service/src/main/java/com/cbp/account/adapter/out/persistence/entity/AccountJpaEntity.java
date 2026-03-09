package com.cbp.account.adapter.out.persistence.entity;

import com.cbp.account.domain.model.AccountStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA persistence entity representing a bank account record in the {@code account_db} database.
 *
 * <p>This class is intentionally separated from the domain {@link com.cbp.account.domain.model.Account}
 * entity to prevent JPA annotations from polluting the domain model, following Hexagonal Architecture.
 * Mapping between the two representations is the responsibility of
 * {@link AccountPersistenceAdapter}.
 *
 * <p>The {@code version} field enables optimistic locking via {@link Version}, preventing
 * lost updates when multiple concurrent transfer operations target the same account.
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
@Entity
@Table(name = "accounts", indexes = {
        @Index(name = "idx_accounts_account_number", columnList = "account_number", unique = true)
})
public class AccountJpaEntity {

    /** Surrogate primary key stored as UUID. */
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /** Unique, human-readable account identifier (e.g., {@code CBP-XXXX-YYYY-ZZZZ}). */
    @Column(name = "account_number", unique = true, nullable = false, length = 24)
    private String accountNumber;

    /** Full name of the account owner. */
    @Column(name = "owner_name", nullable = false, length = 100)
    private String ownerName;

    /**
     * Current available balance, stored with full monetary precision.
     * Precision 19 digits with scale 2 supports amounts up to ~9.99 quadrillion.
     */
    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    /** ISO-4217 currency code (e.g., {@code USD}). */
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    /** Lifecycle state of the account. */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AccountStatus status;

    /** Optimistic locking version — incremented on every update. */
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    /** Timestamp of the initial record insertion (UTC). */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Timestamp of the most recent update (UTC). */
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Required no-args constructor for JPA.
     */
    protected AccountJpaEntity() {}

    /**
     * Full constructor used by the persistence adapter to create or reconstitute entities.
     *
     * @param id            surrogate primary key
     * @param accountNumber unique account number
     * @param ownerName     account owner's full name
     * @param balance       current balance
     * @param currency      ISO-4217 currency code
     * @param status        current lifecycle status
     * @param version       optimistic locking version
     * @param createdAt     creation timestamp
     * @param updatedAt     last-modified timestamp
     */
    public AccountJpaEntity(UUID id, String accountNumber, String ownerName,
                             BigDecimal balance, String currency, AccountStatus status,
                             Long version, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.ownerName = ownerName;
        this.balance = balance;
        this.currency = currency;
        this.status = status;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters

    /** @return the surrogate primary key */
    public UUID getId() { return id; }

    /** @return the unique account number */
    public String getAccountNumber() { return accountNumber; }

    /** @return the owner's full name */
    public String getOwnerName() { return ownerName; }

    /** @return the current balance */
    public BigDecimal getBalance() { return balance; }

    /** @return the ISO-4217 currency code */
    public String getCurrency() { return currency; }

    /** @return the current lifecycle status */
    public AccountStatus getStatus() { return status; }

    /** @return the optimistic locking version */
    public Long getVersion() { return version; }

    /** @return the creation timestamp */
    public Instant getCreatedAt() { return createdAt; }

    /** @return the last-modification timestamp */
    public Instant getUpdatedAt() { return updatedAt; }
}
