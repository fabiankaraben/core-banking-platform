package com.fabiankaraben.corebanking.notification.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface SpringDataNotificationRepository extends JpaRepository<NotificationEntity, UUID> {}
