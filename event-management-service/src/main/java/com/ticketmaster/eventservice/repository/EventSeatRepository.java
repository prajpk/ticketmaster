package com.ticketmaster.eventservice.repository;

import com.ticketmaster.eventservice.entity.EventSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventSeatRepository extends JpaRepository<EventSeat, UUID> {
    List<EventSeat> findByEventId(UUID eventId);
    long countByEventId(UUID eventId);
}
