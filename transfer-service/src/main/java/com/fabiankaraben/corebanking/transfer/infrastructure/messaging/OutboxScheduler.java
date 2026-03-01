package com.fabiankaraben.corebanking.transfer.infrastructure.messaging;

import com.fabiankaraben.corebanking.transfer.infrastructure.persistence.OutboxEntity;
import com.fabiankaraben.corebanking.transfer.infrastructure.persistence.SpringDataOutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@EnableScheduling
public class OutboxScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxScheduler.class);
    private final SpringDataOutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxScheduler(SpringDataOutboxRepository outboxRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelayString = "1000") // Run every 1 second
    public void processOutbox() {
        List<OutboxEntity> events = outboxRepository.findAllByOrderByCreatedAtAsc();
        for (OutboxEntity event : events) {
            log.info("Publishing outbox event for {} {}", event.getAggregateType(), event.getAggregateId());
            try {
                kafkaTemplate.send(event.getEventType(), event.getAggregateId().toString(), event.getPayload())
                        .whenComplete((result, ex) -> {
                            if (ex == null) {
                                // Delete on success callback to avoid long DB locks
                                outboxRepository.delete(event);
                                log.debug("Successfully published and deleted outbox event {}", event.getId());
                            } else {
                                log.error("Failed to publish outbox event {}: {}", event.getId(), ex.getMessage());
                            }
                        });
            } catch (Exception e) {
                log.error("Error processing outbox event {}", event.getId(), e);
            }
        }
    }
}
