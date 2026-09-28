package com.sales.gateway.domain.port.repository;

import com.sales.gateway.domain.entity.Event;
import java.util.Optional;
import java.util.UUID;

public interface EventRepositoryPort {

    Event save(Event event);

    Optional<Event> findById(UUID id);

    Optional<Event> findByContractId(UUID contractId);
}
