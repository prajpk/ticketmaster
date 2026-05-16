package com.ticketmaster.bookingservice.repository;

import com.ticketmaster.bookingservice.entity.BookingFulfillmentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingFulfillmentRequestRepository extends JpaRepository<BookingFulfillmentRequest, UUID> {
    Optional<BookingFulfillmentRequest> findByPaymentId(UUID paymentId);
    boolean existsByPaymentId(UUID paymentId);
}
