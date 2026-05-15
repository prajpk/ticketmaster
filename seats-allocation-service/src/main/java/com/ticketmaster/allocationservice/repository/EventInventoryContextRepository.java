package com.ticketmaster.allocationservice.repository;

import com.ticketmaster.allocationservice.entity.EventInventoryContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventInventoryContextRepository extends JpaRepository<EventInventoryContext, UUID> {
    Optional<EventInventoryContext> findByEventId(UUID eventId);
    boolean existsByEventId(UUID eventId);
}
