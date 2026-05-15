package com.ticketmaster.allocationservice.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventInventoryMessage {
    private UUID eventId;
    private UUID venueId;
    private String eventTitle;
    private OffsetDateTime startsAt;
    private List<SeatInfo> seats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatInfo {
        private UUID venueSeatId;
        private UUID sectionId;
        private String seatCode;
        private String rowLabel;
        private Integer seatNumber;
        private Integer priceCents;
        private String currency;
    }
}
