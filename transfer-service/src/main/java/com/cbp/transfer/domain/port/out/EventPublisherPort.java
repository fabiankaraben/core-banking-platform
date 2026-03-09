package com.cbp.transfer.domain.port.out;

import com.cbp.transfer.domain.event.DomainEvent;

/**
 * Secondary outbound port for publishing domain events from the Transfer Service.
 *
 * <p>The concrete implementation is provided by {@code KafkaEventPublisherAdapter},
 * which routes each event to the appropriate Kafka topic. The domain and application
 * layers have no knowledge of Kafka or any other messaging infrastructure.
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
public interface EventPublisherPort {

    /**
     * Publishes a domain event to the configured event stream.
     *
     * @param event the domain event to publish; must not be null
     */
    void publish(DomainEvent event);
}
