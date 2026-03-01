package com.fabiankaraben.corebanking.transfer.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

public interface SpringDataOutboxRepository extends JpaRepository<OutboxEntity, UUID> {
    List<OutboxEntity> findAllByOrderByCreatedAtAsc();
}
