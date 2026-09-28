package com.sales.gateway.infrastructure.client;

import com.sales.gateway.application.dto.integration.CreateContractRequestDTO;
import com.sales.gateway.application.dto.integration.CreateContractResponseDTO;
import com.sales.gateway.domain.exception.DomainException;
import com.sales.gateway.domain.port.client.ContractClientPort;
import java.nio.charset.StandardCharsets;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
public class ContractClientAdapter implements ContractClientPort {

    static final String CONTRACTS_PATH = "/api/v1/contracts";

    private final RestClient restClient;

    public ContractClientAdapter(RestClient contractRestClient) {
        this.restClient = contractRestClient;
    }

    @Override
    public CreateContractResponseDTO sendContract(CreateContractRequestDTO request) {
        try {
            return restClient
                    .post()
                    .uri(CONTRACTS_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(request)
                    .exchange(
                            (req, response) -> {
                                if (response.getStatusCode().isError()) {
                                    throw new DomainException(
                                            "Contract API responded with status %s: %s"
                                                    .formatted(
                                                            response.getStatusCode().value(),
                                                            new String(
                                                                    response.getBody()
                                                                            .readAllBytes(),
                                                                    StandardCharsets.UTF_8)));
                                }
                                return response.bodyTo(CreateContractResponseDTO.class);
                            });
        } catch (ResourceAccessException e) {
            throw new DomainException("Contract API is unreachable", e);
        }
    }
}
