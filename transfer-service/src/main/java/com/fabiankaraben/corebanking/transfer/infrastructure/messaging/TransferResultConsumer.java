package com.fabiankaraben.corebanking.transfer.infrastructure.messaging;

import com.fabiankaraben.corebanking.transfer.application.port.in.TransferUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class TransferResultConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransferResultConsumer.class);
    private final TransferUseCase transferUseCase;
    private final ObjectMapper objectMapper;

    public TransferResultConsumer(TransferUseCase transferUseCase, ObjectMapper objectMapper) {
        this.transferUseCase = transferUseCase;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "transfer-result-topic", groupId = "transfer-service-group")
    public void handleTransferResult(String eventJson) {
        log.info("Received TransferResultEvent JSON: {}", eventJson);
        try {
            TransferResultEvent event = objectMapper.readValue(eventJson, TransferResultEvent.class);
            transferUseCase.processTransferResult(event.transferId(), event.status(), event.reason());
        } catch (Exception e) {
            log.error("Failed to process transfer result: {}", e.getMessage());
        }
    }
}
