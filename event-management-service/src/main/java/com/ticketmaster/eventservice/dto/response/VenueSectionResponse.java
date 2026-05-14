package com.ticketmaster.eventservice.dto.response;

import lombok.*;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class VenueSectionResponse {
    private UUID id;
    private UUID venueId;
    private String name;
    private Integer sortOrder;
}
