package com.fabiankaraben.corebanking.transfer.infrastructure.persistence;

import com.fabiankaraben.corebanking.transfer.application.port.out.OutboxRepositoryPort;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class OutboxPersistenceAdapter implements OutboxRepositoryPort {

    private final SpringDataOutboxRepository repository;

    public OutboxPersistenceAdapter(SpringDataOutboxRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(String aggregateType, UUID aggregateId, String topic, String payload) {
        OutboxEntity entity = new OutboxEntity(
                UUID.randomUUID(),
                aggregateId,
                aggregateType,
                topic,
                payload,
                LocalDateTime.now());
        repository.save(entity);
    }
}
