package com.cbp.account.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for the {@link Account} domain entity.
 *
 * <p>Verifies all business rules enforced within the domain model:
 * financial precision, balance constraints, status transitions, and
 * exception conditions. These tests run without any Spring context,
 * making them fast and infrastructure-independent.
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
@DisplayName("Account Domain Entity")
class AccountTest {

    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account("CBP-0001-0001-0001", "Jane Doe", "USD");
        account.activate();
    }

    @Nested
    @DisplayName("Account Creation")
    class AccountCreationTest {

        @Test
        @DisplayName("should create account with PENDING_ACTIVATION status and zero balance")
        void shouldCreateWithPendingStatusAndZeroBalance() {
            Account newAccount = new Account("CBP-0002-0002-0002", "John Smith", "EUR");
            assertThat(newAccount.getStatus()).isEqualTo(AccountStatus.PENDING_ACTIVATION);
            assertThat(newAccount.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(newAccount.getCurrency()).isEqualTo("EUR");
            assertThat(newAccount.getOwnerName()).isEqualTo("John Smith");
            assertThat(newAccount.getId()).isNotNull();
        }

        @Test
        @DisplayName("should transition to ACTIVE status after activation")
        void shouldActivateSuccessfully() {
            Account newAccount = new Account("CBP-0003-0003-0003", "Alice", "USD");
            newAccount.activate();
            assertThat(newAccount.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        }

        @Test
        @DisplayName("should throw when activating already ACTIVE account")
        void shouldThrowWhenActivatingActiveAccount() {
            assertThatThrownBy(() -> account.activate())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("ACTIVE");
        }
    }

    @Nested
    @DisplayName("Credit Operation")
    class CreditTest {

        @Test
        @DisplayName("should increase balance by exact credited amount")
        void shouldIncreaseBalanceOnCredit() {
            account.credit(new BigDecimal("100.00"));
            assertThat(account.getBalance()).isEqualByComparingTo("100.00");
        }

        @Test
        @DisplayName("should apply Banker's Rounding (HALF_EVEN) on credit")
        void shouldApplyBankersRoundingOnCredit() {
            account.credit(new BigDecimal("0.005"));
            assertThat(account.getBalance()).isEqualByComparingTo("0.00");

            account.credit(new BigDecimal("0.015"));
            assertThat(account.getBalance()).isEqualByComparingTo("0.02");
        }

        @Test
        @DisplayName("should accumulate multiple credits correctly")
        void shouldAccumulateMultipleCredits() {
            account.credit(new BigDecimal("50.00"));
            account.credit(new BigDecimal("25.50"));
            account.credit(new BigDecimal("24.50"));
            assertThat(account.getBalance()).isEqualByComparingTo("100.00");
        }

        @Test
        @DisplayName("should throw on zero credit amount")
        void shouldThrowOnZeroCreditAmount() {
            assertThatThrownBy(() -> account.credit(BigDecimal.ZERO))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw on negative credit amount")
        void shouldThrowOnNegativeCreditAmount() {
            assertThatThrownBy(() -> account.credit(new BigDecimal("-1.00")))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw when crediting a FROZEN account")
        void shouldThrowWhenCreditingFrozenAccount() {
            account.credit(new BigDecimal("100.00"));
            account.freeze();
            assertThatThrownBy(() -> account.credit(new BigDecimal("50.00")))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Debit Operation")
    class DebitTest {

        @BeforeEach
        void depositInitialFunds() {
            account.credit(new BigDecimal("200.00"));
        }

        @Test
        @DisplayName("should decrease balance by exact debited amount")
        void shouldDecreaseBalanceOnDebit() {
            account.debit(new BigDecimal("75.00"));
            assertThat(account.getBalance()).isEqualByComparingTo("125.00");
        }

        @Test
        @DisplayName("should allow debit of exact balance amount")
        void shouldAllowDebitOfExactBalance() {
            account.debit(new BigDecimal("200.00"));
            assertThat(account.getBalance()).isEqualByComparingTo("0.00");
        }

        @Test
        @DisplayName("should throw InsufficientFundsException when debit exceeds balance")
        void shouldThrowOnInsufficientFunds() {
            assertThatThrownBy(() -> account.debit(new BigDecimal("200.01")))
                    .isInstanceOf(InsufficientFundsException.class)
                    .hasMessageContaining("Insufficient funds");
        }

        @Test
        @DisplayName("should throw on zero debit amount")
        void shouldThrowOnZeroDebitAmount() {
            assertThatThrownBy(() -> account.debit(BigDecimal.ZERO))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw when debiting a FROZEN account")
        void shouldThrowWhenDebitingFrozenAccount() {
            account.freeze();
            assertThatThrownBy(() -> account.debit(new BigDecimal("50.00")))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("should throw when debiting a CLOSED account")
        void shouldThrowWhenDebitingClosedAccount() {
            account.close();
            assertThatThrownBy(() -> account.debit(new BigDecimal("50.00")))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Status Transitions")
    class StatusTransitionTest {

        @Test
        @DisplayName("should freeze an ACTIVE account")
        void shouldFreezeActiveAccount() {
            account.freeze();
            assertThat(account.getStatus()).isEqualTo(AccountStatus.FROZEN);
        }

        @Test
        @DisplayName("should close an ACTIVE account")
        void shouldCloseActiveAccount() {
            account.close();
            assertThat(account.getStatus()).isEqualTo(AccountStatus.CLOSED);
        }

        @Test
        @DisplayName("should throw when closing an already CLOSED account")
        void shouldThrowWhenClosingAlreadyClosedAccount() {
            account.close();
            assertThatThrownBy(account::close)
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("should throw when freezing a FROZEN account")
        void shouldThrowWhenFreezingFrozenAccount() {
            account.freeze();
            assertThatThrownBy(account::freeze)
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
