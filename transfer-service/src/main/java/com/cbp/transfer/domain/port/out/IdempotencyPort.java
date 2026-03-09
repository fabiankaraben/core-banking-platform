package com.cbp.transfer.domain.port.out;

import java.util.Optional;

/**
 * Secondary outbound port for idempotency key management.
 *
 * <p>Idempotency guarantees that submitting the same transfer request multiple times
 * (e.g., due to network retries) produces exactly one transfer, not duplicates.
 * The concrete implementation stores keys and response payloads in Redis with a
 * 24-hour TTL.
 *
 * <p>Usage pattern in the application layer:
 * <pre>{@code
 *   Optional<String> cached = idempotency.get(key);
 *   if (cached.isPresent()) return deserialize(cached.get());
 *   Transfer t = createTransfer(...);
 *   idempotency.store(key, serialize(t));
 *   return t;
 * }</pre>
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
public interface IdempotencyPort {

    /**
     * Retrieves a previously cached response for the given idempotency key.
     *
     * @param idempotencyKey the client-supplied key to look up
     * @return an {@link Optional} containing the cached JSON response if present,
     *         or empty if this is a new request
     */
    Optional<String> get(String idempotencyKey);

    /**
     * Stores a response payload associated with an idempotency key.
     *
     * <p>The entry expires after 24 hours, allowing clients to safely retry
     * failed requests within that window.
     *
     * @param idempotencyKey the client-supplied key to associate with the response
     * @param responseJson   the JSON-serialized response payload to cache
     */
    void store(String idempotencyKey, String responseJson);
}
