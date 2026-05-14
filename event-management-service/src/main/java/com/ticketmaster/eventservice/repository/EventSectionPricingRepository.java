package com.ticketmaster.eventservice.repository;

import com.ticketmaster.eventservice.entity.EventSectionPricing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventSectionPricingRepository extends JpaRepository<EventSectionPricing, UUID> {
    List<EventSectionPricing> findByEventId(UUID eventId);
    Optional<EventSectionPricing> findByEventIdAndSectionId(UUID eventId, UUID sectionId);
}
