package com.ticketmaster.eventservice.service;

import com.ticketmaster.eventservice.dto.request.*;
import com.ticketmaster.eventservice.dto.response.*;
import com.ticketmaster.eventservice.entity.*;
import com.ticketmaster.eventservice.enums.EventStatus;
import com.ticketmaster.eventservice.exception.*;
import com.ticketmaster.eventservice.kafka.*;
import com.ticketmaster.eventservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;
    private final VenueSeatRepository venueSeatRepository;
    private final VenueSectionRepository venueSectionRepository;
    private final EventSectionPricingRepository pricingRepository;
    private final EventSeatRepository eventSeatRepository;
    private final EventInventoryProducer inventoryProducer;

    @Transactional
    public EventResponse createEvent(UUID organiserId, String organiserEmail, CreateEventRequest request) {
        Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found: " + request.getVenueId()));

        Event event = Event.builder()
                .organiserId(organiserId)
                .organiserEmail(organiserEmail)
                .venue(venue)
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .startsAt(request.getStartsAt())
                .endsAt(request.getEndsAt())
                .build();

        event = eventRepository.save(event);
        log.info("Created event: {}", event.getId());
        return mapToEventResponse(event);
    }

    @Transactional
    public List<PricingResponse> configurePricing(UUID eventId, UUID organiserId,
                                                   List<ConfigurePricingRequest> requests) {
        Event event = getOwnedEvent(eventId, organiserId);

        List<EventSectionPricing> pricingList = new ArrayList<>();
        for (ConfigurePricingRequest req : requests) {
            VenueSection section = venueSectionRepository.findById(req.getSectionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Section not found: " + req.getSectionId()));

            EventSectionPricing pricing = pricingRepository
                    .findByEventIdAndSectionId(eventId, req.getSectionId())
                    .orElse(EventSectionPricing.builder().event(event).section(section).build());

            pricing.setPriceCents(req.getPriceCents());
            pricing.setCurrency(req.getCurrency() != null ? req.getCurrency() : "INR");
            pricingList.add(pricing);
        }

        pricingList = pricingRepository.saveAll(pricingList);
        return pricingList.stream().map(this::mapToPricingResponse).collect(Collectors.toList());
    }

    @Transactional
    public EventResponse initializeInventory(UUID eventId, UUID organiserId) {
        Event event = getOwnedEvent(eventId, organiserId);

        if (event.getStatus() != EventStatus.DRAFT) {
            throw new BadRequestException("Inventory can only be initialized for DRAFT events");
        }

        List<EventSectionPricing> pricingList = pricingRepository.findByEventId(eventId);
        if (pricingList.isEmpty()) {
            throw new BadRequestException("Configure pricing before initializing inventory");
        }

        // Create event seats from venue seats
        List<VenueSeat> venueSeats = venueSeatRepository.findByVenueId(event.getVenue().getId());
        if (venueSeats.isEmpty()) {
            throw new BadRequestException("No venue seats found to initialize inventory");
        }

        List<EventSeat> eventSeats = venueSeats.stream()
                .map(vs -> EventSeat.builder().event(event).venueSeat(vs).build())
                .collect(Collectors.toList());
        eventSeatRepository.saveAll(eventSeats);

        log.info("Initialized inventory for event {} with {} seats", eventId, eventSeats.size());
        return mapToEventResponse(event);
    }

    @Transactional
    public EventResponse publishEvent(UUID eventId, UUID organiserId) {
        Event event = getOwnedEvent(eventId, organiserId);

        if (event.getStatus() != EventStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT events can be published");
        }

        long seatCount = eventSeatRepository.countByEventId(eventId);
        if (seatCount == 0) {
            throw new BadRequestException("Initialize inventory before publishing");
        }

        event.setStatus(EventStatus.PUBLISHED);
        event = eventRepository.save(event);

        // Build and publish Kafka message
        List<EventSeat> seats = eventSeatRepository.findByEventId(eventId);
        List<EventSectionPricing> pricing = pricingRepository.findByEventId(eventId);
        Map<UUID, Integer> sectionPriceMap = pricing.stream()
                .collect(Collectors.toMap(p -> p.getSection().getId(), EventSectionPricing::getPriceCents));

        List<EventInventoryMessage.SeatInfo> seatInfos = seats.stream().map(es -> {
            VenueSeat vs = es.getVenueSeat();
            return EventInventoryMessage.SeatInfo.builder()
                    .venueSeatId(vs.getId())
                    .sectionId(vs.getSection().getId())
                    .seatCode(vs.getSeatCode())
                    .rowLabel(vs.getRowLabel())
                    .seatNumber(vs.getSeatNumber())
                    .priceCents(sectionPriceMap.getOrDefault(vs.getSection().getId(), 0))
                    .currency("INR")
                    .build();
        }).collect(Collectors.toList());

        EventInventoryMessage message = EventInventoryMessage.builder()
                .eventId(event.getId())
                .venueId(event.getVenue().getId())
                .eventTitle(event.getTitle())
                .startsAt(event.getStartsAt())
                .seats(seatInfos)
                .build();

        inventoryProducer.publishInventory(message);
        log.info("Published event {} with {} seats to Kafka", eventId, seatInfos.size());
        return mapToEventResponse(event);
    }

    public Page<EventResponse> listPublishedEvents(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("startsAt").ascending());
        return eventRepository.findByStatus(EventStatus.PUBLISHED, pageable)
                .map(this::mapToEventResponse);
    }

    public EventResponse getEvent(UUID eventId) {
        return eventRepository.findById(eventId)
                .map(this::mapToEventResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));
    }

    private Event getOwnedEvent(UUID eventId, UUID organiserId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));
        if (!event.getOrganiserId().equals(organiserId)) {
            throw new ForbiddenException("You do not own this event");
        }
        return event;
    }

    private EventResponse mapToEventResponse(Event e) {
        return EventResponse.builder()
                .id(e.getId()).organiserId(e.getOrganiserId())
                .venueId(e.getVenue().getId()).venueName(e.getVenue().getName())
                .title(e.getTitle()).description(e.getDescription())
                .category(e.getCategory()).status(e.getStatus())
                .startsAt(e.getStartsAt()).endsAt(e.getEndsAt())
                .createdAt(e.getCreatedAt()).build();
    }

    private PricingResponse mapToPricingResponse(EventSectionPricing p) {
        return PricingResponse.builder()
                .id(p.getId()).eventId(p.getEvent().getId())
                .sectionId(p.getSection().getId()).sectionName(p.getSection().getName())
                .priceCents(p.getPriceCents()).currency(p.getCurrency()).build();
    }
}
