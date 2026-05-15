package com.ticketmaster.allocationservice.dto.response;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class LockSeatsResponse {
    private UUID eventId;
    private List<SeatResponse> lockedSeats;
    private OffsetDateTime lockExpiresAt;
    private String idempotencyKey;
}
