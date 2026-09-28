package com.sales.gateway.infrastructure.messaging.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sales.gateway.application.dto.event.EventMessageDTO;
import com.sales.gateway.application.usecase.CreateContractUseCase;
import com.sales.gateway.domain.exception.DomainException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ContractEventListener {

    private final ObjectMapper objectMapper;
    private final CreateContractUseCase createContractUseCase;

    @KafkaListener(
            topics = "${app.kafka.topics.contract-events}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void onMessage(String message) {
        EventMessageDTO event;
        try {
            event = objectMapper.readValue(message, EventMessageDTO.class);
        } catch (JsonProcessingException e) {
            throw new DomainException("Unable to deserialize contract event", e);
        }
        log.info("Received contract event {}", event.referenceId());
        createContractUseCase.execute(event);
    }
}
