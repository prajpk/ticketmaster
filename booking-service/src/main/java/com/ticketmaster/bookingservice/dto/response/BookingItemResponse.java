package com.ticketmaster.bookingservice.dto.response;

import lombok.*;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BookingItemResponse {
    private UUID id;
    private UUID eventSeatId;
    private UUID sectionId;
    private String seatCode;
    private Long priceMinor;
}
