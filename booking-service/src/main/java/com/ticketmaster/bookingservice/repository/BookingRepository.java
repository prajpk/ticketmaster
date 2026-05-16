package com.ticketmaster.bookingservice.repository;

import com.ticketmaster.bookingservice.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    List<Booking> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<Booking> findByPaymentId(UUID paymentId);

    boolean existsByPaymentId(UUID paymentId);
}