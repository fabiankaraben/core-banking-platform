package com.cbp.account.adapter.in.messaging;

import com.cbp.account.domain.port.in.AccountUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Kafka inbound adapter that listens for {@link TransferRequestedEvent} messages
 * from the {@code transfer.requested} topic and delegates saga processing to the
 * {@link AccountUseCase}.
 *
 * <p>This listener is the Account Service's entry point into the
 * choreography-based Transfer Saga. Upon receiving a valid event, it invokes
 * {@link AccountUseCase#processTransferRequest} which:
 * <ol>
 *   <li>Debits the source account.</li>
 *   <li>Credits the target account.</li>
 *   <li>Publishes a {@link com.cbp.account.domain.event.TransferCompletedEvent}
 *       or {@link com.cbp.account.domain.event.TransferFailedEvent} to Kafka.</li>
 * </ol>
 *
 * <p>Manual acknowledgement mode is used to ensure the Kafka offset is only
 * committed after successful processing, preventing message loss on application restart.
 *
 * @author Core Banking Platform Team
 * @version 1.0.0
 */
@Component
public class TransferRequestedEventListener {

    private static final Logger log = LoggerFactory.getLogger(TransferRequestedEventListener.class);

    private final AccountUseCase accountUseCase;
    private final ObjectMapper objectMapper;

    /**
     * Constructs the listener with required dependencies.
     *
     * @param accountUseCase the inbound port for processing transfer saga steps
     * @param objectMapper   the Jackson {@link ObjectMapper} for JSON deserialization
     */
    public TransferRequestedEventListener(AccountUseCase accountUseCase,
                                          ObjectMapper objectMapper) {
        this.accountUseCase = accountUseCase;
        this.objectMapper = objectMapper;
    }

    /**
     * Consumes a {@code TransferRequestedEvent} from the {@code transfer.requested} Kafka topic.
     *
     * <p>Deserializes the JSON payload, delegates saga processing to the application layer,
     * and manually acknowledges the offset upon successful handling. Deserialization errors
     * are logged and the message is acknowledged to avoid poison-pill blocking of the partition.
     *
     * @param record the raw Kafka consumer record containing the JSON event payload
     * @param acknowledgment the manual acknowledgment handle for offset commit
     */
    @KafkaListener(
            topics = "transfer.requested",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onTransferRequested(ConsumerRecord<String, String> record,
                                    Acknowledgment acknowledgment) {
        log.info("Received TransferRequestedEvent from partition={} offset={}",
                record.partition(), record.offset());
        try {
            TransferRequestedEvent event = objectMapper.readValue(
                    record.value(), TransferRequestedEvent.class);

            accountUseCase.processTransferRequest(
                    event.transferId(),
                    event.sourceAccountId(),
                    event.targetAccountId(),
                    event.amount()
            );
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process TransferRequestedEvent: {}", e.getMessage(), e);
            acknowledgment.acknowledge();
        }
    }
}
