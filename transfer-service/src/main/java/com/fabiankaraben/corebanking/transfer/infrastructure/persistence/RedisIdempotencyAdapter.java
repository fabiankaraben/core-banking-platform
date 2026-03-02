package com.fabiankaraben.corebanking.transfer.infrastructure.persistence;

import com.fabiankaraben.corebanking.transfer.application.port.out.IdempotencyRepositoryPort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Component
public class RedisIdempotencyAdapter implements IdempotencyRepositoryPort {

    private static final String IDEMPOTENCY_KEY_PREFIX = "idempotency:transfer:";
    private static final Duration TTL = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;

    public RedisIdempotencyAdapter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Optional<UUID> getTransferId(String idempotencyKey) {
        String value = redisTemplate.opsForValue().get(IDEMPOTENCY_KEY_PREFIX + idempotencyKey);
        return value != null ? Optional.of(UUID.fromString(value)) : Optional.empty();
    }

    @Override
    public void saveTransferId(String idempotencyKey, UUID transferId) {
        redisTemplate.opsForValue().set(IDEMPOTENCY_KEY_PREFIX + idempotencyKey, transferId.toString(), TTL);
    }
}
