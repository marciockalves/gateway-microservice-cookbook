package com.sales.gateway.infrastructure.persistence.adapter;

import com.sales.gateway.domain.entity.Event;
import com.sales.gateway.domain.port.repository.EventRepositoryPort;
import com.sales.gateway.infrastructure.persistence.repository.SpringDataEventRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventRepositoryAdapter implements EventRepositoryPort {

    private final SpringDataEventRepository repository;

    @Override
    public Event save(Event event) {
        return repository.save(event);
    }

    @Override
    public Optional<Event> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public Optional<Event> findByContractId(UUID contractId) {
        return repository.findByContractId(contractId);
    }
}
