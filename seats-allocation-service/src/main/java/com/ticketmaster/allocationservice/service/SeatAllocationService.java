package com.ticketmaster.allocationservice.service;

import com.ticketmaster.allocationservice.dto.request.*;
import com.ticketmaster.allocationservice.dto.response.*;
import com.ticketmaster.allocationservice.entity.*;
import com.ticketmaster.allocationservice.enums.SeatStatus;
import com.ticketmaster.allocationservice.exception.*;
import com.ticketmaster.allocationservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatAllocationService {

    private static final int LOCK_DURATION_MINUTES = 10;

    private final AllocationEventSeatRepository seatRepository;
    private final EventInventoryContextRepository contextRepository;
    private final AllocationIdempotencyRepository idempotencyRepository;

    // ---- Public: Get available seats ----
    public List<SeatResponse> getAvailableSeats(UUID eventId) {
        validateEventExists(eventId);
        return seatRepository.findByEventIdAndStatus(eventId, SeatStatus.AVAILABLE)
                .stream().map(this::mapToSeatResponse).collect(Collectors.toList());
    }

    // ---- Public: Get all seats for event ----
    public List<SeatResponse> getAllSeats(UUID eventId) {
        validateEventExists(eventId);
        return seatRepository.findByEventId(eventId)
                .stream().map(this::mapToSeatResponse).collect(Collectors.toList());
    }

    // ---- Internal: Lock seats during checkout ----
    @Transactional
    public LockSeatsResponse lockSeats(LockSeatsRequest request) {
        validateEventExists(request.getEventId());

        // Idempotency check
        if (idempotencyRepository.existsByEventIdAndIdempotencyKey(
                request.getEventId(), request.getIdempotencyKey())) {
            log.info("Duplicate lock request - idempotencyKey: {}", request.getIdempotencyKey());
            List<AllocationEventSeat> lockedSeats =
                    seatRepository.findByIdInAndEventId(request.getSeatIds(), request.getEventId());
            return buildLockResponse(request.getEventId(), lockedSeats, request.getIdempotencyKey());
        }

        // Fetch and validate seats
        List<AllocationEventSeat> seats =
                seatRepository.findByIdInAndEventId(request.getSeatIds(), request.getEventId());

        if (seats.size() != request.getSeatIds().size()) {
            throw new BadRequestException("One or more seat IDs not found for this event");
        }

        List<AllocationEventSeat> unavailable = seats.stream()
                .filter(s -> s.getStatus() != SeatStatus.AVAILABLE)
                .collect(Collectors.toList());

        if (!unavailable.isEmpty()) {
            String codes = unavailable.stream().map(AllocationEventSeat::getSeatCode)
                    .collect(Collectors.joining(", "));
            throw new SeatNotAvailableException("Seats not available: " + codes);
        }

        // Lock the seats
        OffsetDateTime lockExpiresAt = OffsetDateTime.now().plusMinutes(LOCK_DURATION_MINUTES);
        seats.forEach(seat -> {
            seat.setStatus(SeatStatus.LOCKED);
            seat.setLockedBy(request.getUserId());
            seat.setLockedAt(OffsetDateTime.now());
            seat.setLockExpiresAt(lockExpiresAt);
        });
        seatRepository.saveAll(seats);

        // Save idempotency record
        idempotencyRepository.save(AllocationIdempotency.builder()
                .eventId(request.getEventId())
                .idempotencyKey(request.getIdempotencyKey())
                .operation("LOCK")
                .build());

        log.info("Locked {} seats for event {} by user {}",
                seats.size(), request.getEventId(), request.getUserId());
        return buildLockResponse(request.getEventId(), seats, request.getIdempotencyKey());
    }

    // ---- Internal: Confirm seats after payment success ----
    @Transactional
    public List<SeatResponse> confirmSeats(ConfirmSeatsRequest request) {
        validateEventExists(request.getEventId());

        // Idempotency check
        if (idempotencyRepository.existsByEventIdAndIdempotencyKey(
                request.getEventId(), request.getIdempotencyKey())) {
            log.info("Duplicate confirm request - idempotencyKey: {}", request.getIdempotencyKey());
            return seatRepository.findByIdInAndEventId(request.getSeatIds(), request.getEventId())
                    .stream().map(this::mapToSeatResponse).collect(Collectors.toList());
        }

        List<AllocationEventSeat> seats =
                seatRepository.findByIdInAndEventId(request.getSeatIds(), request.getEventId());

        if (seats.size() != request.getSeatIds().size()) {
            throw new BadRequestException("One or more seat IDs not found");
        }

        seats.forEach(seat -> {
            if (seat.getStatus() != SeatStatus.LOCKED) {
                throw new BadRequestException("Seat " + seat.getSeatCode() + " is not in LOCKED state");
            }
            seat.setStatus(SeatStatus.BOOKED);
            seat.setBookingId(request.getBookingId());
            seat.setLockedBy(null);
            seat.setLockedAt(null);
            seat.setLockExpiresAt(null);
        });
        seatRepository.saveAll(seats);

        idempotencyRepository.save(AllocationIdempotency.builder()
                .eventId(request.getEventId())
                .idempotencyKey(request.getIdempotencyKey())
                .operation("CONFIRM")
                .build());

        log.info("Confirmed {} seats for booking {}", seats.size(), request.getBookingId());
        return seats.stream().map(this::mapToSeatResponse).collect(Collectors.toList());
    }

    // ---- Internal: Release seats on failure/cancellation ----
    @Transactional
    public List<SeatResponse> releaseSeats(ReleaseSeatsRequest request) {
        validateEventExists(request.getEventId());

        if (idempotencyRepository.existsByEventIdAndIdempotencyKey(
                request.getEventId(), request.getIdempotencyKey())) {
            log.info("Duplicate release request - idempotencyKey: {}", request.getIdempotencyKey());
            return seatRepository.findByIdInAndEventId(request.getSeatIds(), request.getEventId())
                    .stream().map(this::mapToSeatResponse).collect(Collectors.toList());
        }

        List<AllocationEventSeat> seats =
                seatRepository.findByIdInAndEventId(request.getSeatIds(), request.getEventId());

        seats.forEach(seat -> {
            seat.setStatus(SeatStatus.AVAILABLE);
            seat.setLockedBy(null);
            seat.setLockedAt(null);
            seat.setLockExpiresAt(null);
            seat.setBookingId(null);
        });
        seatRepository.saveAll(seats);

        idempotencyRepository.save(AllocationIdempotency.builder()
                .eventId(request.getEventId())
                .idempotencyKey(request.getIdempotencyKey())
                .operation("RELEASE")
                .build());

        log.info("Released {} seats for event {}", seats.size(), request.getEventId());
        return seats.stream().map(this::mapToSeatResponse).collect(Collectors.toList());
    }

    // ---- Scheduled: Release expired locks every minute ----
    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void releaseExpiredLocks() {
        int released = seatRepository.releaseExpiredLocks(OffsetDateTime.now());
        if (released > 0) {
            log.info("Released {} expired seat locks", released);
        }
    }

    private void validateEventExists(UUID eventId) {
        if (!contextRepository.existsByEventId(eventId)) {
            throw new ResourceNotFoundException("Event inventory not found: " + eventId);
        }
    }

    private LockSeatsResponse buildLockResponse(UUID eventId,
                                                 List<AllocationEventSeat> seats,
                                                 String idempotencyKey) {
        OffsetDateTime expiresAt = seats.stream()
                .filter(s -> s.getLockExpiresAt() != null)
                .map(AllocationEventSeat::getLockExpiresAt)
                .findFirst().orElse(OffsetDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));

        return LockSeatsResponse.builder()
                .eventId(eventId)
                .lockedSeats(seats.stream().map(this::mapToSeatResponse).collect(Collectors.toList()))
                .lockExpiresAt(expiresAt)
                .idempotencyKey(idempotencyKey)
                .build();
    }

    private SeatResponse mapToSeatResponse(AllocationEventSeat seat) {
        return SeatResponse.builder()
                .id(seat.getId())
                .eventId(seat.getEventId())
                .venueSeatId(seat.getVenueSeatId())
                .sectionId(seat.getSectionId())
                .seatCode(seat.getSeatCode())
                .rowLabel(seat.getRowLabel())
                .seatNumber(seat.getSeatNumber())
                .priceCents(seat.getPriceCents())
                .currency(seat.getCurrency())
                .status(seat.getStatus())
                .build();
    }
}
