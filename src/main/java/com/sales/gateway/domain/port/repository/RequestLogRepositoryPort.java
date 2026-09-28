package com.sales.gateway.domain.port.repository;

import com.sales.gateway.domain.entity.RequestLog;
import java.util.List;
import java.util.UUID;

public interface RequestLogRepositoryPort {

    RequestLog save(RequestLog requestLog);

    List<RequestLog> findByEventId(UUID eventId);
}
