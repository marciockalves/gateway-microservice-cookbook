package com.sales.gateway.infrastructure.persistence.adapter;

import com.sales.gateway.domain.entity.RequestLog;
import com.sales.gateway.domain.port.repository.RequestLogRepositoryPort;
import com.sales.gateway.infrastructure.persistence.repository.SpringDataRequestLogRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RequestLogRepositoryAdapter implements RequestLogRepositoryPort {

    private final SpringDataRequestLogRepository repository;

    @Override
    public RequestLog save(RequestLog requestLog) {
        return repository.save(requestLog);
    }

    @Override
    public List<RequestLog> findByEventId(UUID eventId) {
        return repository.findByEventId(eventId);
    }
}
