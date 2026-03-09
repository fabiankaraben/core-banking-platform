package com.cbp.notification.application;

import com.cbp.notification.adapter.persistence.entity.NotificationJpaEntity;
import com.cbp.notification.adapter.persistence.repository.NotificationJpaRepository;
import com.cbp.notification.domain.model.Notification;
import com.cbp.notification.domain.model.NotificationStatus;
import com.cbp.notification.domain.model.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Application service responsible for processing and persisting customer notifications.
 *
 * <p>Invoked by the Kafka event listener when transfer outcome events arrive.
 * This service:
 * <ol>
 *   <li>Renders a human-readable notification message based on the event type.</li>
 *   <li>Simulates dispatching the notification to the customer (email/SMS).</li>
 *   <li>Persists an immutable audit record to {@code notification_db}.</li>
 * </ol>
 *
 * <p>In a production system, step 2 would integrate with a provider such as
 * AWS SES, Twilio, or SendGrid. For this portfolio demonstration, the dispatch
 * is simulated via structured logging.
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
@Service
@Transactional
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationJpaRepository notificationRepository;

    /**
     * Constructs the service with the required persistence repository.
     *
     * @param notificationRepository the JPA repository for persisting notification records
     */
    public NotificationService(NotificationJpaRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * Processes a transfer-completed event: renders the success message, simulates
     * dispatch, and persists the audit record.
     *
     * @param transferId      the saga correlation UUID
     * @param sourceAccountId UUID of the debited account (notification recipient)
     * @param amount          the transferred amount
     * @param currency        ISO-4217 currency code
     */
    public void handleTransferCompleted(UUID transferId, UUID sourceAccountId,
                                        BigDecimal amount, String currency) {
        String message = String.format(
                "Your transfer of %s %s (reference: %s) has been completed successfully.",
                amount, currency, transferId);

        simulateDispatch(sourceAccountId, message);

        Notification notification = new Notification(
                transferId, sourceAccountId, NotificationType.TRANSFER_COMPLETED,
                amount, currency, NotificationStatus.SENT, message);

        notificationRepository.save(toJpaEntity(notification));
        log.info("Notification persisted: transferId={}, type=TRANSFER_COMPLETED, status=SENT",
                transferId);
    }

    /**
     * Processes a transfer-failed event: renders the failure message, simulates
     * dispatch, and persists the audit record.
     *
     * @param transferId      the saga correlation UUID
     * @param sourceAccountId UUID of the attempted source account
     * @param amount          the attempted transfer amount
     * @param currency        ISO-4217 currency code
     * @param reason          human-readable failure reason
     */
    public void handleTransferFailed(UUID transferId, UUID sourceAccountId,
                                     BigDecimal amount, String currency, String reason) {
        String message = String.format(
                "Your transfer of %s %s (reference: %s) could not be completed. Reason: %s",
                amount, currency, transferId, reason);

        simulateDispatch(sourceAccountId, message);

        Notification notification = new Notification(
                transferId, sourceAccountId, NotificationType.TRANSFER_FAILED,
                amount, currency, NotificationStatus.SENT, message);

        notificationRepository.save(toJpaEntity(notification));
        log.info("Notification persisted: transferId={}, type=TRANSFER_FAILED, status=SENT",
                transferId);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Simulates dispatching a notification to the customer.
     *
     * <p>In production this would call an external email/SMS provider.
     * Here we emit a structured log entry that can be captured by the
     * Prometheus + Grafana observability stack.
     *
     * @param accountId the account owner to notify
     * @param message   the rendered notification message
     */
    private void simulateDispatch(UUID accountId, String message) {
        log.info("[NOTIFICATION DISPATCH] accountId={} | message=\"{}\"", accountId, message);
    }

    private NotificationJpaEntity toJpaEntity(Notification n) {
        return new NotificationJpaEntity(
                n.getId(), n.getTransferId(), n.getAccountId(), n.getType(),
                n.getAmount(), n.getCurrency(), n.getStatus(), n.getMessage(), n.getCreatedAt()
        );
    }
}
