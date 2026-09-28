package com.sales.gateway.infrastructure.persistence.repository;

import com.sales.gateway.domain.entity.Event;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataEventRepository extends JpaRepository<Event, UUID> {

    Optional<Event> findByContractId(UUID contractId);
}
