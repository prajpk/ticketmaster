package com.ticketmaster.eventservice.repository;

import com.ticketmaster.eventservice.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface VenueRepository extends JpaRepository<Venue, UUID> {
    boolean existsByNameAndCity(String name, String city);
}
