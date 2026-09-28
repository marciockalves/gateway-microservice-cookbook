package com.sales.gateway.application.dto.integration;

import com.sales.gateway.domain.enums.ContractModel;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateContractRequestDTO(
        UUID contractId,
        CustomerDTO customerId,
        ContractModel contractModel,
        LocalDate endDate,
        List<ContractItemDTO> items) {}
