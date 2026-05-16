package com.ticketmaster.bookingservice.dto.response;

import com.ticketmaster.bookingservice.enums.TicketStatus;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TicketResponse {
    private UUID id;
    private UUID bookingId;
    private UUID bookingItemId;
    private String seatCode;
    private String ticketNumber;
    private String ticketCode;
    private TicketStatus status;
    private OffsetDateTime createdAt;
}
