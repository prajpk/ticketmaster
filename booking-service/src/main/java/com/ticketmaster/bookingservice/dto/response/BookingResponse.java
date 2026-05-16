package com.ticketmaster.bookingservice.dto.response;

import com.ticketmaster.bookingservice.enums.BookingStatus;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BookingResponse {
    private UUID id;
    private UUID userId;
    private UUID eventId;
    private UUID paymentId;
    private BookingStatus status;
    private Long totalAmountMinor;
    private String currency;
    private List<BookingItemResponse> items;
    private OffsetDateTime createdAt;
}
