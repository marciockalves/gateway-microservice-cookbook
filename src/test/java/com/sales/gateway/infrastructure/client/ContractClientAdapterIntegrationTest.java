package com.sales.gateway.infrastructure.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sales.gateway.application.dto.integration.ContractItemDTO;
import com.sales.gateway.application.dto.integration.CreateContractRequestDTO;
import com.sales.gateway.application.dto.integration.CreateContractResponseDTO;
import com.sales.gateway.application.dto.integration.CustomerDTO;
import com.sales.gateway.domain.enums.ContractModel;
import com.sales.gateway.domain.exception.DomainException;
import com.sales.gateway.domain.port.client.ContractClientPort;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWireMock(port = 0, stubs = "file:./wiremock/mappings")
@TestPropertySource(
        properties = "app.services.contract-api.url=http://localhost:${wiremock.server.port}")
class ContractClientAdapterIntegrationTest {

    private static final UUID CONTRACT_ID = UUID.fromString("770e8400-e29b-41d4-a716-446655440000");
    private static final UUID FAILING_CONTRACT_ID =
            UUID.fromString("880e8400-e29b-41d4-a716-446655440000");
    private static final String EXPECTED_BODY =
            """
            {
              "contractId": "770e8400-e29b-41d4-a716-446655440000",
              "customerId": {
                "accountId": "550e8400-e29b-41d4-a716-446655440000",
                "document": "1233445656",
                "fullName": "nome completo",
                "email": "email@mail.com"
              },
              "contractModel": "PREPAID",
              "endDate": "2026-12-31",
              "items": [
                {
                  "productId": "PROD-8801",
                  "productName": "Plano Internet 500 Mega",
                  "quantity": 1,
                  "unitPrice": 120.00
                }
              ]
            }
            """;

    @Autowired private ContractClientPort contractClientPort;

    @Test
    void shouldSendContractAndParseResponse() {
        CreateContractResponseDTO response = contractClientPort.sendContract(validRequest());

        assertThat(response)
                .isEqualTo(
                        new CreateContractResponseDTO(
                                CONTRACT_ID, "CREATED", LocalDateTime.parse("2026-09-28T12:00:00")));
        verify(
                postRequestedFor(urlEqualTo(ContractClientAdapter.CONTRACTS_PATH))
                        .withRequestBody(equalToJson(EXPECTED_BODY)));
    }

    @Test
    void shouldTranslateDownstreamErrorIntoDomainException() {
        stubFor(
                post(urlEqualTo(ContractClientAdapter.CONTRACTS_PATH))
                        .withRequestBody(
                                equalToJson(
                                        "{\"contractId\":\"%s\"}".formatted(FAILING_CONTRACT_ID),
                                        true,
                                        true))
                        .willReturn(aResponse().withStatus(503).withBody("downstream unavailable")));

        CreateContractRequestDTO request =
                new CreateContractRequestDTO(
                        FAILING_CONTRACT_ID, null, ContractModel.POSTPAID, null, List.of());

        assertThatThrownBy(() -> contractClientPort.sendContract(request))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("503")
                .hasMessageContaining("downstream unavailable");
    }

    private CreateContractRequestDTO validRequest() {
        return new CreateContractRequestDTO(
                CONTRACT_ID,
                new CustomerDTO(
                        UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
                        "1233445656",
                        "nome completo",
                        "email@mail.com"),
                ContractModel.PREPAID,
                LocalDate.of(2026, 12, 31),
                List.of(
                        new ContractItemDTO(
                                "PROD-8801",
                                "Plano Internet 500 Mega",
                                1,
                                new BigDecimal("120.00"))));
    }
}
