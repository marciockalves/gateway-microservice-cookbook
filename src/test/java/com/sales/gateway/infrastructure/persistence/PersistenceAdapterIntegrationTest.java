package com.sales.gateway.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.sales.gateway.domain.entity.Event;
import com.sales.gateway.domain.entity.RequestLog;
import com.sales.gateway.domain.enums.EventStatus;
import com.sales.gateway.domain.enums.TransactionType;
import com.sales.gateway.domain.port.repository.EventRepositoryPort;
import com.sales.gateway.domain.port.repository.RequestLogRepositoryPort;
import com.sales.gateway.infrastructure.persistence.adapter.EventRepositoryAdapter;
import com.sales.gateway.infrastructure.persistence.adapter.RequestLogRepositoryAdapter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import({EventRepositoryAdapter.class, RequestLogRepositoryAdapter.class})
class PersistenceAdapterIntegrationTest {

    @Autowired
    private EventRepositoryPort eventRepositoryPort;

    @Autowired
    private RequestLogRepositoryPort requestLogRepositoryPort;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldApplyPrePersistDefaultsWhenSavingEvent() {
        Event saved = eventRepositoryPort.save(newEvent(UUID.randomUUID()));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(EventStatus.RECEIVED);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNull();
    }

    @Test
    void shouldKeepExplicitStatusWhenSavingEvent() {
        Event event = newEvent(UUID.randomUUID());
        event.setStatus(EventStatus.PROCESSING);

        assertThat(eventRepositoryPort.save(event).getStatus()).isEqualTo(EventStatus.PROCESSING);
    }

    @Test
    void shouldSetUpdatedAtOnUpdate() {
        Event saved = eventRepositoryPort.save(newEvent(UUID.randomUUID()));
        entityManager.flush();

        saved.setStatus(EventStatus.COMPLETED);
        eventRepositoryPort.save(saved);
        entityManager.flush();

        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldCascadeRequestLogsAndPopulateCreatedAt() {
        Event event = newEvent(UUID.randomUUID());
        event.addRequestLog(
                RequestLog.builder()
                        .transactionType(TransactionType.CONSUMER_INPUT)
                        .payload("{\"contract\":\"payload\"}")
                        .build());
        event.addRequestLog(
                RequestLog.builder()
                        .transactionType(TransactionType.CONTRACT_API_OUTPUT)
                        .httpStatus(201)
                        .build());

        Event saved = eventRepositoryPort.save(event);
        entityManager.flush();
        entityManager.clear();

        List<RequestLog> logs = requestLogRepositoryPort.findByEventId(saved.getId());
        assertThat(logs).hasSize(2);
        assertThat(logs).allSatisfy(log -> {
            assertThat(log.getId()).isNotNull();
            assertThat(log.getCreatedAt()).isNotNull();
            assertThat(log.getEvent().getId()).isEqualTo(saved.getId());
        });
        assertThat(logs)
                .extracting(RequestLog::getTransactionType)
                .containsExactlyInAnyOrder(
                        TransactionType.CONSUMER_INPUT, TransactionType.CONTRACT_API_OUTPUT);
    }

    @Test
    void shouldFindEventByIdAndContractId() {
        UUID contractId = UUID.randomUUID();
        Event saved = eventRepositoryPort.save(newEvent(contractId));
        entityManager.flush();
        entityManager.clear();

        assertThat(eventRepositoryPort.findById(saved.getId())).isPresent();
        Optional<Event> byContract = eventRepositoryPort.findByContractId(contractId);
        assertThat(byContract).isPresent();
        assertThat(byContract.get().getId()).isEqualTo(saved.getId());
        assertThat(eventRepositoryPort.findByContractId(UUID.randomUUID())).isEmpty();
    }

    @Test
    void shouldSaveRequestLogDirectlyAgainstPersistedEvent() {
        Event saved = eventRepositoryPort.save(newEvent(UUID.randomUUID()));

        RequestLog log =
                requestLogRepositoryPort.save(
                        RequestLog.builder()
                                .event(saved)
                                .transactionType(TransactionType.PROCESSING_ERROR)
                                .payload("boom")
                                .build());

        assertThat(log.getId()).isNotNull();
        assertThat(log.getCreatedAt()).isNotNull();
        assertThat(requestLogRepositoryPort.findByEventId(saved.getId())).hasSize(1);
    }

    private Event newEvent(UUID contractId) {
        return Event.builder().contractId(contractId).eventType("CONTRACT_CREATION").build();
    }
}
