package com.fabiankaraben.corebanking.notification.infrastructure.messaging;

import com.fabiankaraben.corebanking.notification.domain.NotificationEntity;
import com.fabiankaraben.corebanking.notification.domain.SpringDataNotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);
    private final SpringDataNotificationRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NotificationConsumer(SpringDataNotificationRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(topics = "transfer-result-topic", groupId = "notification-service-group")
    public void handleTransferResult(String eventJson) {
        log.info("Received TransferResultEvent JSON for Notification: {}", eventJson);
        try {
            TransferResultEvent event = objectMapper.readValue(eventJson, TransferResultEvent.class);
            String message = "COMPLETED".equals(event.status())
                    ? "Your transfer " + event.transferId() + " was successful."
                    : "Your transfer " + event.transferId() + " failed: " + event.reason();

            NotificationEntity notification = new NotificationEntity(
                    UUID.randomUUID(),
                    event.transferId(),
                    message,
                    "SENT",
                    LocalDateTime.now());
            repository.save(notification);
            log.info("Notification saved for transfer: {}", event.transferId());
        } catch (Exception e) {
            log.error("Failed to process notification: {}", e.getMessage());
        }
    }
}
