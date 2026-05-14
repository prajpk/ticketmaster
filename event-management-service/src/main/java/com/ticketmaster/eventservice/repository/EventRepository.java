package com.ticketmaster.eventservice.repository;

import com.ticketmaster.eventservice.entity.Event;
import com.ticketmaster.eventservice.enums.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {
    Page<Event> findByStatus(EventStatus status, Pageable pageable);
    List<Event> findByOrganiserId(UUID organiserId);
}
