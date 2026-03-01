package com.fabiankaraben.corebanking.account.infrastructure.messaging;

import com.fabiankaraben.corebanking.account.application.port.in.AccountUseCase;
import com.fabiankaraben.corebanking.account.domain.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class TransferEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransferEventConsumer.class);

    private final AccountUseCase accountUseCase;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public TransferEventConsumer(AccountUseCase accountUseCase, KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {
        this.accountUseCase = accountUseCase;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "transfer-requested-topic", groupId = "account-service-group")
    @Transactional
    public void handleTransferRequested(String eventJson) {
        log.info("Received TransferRequestedEvent JSON: {}", eventJson);
        TransferRequestedEvent event = null;
        try {
            event = objectMapper.readValue(eventJson, TransferRequestedEvent.class);
            Money amount = Money.of(event.amount());

            // Withdraw from source
            accountUseCase.withdraw(event.sourceAccountId(), amount);

            // Deposit to target
            accountUseCase.deposit(event.targetAccountId(), amount);

            log.info("Transfer {} processed successfully.", event.transferId());
            String responsePayload = objectMapper
                    .writeValueAsString(new TransferResultEvent(event.transferId(), "COMPLETED", null));
            kafkaTemplate.send("transfer-result-topic", event.transferId().toString(), responsePayload);

        } catch (Exception e) {
            String transferIdStr = (event != null && event.transferId() != null) ? event.transferId().toString()
                    : "UNKNOWN";
            log.error("Failed to process transfer {}: {}", transferIdStr, e.getMessage());
            if (event != null && event.transferId() != null) {
                try {
                    String failedPayload = objectMapper
                            .writeValueAsString(new TransferResultEvent(event.transferId(), "FAILED", e.getMessage()));
                    kafkaTemplate.send("transfer-result-topic", event.transferId().toString(), failedPayload);
                } catch (Exception ex) {
                    log.error("Failed to serialize failure event", ex);
                }
            }
        }
    }
}
