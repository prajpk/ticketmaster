package com.ticketmaster.allocationservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ticketmaster.allocationservice.entity.AllocationEventSeat;
import com.ticketmaster.allocationservice.entity.EventInventoryContext;
import com.ticketmaster.allocationservice.repository.AllocationEventSeatRepository;
import com.ticketmaster.allocationservice.repository.EventInventoryContextRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventInventoryConsumer {

    private final EventInventoryContextRepository contextRepository;
    private final AllocationEventSeatRepository seatRepository;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @KafkaListener(topics = "${kafka.topics.event-inventory}",
                   groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void consumeEventInventory(String messageJson) {
        try {
            EventInventoryMessage message = objectMapper.readValue(
                    messageJson, EventInventoryMessage.class);

            log.info("Received inventory for event: {} with {} seats",
                    message.getEventId(), message.getSeats().size());

            if (contextRepository.existsByEventId(message.getEventId())) {
                log.warn("Inventory already exists for event: {} - skipping", message.getEventId());
                return;
            }

            EventInventoryContext context = EventInventoryContext.builder()
                    .eventId(message.getEventId())
                    .venueId(message.getVenueId())
                    .eventTitle(message.getEventTitle())
                    .startsAt(message.getStartsAt())
                    .build();
            contextRepository.save(context);

            List<AllocationEventSeat> seats = message.getSeats().stream()
                    .map(seatInfo -> AllocationEventSeat.builder()
                            .eventId(message.getEventId())
                            .venueSeatId(seatInfo.getVenueSeatId())
                            .sectionId(seatInfo.getSectionId())
                            .seatCode(seatInfo.getSeatCode())
                            .rowLabel(seatInfo.getRowLabel())
                            .seatNumber(seatInfo.getSeatNumber())
                            .priceCents(seatInfo.getPriceCents())
                            .currency(seatInfo.getCurrency() != null ? seatInfo.getCurrency() : "INR")
                            .build())
                    .collect(Collectors.toList());

            seatRepository.saveAll(seats);
            log.info("Created {} seats for event: {}", seats.size(), message.getEventId());

        } catch (Exception e) {
            log.error("Failed to process inventory message: {}", e.getMessage(), e);
        }
    }
}