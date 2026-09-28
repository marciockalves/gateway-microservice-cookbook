package com.sales.gateway.application.usecase;

import com.sales.gateway.application.dto.event.EventMessageDTO;
import com.sales.gateway.application.dto.integration.CreateContractRequestDTO;
import com.sales.gateway.application.dto.integration.CreateContractResponseDTO;
import com.sales.gateway.domain.entity.Event;
import com.sales.gateway.domain.entity.RequestLog;
import com.sales.gateway.domain.enums.EventStatus;
import com.sales.gateway.domain.enums.TransactionType;
import com.sales.gateway.domain.exception.ExternalApiException;
import com.sales.gateway.domain.port.client.ContractClientPort;
import com.sales.gateway.domain.port.repository.EventRepositoryPort;
import com.sales.gateway.domain.port.repository.RequestLogRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateContractUseCase {

    private final EventRepositoryPort eventRepositoryPort;
    private final RequestLogRepositoryPort requestLogRepositoryPort;
    private final ContractClientPort contractClientPort;

    public void execute(EventMessageDTO message) {
        CreateContractRequestDTO payload = message.payload();

        Event event =
                eventRepositoryPort.save(
                        Event.builder()
                                .contractId(payload.contractId())
                                .eventType(message.eventType().name())
                                .status(EventStatus.RECEIVED)
                                .build());
        log(event, TransactionType.CONSUMER_INPUT, 200, payload.toString());

        try {
            CreateContractResponseDTO response = contractClientPort.sendContract(payload);
            log(event, TransactionType.CONTRACT_API_OUTPUT, 201, response.toString());
            event.setStatus(EventStatus.COMPLETED);
        } catch (ExternalApiException e) {
            log(event, TransactionType.PROCESSING_ERROR, e.getHttpStatus(), e.getMessage());
            event.setStatus(e.isTransient() ? EventStatus.PROCESSING : EventStatus.FAILED);
            event.setErrorMessage(e.getMessage());
        }

        eventRepositoryPort.save(event);
    }

    private void log(Event event, TransactionType transactionType, Integer httpStatus, String payload) {
        requestLogRepositoryPort.save(
                RequestLog.builder()
                        .event(event)
                        .transactionType(transactionType)
                        .httpStatus(httpStatus)
                        .payload(payload)
                        .build());
    }
}
