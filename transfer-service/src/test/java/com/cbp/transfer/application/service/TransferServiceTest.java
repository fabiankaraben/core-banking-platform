package com.cbp.transfer.application.service;

import com.cbp.transfer.adapter.in.web.exception.TransferNotFoundException;
import com.cbp.transfer.domain.event.TransferRequestedEvent;
import com.cbp.transfer.domain.model.Transfer;
import com.cbp.transfer.domain.model.TransferStatus;
import com.cbp.transfer.domain.port.out.EventPublisherPort;
import com.cbp.transfer.domain.port.out.IdempotencyPort;
import com.cbp.transfer.domain.port.out.TransferRepositoryPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link TransferService} application service.
 *
 * <p>All outbound ports are mocked with Mockito, allowing the application
 * layer to be tested in isolation.
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TransferService Application Service")
class TransferServiceTest {

    @Mock private TransferRepositoryPort transferRepository;
    @Mock private EventPublisherPort eventPublisher;
    @Mock private IdempotencyPort idempotency;

    private TransferService transferService;

    @BeforeEach
    void setUp() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        transferService = new TransferService(
                transferRepository, eventPublisher, idempotency, mapper
        );
    }

    private Transfer pendingTransfer(UUID id) {
        return new Transfer(
                id, UUID.randomUUID(), UUID.randomUUID(),
                new BigDecimal("100.00"), "USD", TransferStatus.PENDING,
                "test", UUID.randomUUID().toString(), null, 0L,
                Instant.now(), Instant.now()
        );
    }

    @Nested
    @DisplayName("initiateTransfer")
    class InitiateTransferTests {

        @Test
        @DisplayName("should save transfer and publish TransferRequestedEvent")
        void shouldSaveAndPublishEvent() {
            UUID sourceId = UUID.randomUUID();
            UUID targetId = UUID.randomUUID();
            String idemKey = UUID.randomUUID().toString();

            when(transferRepository.findByIdempotencyKey(idemKey)).thenReturn(Optional.empty());
            when(transferRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Transfer result = transferService.initiateTransfer(
                    sourceId, targetId, new BigDecimal("100.00"), "USD", "desc", idemKey);

            assertThat(result.getStatus()).isEqualTo(TransferStatus.PENDING);
            assertThat(result.getSourceAccountId()).isEqualTo(sourceId);
            assertThat(result.getTargetAccountId()).isEqualTo(targetId);
            assertThat(result.getAmount()).isEqualByComparingTo("100.00");

            verify(eventPublisher).publish(argThat(e -> e instanceof TransferRequestedEvent));
            verify(transferRepository).save(any());
        }

        @Test
        @DisplayName("should return existing transfer on duplicate idempotency key")
        void shouldReturnExistingOnDuplicateKey() {
            String idemKey = UUID.randomUUID().toString();
            Transfer existing = pendingTransfer(UUID.randomUUID());

            when(transferRepository.findByIdempotencyKey(idemKey))
                    .thenReturn(Optional.of(existing));

            Transfer result = transferService.initiateTransfer(
                    existing.getSourceAccountId(), existing.getTargetAccountId(),
                    new BigDecimal("100.00"), "USD", "desc", idemKey);

            assertThat(result.getId()).isEqualTo(existing.getId());
            verify(transferRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }

        @Test
        @DisplayName("should throw on zero amount")
        void shouldThrowOnZeroAmount() {
            when(transferRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
            assertThatThrownBy(() -> transferService.initiateTransfer(
                    UUID.randomUUID(), UUID.randomUUID(),
                    BigDecimal.ZERO, "USD", null, "key"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw when source and target accounts are the same")
        void shouldThrowOnSameAccounts() {
            UUID sameId = UUID.randomUUID();
            when(transferRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
            assertThatThrownBy(() -> transferService.initiateTransfer(
                    sameId, sameId, new BigDecimal("100.00"), "USD", null, "key"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("different");
        }
    }

    @Nested
    @DisplayName("markTransferCompleted")
    class MarkCompletedTests {

        @Test
        @DisplayName("should update transfer to COMPLETED status")
        void shouldMarkCompleted() {
            UUID id = UUID.randomUUID();
            Transfer transfer = pendingTransfer(id);
            when(transferRepository.findById(id)).thenReturn(Optional.of(transfer));
            when(transferRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            transferService.markTransferCompleted(id);

            assertThat(transfer.getStatus()).isEqualTo(TransferStatus.COMPLETED);
            verify(transferRepository).save(transfer);
        }

        @Test
        @DisplayName("should silently ignore unknown transfer ID")
        void shouldIgnoreUnknownId() {
            UUID id = UUID.randomUUID();
            when(transferRepository.findById(id)).thenReturn(Optional.empty());
            assertThatCode(() -> transferService.markTransferCompleted(id)).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("markTransferFailed")
    class MarkFailedTests {

        @Test
        @DisplayName("should update transfer to FAILED status with reason")
        void shouldMarkFailed() {
            UUID id = UUID.randomUUID();
            Transfer transfer = pendingTransfer(id);
            when(transferRepository.findById(id)).thenReturn(Optional.of(transfer));
            when(transferRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            transferService.markTransferFailed(id, "Insufficient funds");

            assertThat(transfer.getStatus()).isEqualTo(TransferStatus.FAILED);
            assertThat(transfer.getFailureReason()).isEqualTo("Insufficient funds");
        }
    }

    @Nested
    @DisplayName("getTransferById")
    class GetByIdTests {

        @Test
        @DisplayName("should throw TransferNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            UUID id = UUID.randomUUID();
            when(transferRepository.findById(id)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> transferService.getTransferById(id))
                    .isInstanceOf(TransferNotFoundException.class);
        }

        @Test
        @DisplayName("should return transfer when found")
        void shouldReturnWhenFound() {
            UUID id = UUID.randomUUID();
            Transfer t = pendingTransfer(id);
            when(transferRepository.findById(id)).thenReturn(Optional.of(t));
            assertThat(transferService.getTransferById(id).getId()).isEqualTo(id);
        }
    }

    @Nested
    @DisplayName("getAllTransfers")
    class GetAllTests {

        @Test
        @DisplayName("should return all transfers from repository")
        void shouldReturnAll() {
            when(transferRepository.findAll()).thenReturn(
                    List.of(pendingTransfer(UUID.randomUUID()), pendingTransfer(UUID.randomUUID())));
            assertThat(transferService.getAllTransfers()).hasSize(2);
        }
    }
}
