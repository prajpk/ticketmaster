package com.ticketmaster.allocationservice.controller;

import com.ticketmaster.allocationservice.dto.request.*;
import com.ticketmaster.allocationservice.dto.response.*;
import com.ticketmaster.allocationservice.service.SeatAllocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class SeatController {

    private final SeatAllocationService seatAllocationService;

    @GetMapping("/api/v1/events/{eventId}/seats")
    public ResponseEntity<List<SeatResponse>> getAvailableSeats(@PathVariable UUID eventId) {
        return ResponseEntity.ok(seatAllocationService.getAvailableSeats(eventId));
    }

    @GetMapping("/api/v1/events/{eventId}/seats/all")
    public ResponseEntity<List<SeatResponse>> getAllSeats(@PathVariable UUID eventId) {
        return ResponseEntity.ok(seatAllocationService.getAllSeats(eventId));
    }

    @PostMapping("/api/v1/internal/seats/{eventId}/locks")
    public ResponseEntity<LockSeatsResponse> lockSeats(
            @PathVariable UUID eventId,
            @Valid @RequestBody LockSeatsRequest request) {
        // Force eventId from path variable — ignore what's in body
        request.setEventId(eventId);
        return ResponseEntity.ok(seatAllocationService.lockSeats(request));
    }

    @PostMapping("/api/v1/internal/seats/confirm")
    public ResponseEntity<List<SeatResponse>> confirmSeats(
            @Valid @RequestBody ConfirmSeatsRequest request) {
        return ResponseEntity.ok(seatAllocationService.confirmSeats(request));
    }

    @PostMapping("/api/v1/internal/seats/release")
    public ResponseEntity<List<SeatResponse>> releaseSeats(
            @Valid @RequestBody ReleaseSeatsRequest request) {
        return ResponseEntity.ok(seatAllocationService.releaseSeats(request));
    }
}