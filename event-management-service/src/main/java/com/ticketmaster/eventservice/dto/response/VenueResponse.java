package com.ticketmaster.eventservice.dto.response;

import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class VenueResponse {
    private UUID id;
    private String name;
    private String city;
    private String address;
    private String state;
    private String country;
    private Integer capacity;
    private OffsetDateTime createdAt;
}
