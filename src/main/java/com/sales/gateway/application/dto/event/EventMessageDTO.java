package com.sales.gateway.application.dto.event;

import com.sales.gateway.application.dto.integration.CreateContractRequestDTO;
import com.sales.gateway.domain.enums.EventType;
import java.util.UUID;

public record EventMessageDTO(
        UUID referenceId, EventType eventType, CreateContractRequestDTO payload) {}
