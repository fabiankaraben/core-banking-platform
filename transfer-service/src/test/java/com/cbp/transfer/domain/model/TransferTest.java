package com.cbp.transfer.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for the {@link Transfer} domain entity.
 *
 * <p>Verifies all business invariants enforced within the domain model,
 * including Saga state transitions and financial precision. These tests
 * run without any Spring context or infrastructure dependencies.
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
@DisplayName("Transfer Domain Entity")
class TransferTest {

    private Transfer transfer;

    @BeforeEach
    void setUp() {
        transfer = new Transfer(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("250.00"),
                "USD",
                "Test transfer",
                UUID.randomUUID().toString()
        );
    }

    @Nested
    @DisplayName("Transfer Creation")
    class CreationTest {

        @Test
        @DisplayName("should create transfer in PENDING status")
        void shouldCreateInPendingStatus() {
            assertThat(transfer.getStatus()).isEqualTo(TransferStatus.PENDING);
        }

        @Test
        @DisplayName("should apply Banker's Rounding (HALF_EVEN) to amount on creation")
        void shouldApplyBankersRoundingToAmount() {
            Transfer t = new Transfer(
                    UUID.randomUUID(), UUID.randomUUID(),
                    new BigDecimal("100.005"), "USD", null, "key-1"
            );
            assertThat(t.getAmount()).isEqualByComparingTo("100.00");
        }

        @Test
        @DisplayName("should auto-generate a non-null UUID id")
        void shouldAutoGenerateId() {
            assertThat(transfer.getId()).isNotNull();
        }

        @Test
        @DisplayName("should have null failureReason initially")
        void shouldHaveNullFailureReasonInitially() {
            assertThat(transfer.getFailureReason()).isNull();
        }
    }

    @Nested
    @DisplayName("complete()")
    class CompleteTest {

        @Test
        @DisplayName("should transition PENDING → COMPLETED")
        void shouldTransitionToCompleted() {
            transfer.complete();
            assertThat(transfer.getStatus()).isEqualTo(TransferStatus.COMPLETED);
        }

        @Test
        @DisplayName("should throw when completing a COMPLETED transfer")
        void shouldThrowWhenAlreadyCompleted() {
            transfer.complete();
            assertThatThrownBy(transfer::complete)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("COMPLETED");
        }

        @Test
        @DisplayName("should throw when completing a FAILED transfer")
        void shouldThrowWhenAlreadyFailed() {
            transfer.fail("Insufficient funds");
            assertThatThrownBy(transfer::complete)
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("fail()")
    class FailTest {

        @Test
        @DisplayName("should transition PENDING → FAILED and record reason")
        void shouldTransitionToFailedWithReason() {
            transfer.fail("Insufficient funds in source account");
            assertThat(transfer.getStatus()).isEqualTo(TransferStatus.FAILED);
            assertThat(transfer.getFailureReason()).isEqualTo("Insufficient funds in source account");
        }

        @Test
        @DisplayName("should throw when failing a COMPLETED transfer")
        void shouldThrowWhenAlreadyCompleted() {
            transfer.complete();
            assertThatThrownBy(() -> transfer.fail("reason"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("COMPLETED");
        }
    }
}
