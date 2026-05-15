package com.ticketmaster.allocationservice.repository;

import com.ticketmaster.allocationservice.entity.AllocationIdempotency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AllocationIdempotencyRepository extends JpaRepository<AllocationIdempotency, UUID> {
    Optional<AllocationIdempotency> findByEventIdAndIdempotencyKey(UUID eventId, String idempotencyKey);
    boolean existsByEventIdAndIdempotencyKey(UUID eventId, String idempotencyKey);
}
