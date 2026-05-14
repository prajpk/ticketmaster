package com.ticketmaster.eventservice.repository;

import com.ticketmaster.eventservice.entity.VenueSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VenueSeatRepository extends JpaRepository<VenueSeat, UUID> {
    List<VenueSeat> findBySectionId(UUID sectionId);
    List<VenueSeat> findByVenueId(UUID venueId);
    boolean existsByVenueIdAndSeatCode(UUID venueId, String seatCode);
}
