package com.ticketmaster.paymentservice.repository;

import com.ticketmaster.paymentservice.entity.PaymentIdempotency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentIdempotencyRepository extends JpaRepository<PaymentIdempotency, UUID> {
    Optional<PaymentIdempotency> findByUserIdAndIdempotencyKey(UUID userId, String idempotencyKey);
    boolean existsByUserIdAndIdempotencyKey(UUID userId, String idempotencyKey);
}
