package com.sales.gateway.domain.port.client;

import com.sales.gateway.application.dto.integration.CreateContractRequestDTO;
import com.sales.gateway.application.dto.integration.CreateContractResponseDTO;

public interface ContractClientPort {

    CreateContractResponseDTO sendContract(CreateContractRequestDTO request);
}
