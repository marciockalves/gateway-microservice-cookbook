package com.sales.gateway.infrastructure.persistence.repository;

import com.sales.gateway.domain.entity.RequestLog;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataRequestLogRepository extends JpaRepository<RequestLog, UUID> {

    List<RequestLog> findByEventId(UUID eventId);
}
