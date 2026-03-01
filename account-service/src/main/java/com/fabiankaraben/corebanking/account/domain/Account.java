package com.fabiankaraben.corebanking.account.domain;

import com.fabiankaraben.corebanking.account.domain.exception.InsufficientFundsException;
import java.time.LocalDateTime;
import java.util.UUID;

public class Account {

    private final UUID id;
    private final String customerId;
    private Money balance;
    private Long version;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Account(UUID id, String customerId, Money balance, Long version, LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = id;
        this.customerId = customerId;
        this.balance = balance;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Account create(String customerId) {
        return new Account(UUID.randomUUID(), customerId, Money.zero(), 0L, LocalDateTime.now(), LocalDateTime.now());
    }

    public void deposit(Money amount) {
        this.balance = this.balance.add(amount);
        this.updatedAt = LocalDateTime.now();
    }

    public void withdraw(Money amount) {
        if (this.balance.isLessThan(amount)) {
            throw new InsufficientFundsException(this.id, this.balance, amount);
        }
        this.balance = this.balance.subtract(amount);
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public Money getBalance() {
        return balance;
    }

    public Long getVersion() {
        return version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
