package com.fabiankaraben.corebanking.transfer.application.port.out;

import java.util.UUID;

public interface OutboxRepositoryPort {
    void save(String aggregateType, UUID aggregateId, String topic, String payload);
}
