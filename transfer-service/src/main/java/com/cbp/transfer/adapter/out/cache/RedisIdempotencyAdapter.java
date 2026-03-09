package com.cbp.transfer.adapter.out.cache;

import com.cbp.transfer.domain.port.out.IdempotencyPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

/**
 * Redis-backed implementation of the {@link IdempotencyPort} outbound port.
 *
 * <p>Stores idempotency keys and their associated response payloads in Redis with a
 * 24-hour TTL. This enables clients to safely replay failed HTTP requests without
 * risk of duplicate transfer creation or duplicate Saga initiation.
 *
 * <p>Redis key format: {@code idempotency:transfer:<idempotencyKey>}
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
@Component
public class RedisIdempotencyAdapter implements IdempotencyPort {

    private static final Logger log = LoggerFactory.getLogger(RedisIdempotencyAdapter.class);

    /** Redis key prefix to namespace idempotency entries. */
    private static final String KEY_PREFIX = "idempotency:transfer:";

    /** Time-to-live for idempotency entries — 24 hours per specification. */
    private static final Duration TTL = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;

    /**
     * Constructs the Redis idempotency adapter.
     *
     * @param redisTemplate the Spring Data Redis string template for key/value operations
     */
    public RedisIdempotencyAdapter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Looks up the Redis key {@code idempotency:transfer:<idempotencyKey>}.
     */
    @Override
    public Optional<String> get(String idempotencyKey) {
        String value = redisTemplate.opsForValue().get(KEY_PREFIX + idempotencyKey);
        if (value != null) {
            log.debug("Idempotency cache hit for key={}", idempotencyKey);
        }
        return Optional.ofNullable(value);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Stores the response under {@code idempotency:transfer:<idempotencyKey>}
     * with a 24-hour TTL.
     */
    @Override
    public void store(String idempotencyKey, String responseJson) {
        redisTemplate.opsForValue().set(KEY_PREFIX + idempotencyKey, responseJson, TTL);
        log.debug("Stored idempotency response for key={}, TTL={}", idempotencyKey, TTL);
    }
}
