package com.ticketmaster.allocationservice.dto.response;

import com.ticketmaster.allocationservice.enums.SeatStatus;
import lombok.*;

import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SeatResponse {
    private UUID id;
    private UUID eventId;
    private UUID venueSeatId;
    private UUID sectionId;
    private String seatCode;
    private String rowLabel;
    private Integer seatNumber;
    private Integer priceCents;
    private String currency;
    private SeatStatus status;
}
