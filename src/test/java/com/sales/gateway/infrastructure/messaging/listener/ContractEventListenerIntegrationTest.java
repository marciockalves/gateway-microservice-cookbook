package com.sales.gateway.infrastructure.messaging.listener;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.awaitility.Awaitility.await;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sales.gateway.application.dto.event.EventMessageDTO;
import com.sales.gateway.application.dto.integration.ContractItemDTO;
import com.sales.gateway.application.dto.integration.CreateContractRequestDTO;
import com.sales.gateway.application.dto.integration.CustomerDTO;
import com.sales.gateway.domain.entity.Event;
import com.sales.gateway.domain.enums.ContractModel;
import com.sales.gateway.domain.enums.EventStatus;
import com.sales.gateway.domain.enums.EventType;
import com.sales.gateway.domain.enums.TransactionType;
import com.sales.gateway.domain.port.repository.EventRepositoryPort;
import com.sales.gateway.domain.port.repository.RequestLogRepositoryPort;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWireMock(port = 0)
@EmbeddedKafka(partitions = 1, topics = ContractEventListenerIntegrationTest.TOPIC)
@TestPropertySource(
        properties = {
            "app.services.contract-api.url=http://localhost:${wiremock.server.port}",
            "app.kafka.topics.contract-events=" + ContractEventListenerIntegrationTest.TOPIC,
            "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
            "spring.kafka.listener.auto-startup=true"
        })
class ContractEventListenerIntegrationTest {

    static final String TOPIC = "contract-events";

    private static final String CONTRACTS_PATH = "/api/v1/contracts";

    @Autowired private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private EventRepositoryPort eventRepositoryPort;
    @Autowired private RequestLogRepositoryPort requestLogRepositoryPort;

    @Test
    void shouldCompleteEventWhenContractApiAccepts() throws Exception {
        UUID contractId = UUID.randomUUID();
        stubContractApi(contractId, 201, createdResponse(contractId));

        publish(contractId);

        Event event = awaitStatus(contractId, EventStatus.COMPLETED);
        assertThat(requestLogRepositoryPort.findByEventId(event.getId()))
                .extracting(log -> log.getTransactionType(), log -> log.getHttpStatus())
                .containsExactlyInAnyOrder(
                        tuple(TransactionType.CONSUMER_INPUT, 200),
                        tuple(TransactionType.CONTRACT_API_OUTPUT, 201));
    }

    @Test
    void shouldKeepEventProcessingWhenContractApiFailsWithServerError() throws Exception {
        UUID contractId = UUID.randomUUID();
        stubContractApi(contractId, 500, "internal error");

        publish(contractId);

        Event event = awaitStatus(contractId, EventStatus.PROCESSING);
        assertThat(event.getErrorMessage()).contains("500");
        assertThat(requestLogRepositoryPort.findByEventId(event.getId()))
                .extracting(log -> log.getTransactionType())
                .contains(TransactionType.PROCESSING_ERROR);
    }

    @Test
    void shouldFailEventWhenContractApiRejectsWithClientError() throws Exception {
        UUID contractId = UUID.randomUUID();
        stubContractApi(contractId, 400, "invalid contract");

        publish(contractId);

        Event event = awaitStatus(contractId, EventStatus.FAILED);
        assertThat(event.getErrorMessage()).contains("400");
        assertThat(requestLogRepositoryPort.findByEventId(event.getId()))
                .extracting(log -> log.getHttpStatus())
                .contains(400);
    }

    private void stubContractApi(UUID contractId, int status, String body) {
        stubFor(
                post(urlEqualTo(CONTRACTS_PATH))
                        .withRequestBody(
                                equalToJson(
                                        "{\"contractId\":\"%s\"}".formatted(contractId),
                                        true,
                                        true))
                        .willReturn(
                                aResponse()
                                        .withStatus(status)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(body)));
    }

    private String createdResponse(UUID contractId) {
        return """
               {"contractId":"%s","externalStatus":"CREATED","processedAt":"2026-09-28T12:00:00"}
               """
                .formatted(contractId);
    }

    private void publish(UUID contractId) throws Exception {
        EventMessageDTO message =
                new EventMessageDTO(
                        UUID.randomUUID(), EventType.CONTRACT_CREATION, payload(contractId));
        kafkaTemplate.send(TOPIC, objectMapper.writeValueAsString(message));
    }

    private Event awaitStatus(UUID contractId, EventStatus expected) {
        return await().atMost(Duration.ofSeconds(30))
                .until(
                        () -> eventRepositoryPort.findByContractId(contractId).orElse(null),
                        event -> event != null && event.getStatus() == expected);
    }

    private CreateContractRequestDTO payload(UUID contractId) {
        return new CreateContractRequestDTO(
                contractId,
                new CustomerDTO(UUID.randomUUID(), "1233445656", "nome completo", "email@mail.com"),
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
