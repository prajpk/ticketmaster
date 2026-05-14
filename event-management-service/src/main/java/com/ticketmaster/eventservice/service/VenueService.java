package com.ticketmaster.eventservice.service;

import com.ticketmaster.eventservice.dto.request.*;
import com.ticketmaster.eventservice.dto.response.*;
import com.ticketmaster.eventservice.entity.*;
import com.ticketmaster.eventservice.exception.*;
import com.ticketmaster.eventservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VenueService {

    private final VenueRepository venueRepository;
    private final VenueSectionRepository venueSectionRepository;
    private final VenueSeatRepository venueSeatRepository;

    @Transactional
    public VenueResponse createVenue(CreateVenueRequest request) {
        if (venueRepository.existsByNameAndCity(request.getName(), request.getCity())) {
            throw new ConflictException("Venue already exists: " + request.getName() + " in " + request.getCity());
        }
        Venue venue = Venue.builder()
                .name(request.getName())
                .city(request.getCity())
                .address(request.getAddress())
                .state(request.getState())
                .country(request.getCountry())
                .capacity(request.getCapacity())
                .build();
        venue = venueRepository.save(venue);
        log.info("Created venue: {}", venue.getId());
        return mapToVenueResponse(venue);
    }

    @Transactional
    public VenueSectionResponse createSection(UUID venueId, CreateVenueSectionRequest request) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found: " + venueId));
        if (venueSectionRepository.existsByVenueIdAndName(venueId, request.getName())) {
            throw new ConflictException("Section already exists: " + request.getName());
        }
        VenueSection section = VenueSection.builder()
                .venue(venue)
                .name(request.getName())
                .sortOrder(request.getSortOrder())
                .build();
        section = venueSectionRepository.save(section);
        log.info("Created section {} in venue {}", section.getId(), venueId);
        return mapToSectionResponse(section);
    }

    @Transactional
    public List<VenueSeatResponse> generateSeats(UUID venueId, UUID sectionId, GenerateSeatsRequest request) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found: " + venueId));
        VenueSection section = venueSectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found: " + sectionId));

        List<VenueSeat> seats = new ArrayList<>();
        for (String row : request.getRows()) {
            for (int i = 1; i <= request.getSeatsPerRow(); i++) {
                String seatCode = row + i;
                if (!venueSeatRepository.existsByVenueIdAndSeatCode(venueId, seatCode)) {
                    seats.add(VenueSeat.builder()
                            .venue(venue)
                            .section(section)
                            .seatCode(seatCode)
                            .rowLabel(row)
                            .seatNumber(i)
                            .build());
                }
            }
        }
        seats = venueSeatRepository.saveAll(seats);
        log.info("Generated {} seats for section {} in venue {}", seats.size(), sectionId, venueId);
        return seats.stream().map(this::mapToSeatResponse).collect(Collectors.toList());
    }

    public VenueResponse getVenue(UUID venueId) {
        return venueRepository.findById(venueId)
                .map(this::mapToVenueResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found: " + venueId));
    }

    public List<VenueResponse> listVenues() {
        return venueRepository.findAll().stream()
                .map(this::mapToVenueResponse)
                .collect(Collectors.toList());
    }

	public List<VenueSectionResponse> getSectionsByVenue(UUID venueId) {
		if (!venueRepository.existsById(venueId)) {
			throw new ResourceNotFoundException("Venue not found: " + venueId);
		}
		return venueSectionRepository.findByVenueId(venueId)
				.stream()
				.map(this::mapToSectionResponse)
				.collect(Collectors.toList());
	}
	
    // --- Mappers ---
    private VenueResponse mapToVenueResponse(Venue v) {
        return VenueResponse.builder()
                .id(v.getId()).name(v.getName()).city(v.getCity())
                .address(v.getAddress()).state(v.getState())
                .country(v.getCountry()).capacity(v.getCapacity())
                .createdAt(v.getCreatedAt()).build();
    }

    private VenueSectionResponse mapToSectionResponse(VenueSection s) {
        return VenueSectionResponse.builder()
                .id(s.getId()).venueId(s.getVenue().getId())
                .name(s.getName()).sortOrder(s.getSortOrder()).build();
    }

    private VenueSeatResponse mapToSeatResponse(VenueSeat s) {
        return VenueSeatResponse.builder()
                .id(s.getId()).venueId(s.getVenue().getId())
                .sectionId(s.getSection().getId()).seatCode(s.getSeatCode())
                .rowLabel(s.getRowLabel()).seatNumber(s.getSeatNumber()).build();
    }
}
