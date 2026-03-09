package com.cbp.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * Spring Cloud Gateway configuration for routing and rate limiting.
 *
 * <p>This configuration class defines the core Gateway beans required for:
 * <ul>
 *   <li>Providing a {@link KeyResolver} that partitions rate-limit buckets by
 *       client IP address, ensuring fair resource distribution among callers.</li>
 *   <li>Providing a {@link RedisRateLimiter} bean that backs the token-bucket
 *       algorithm using Redis, enabling distributed rate limiting that is
 *       consistent across multiple Gateway replicas.</li>
 * </ul>
 *
 * <p>Route definitions (downstream URIs, predicates, filters) are declared
 * declaratively in {@code application.yml} to keep Java configuration lean.
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
@Configuration
public class GatewayConfig {

    /**
     * Resolves the rate-limit partition key from the remote client's IP address.
     *
     * <p>Each unique IP gets its own token bucket in Redis, preventing a single
     * client from exhausting the shared request quota of the platform.
     *
     * @return a {@link KeyResolver} that extracts the caller's IP from the
     *         reactive {@code ServerWebExchange}
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(
                exchange.getRequest().getRemoteAddress() != null
                        ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                        : "anonymous"
        );
    }

    /**
     * Creates the Redis-backed rate limiter used by the Gateway route filters.
     *
     * <p>Configuration:
     * <ul>
     *   <li><b>Replenish Rate:</b> 10 tokens/second — steady-state allowed throughput.</li>
     *   <li><b>Burst Capacity:</b> 20 tokens — allows short bursts above the replenish rate.</li>
     *   <li><b>Requested Tokens:</b> 1 token per request — standard single-request cost.</li>
     * </ul>
     *
     * @return a configured {@link RedisRateLimiter} instance
     */
    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(10, 20, 1);
    }
}
