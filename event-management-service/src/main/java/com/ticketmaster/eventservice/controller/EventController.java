package com.ticketmaster.eventservice.controller;

import com.ticketmaster.eventservice.dto.request.*;
import com.ticketmaster.eventservice.dto.response.*;
import com.ticketmaster.eventservice.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    // ---- Organiser APIs ----
    @PostMapping("/api/v1/organiser/events")
    public ResponseEntity<EventResponse> createEvent(
            Principal principal,
            @Valid @RequestBody CreateEventRequest request) {
        // In production, extract UUID from JWT claims; using random for demo
        UUID organiserId = UUID.nameUUIDFromBytes(principal.getName().getBytes());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventService.createEvent(organiserId, principal.getName(), request));
    }

    @PostMapping("/api/v1/organiser/events/{eventId}/pricing")
    public ResponseEntity<List<PricingResponse>> configurePricing(
            Principal principal,
            @PathVariable UUID eventId,
            @Valid @RequestBody List<ConfigurePricingRequest> requests) {
        UUID organiserId = UUID.nameUUIDFromBytes(principal.getName().getBytes());
        return ResponseEntity.ok(eventService.configurePricing(eventId, organiserId, requests));
    }

    @PostMapping("/api/v1/organiser/events/{eventId}/inventory/init")
    public ResponseEntity<EventResponse> initInventory(
            Principal principal,
            @PathVariable UUID eventId) {
        UUID organiserId = UUID.nameUUIDFromBytes(principal.getName().getBytes());
        return ResponseEntity.ok(eventService.initializeInventory(eventId, organiserId));
    }

    @PostMapping("/api/v1/organiser/events/{eventId}/publish")
    public ResponseEntity<EventResponse> publishEvent(
            Principal principal,
            @PathVariable UUID eventId) {
        UUID organiserId = UUID.nameUUIDFromBytes(principal.getName().getBytes());
        return ResponseEntity.ok(eventService.publishEvent(eventId, organiserId));
    }

    // ---- Public APIs ----
    @GetMapping("/api/v1/events/public")
    public ResponseEntity<Page<EventResponse>> listPublishedEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(eventService.listPublishedEvents(page, size));
    }

    @GetMapping("/api/v1/events/public/{eventId}")
    public ResponseEntity<EventResponse> getEvent(@PathVariable UUID eventId) {
        return ResponseEntity.ok(eventService.getEvent(eventId));
    }
}
