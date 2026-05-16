package com.ticketmaster.bookingservice.controller;

import com.ticketmaster.bookingservice.dto.request.*;
import com.ticketmaster.bookingservice.dto.response.*;
import com.ticketmaster.bookingservice.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/bookings/checkout")
    public ResponseEntity<CheckoutResponse> checkout(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody CheckoutRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.checkout(userId, request));
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<BookingResponse>> getMyBookings(
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(bookingService.getUserBookings(userId));
    }

    @GetMapping("/bookings/{bookingId}/tickets")
    public ResponseEntity<List<TicketResponse>> getTickets(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID bookingId) {
        return ResponseEntity.ok(bookingService.getTickets(bookingId, userId));
    }

    @PostMapping("/tickets/scan/{ticketCode}")
    public ResponseEntity<TicketResponse> scanTicket(@PathVariable String ticketCode) {
        return ResponseEntity.ok(bookingService.scanTicket(ticketCode));
    }

    @PostMapping("/internal/bookings/finalize")
    public ResponseEntity<Void> finalizeBooking(
            @Valid @RequestBody FinalizeBookingRequest request) {
        bookingService.finalizeBooking(request);
        return ResponseEntity.ok().build();
    }
}