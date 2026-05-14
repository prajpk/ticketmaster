package com.ticketmaster.eventservice.dto.response;

import com.ticketmaster.eventservice.enums.EventStatus;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class EventResponse {
    private UUID id;
    private UUID organiserId;
    private UUID venueId;
    private String venueName;
    private String title;
    private String description;
    private String category;
    private EventStatus status;
    private OffsetDateTime startsAt;
    private OffsetDateTime endsAt;
    private OffsetDateTime createdAt;
}
