package com.cbp.notification.application;

import com.cbp.notification.adapter.persistence.entity.NotificationJpaEntity;
import com.cbp.notification.adapter.persistence.repository.NotificationJpaRepository;
import com.cbp.notification.domain.model.NotificationStatus;
import com.cbp.notification.domain.model.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@link NotificationService} application service.
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService")
class NotificationServiceTest {

    @Mock
    private NotificationJpaRepository notificationRepository;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationRepository);
    }

    @Nested
    @DisplayName("handleTransferCompleted")
    class HandleCompletedTests {

        @Test
        @DisplayName("should persist a SENT notification of type TRANSFER_COMPLETED")
        void shouldPersistSentNotificationForCompletedTransfer() {
            when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            UUID transferId = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();

            notificationService.handleTransferCompleted(
                    transferId, accountId, new BigDecimal("150.00"), "USD");

            ArgumentCaptor<NotificationJpaEntity> captor =
                    ArgumentCaptor.forClass(NotificationJpaEntity.class);
            verify(notificationRepository).save(captor.capture());

            NotificationJpaEntity saved = captor.getValue();
            assertThat(saved.getTransferId()).isEqualTo(transferId);
            assertThat(saved.getAccountId()).isEqualTo(accountId);
            assertThat(saved.getType()).isEqualTo(NotificationType.TRANSFER_COMPLETED);
            assertThat(saved.getStatus()).isEqualTo(NotificationStatus.SENT);
            assertThat(saved.getMessage()).contains(transferId.toString());
            assertThat(saved.getMessage()).contains("150.00");
            assertThat(saved.getMessage()).contains("USD");
        }
    }

    @Nested
    @DisplayName("handleTransferFailed")
    class HandleFailedTests {

        @Test
        @DisplayName("should persist a SENT notification of type TRANSFER_FAILED with reason")
        void shouldPersistSentNotificationForFailedTransfer() {
            when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            UUID transferId = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            String reason = "Insufficient funds in source account";

            notificationService.handleTransferFailed(
                    transferId, accountId, new BigDecimal("500.00"), "EUR", reason);

            ArgumentCaptor<NotificationJpaEntity> captor =
                    ArgumentCaptor.forClass(NotificationJpaEntity.class);
            verify(notificationRepository).save(captor.capture());

            NotificationJpaEntity saved = captor.getValue();
            assertThat(saved.getType()).isEqualTo(NotificationType.TRANSFER_FAILED);
            assertThat(saved.getStatus()).isEqualTo(NotificationStatus.SENT);
            assertThat(saved.getMessage()).contains(reason);
            assertThat(saved.getMessage()).contains("500.00");
        }
    }
}
