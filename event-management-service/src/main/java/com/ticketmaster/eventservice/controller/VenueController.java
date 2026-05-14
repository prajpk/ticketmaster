package com.ticketmaster.eventservice.controller;

import com.ticketmaster.eventservice.dto.request.*;
import com.ticketmaster.eventservice.dto.response.*;
import com.ticketmaster.eventservice.service.VenueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/venues")
@RequiredArgsConstructor
public class VenueController {

    private final VenueService venueService;

    @PostMapping
    public ResponseEntity<VenueResponse> createVenue(@Valid @RequestBody CreateVenueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(venueService.createVenue(request));
    }

    @GetMapping
    public ResponseEntity<List<VenueResponse>> listVenues() {
        return ResponseEntity.ok(venueService.listVenues());
    }

    @GetMapping("/{venueId}")
    public ResponseEntity<VenueResponse> getVenue(@PathVariable UUID venueId) {
        return ResponseEntity.ok(venueService.getVenue(venueId));
    }

    @PostMapping("/{venueId}/sections")
    public ResponseEntity<VenueSectionResponse> createSection(
            @PathVariable UUID venueId,
            @Valid @RequestBody CreateVenueSectionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(venueService.createSection(venueId, request));
    }

	@GetMapping("/{venueId}/sections")
	public ResponseEntity<List<VenueSectionResponse>> getSections(@PathVariable UUID venueId) {
		return ResponseEntity.ok(venueService.getSectionsByVenue(venueId));
	}
	
    @PostMapping("/{venueId}/sections/{sectionId}/seats/generate")
    public ResponseEntity<List<VenueSeatResponse>> generateSeats(
            @PathVariable UUID venueId,
            @PathVariable UUID sectionId,
            @Valid @RequestBody GenerateSeatsRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(venueService.generateSeats(venueId, sectionId, request));
    }
}
