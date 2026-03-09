package com.cbp.notification.adapter.persistence.repository;

import com.cbp.notification.adapter.persistence.entity.NotificationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link NotificationJpaEntity}.
 *
 * <p>Referenced only by the persistence adapter. Not injected into domain or application layers.
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
public interface NotificationJpaRepository extends JpaRepository<NotificationJpaEntity, UUID> {

    /**
     * Finds all notifications associated with a given transfer saga ID.
     *
     * @param transferId the saga correlation ID to search by
     * @return list of notifications for the given transfer, ordered by creation time
     */
    List<NotificationJpaEntity> findByTransferIdOrderByCreatedAtAsc(UUID transferId);
}
