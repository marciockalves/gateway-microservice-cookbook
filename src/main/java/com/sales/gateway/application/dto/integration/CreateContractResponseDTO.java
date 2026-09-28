package com.sales.gateway.application.dto.integration;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateContractResponseDTO(
        UUID contractId, String externalStatus, LocalDateTime processedAt) {}
