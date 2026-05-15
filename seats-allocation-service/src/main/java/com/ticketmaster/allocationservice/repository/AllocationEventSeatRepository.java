package com.ticketmaster.allocationservice.repository;

import com.ticketmaster.allocationservice.entity.AllocationEventSeat;
import com.ticketmaster.allocationservice.enums.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AllocationEventSeatRepository extends JpaRepository<AllocationEventSeat, UUID> {

    List<AllocationEventSeat> findByEventId(UUID eventId);

    List<AllocationEventSeat> findByEventIdAndStatus(UUID eventId, SeatStatus status);

    Optional<AllocationEventSeat> findByEventIdAndVenueSeatId(UUID eventId, UUID venueSeatId);

    List<AllocationEventSeat> findByIdInAndEventId(List<UUID> ids, UUID eventId);

    @Modifying
    @Query("UPDATE AllocationEventSeat s SET s.status = 'AVAILABLE', s.lockedBy = null, " +
           "s.lockedAt = null, s.lockExpiresAt = null " +
           "WHERE s.status = 'LOCKED' AND s.lockExpiresAt < :now")
    int releaseExpiredLocks(OffsetDateTime now);
}
