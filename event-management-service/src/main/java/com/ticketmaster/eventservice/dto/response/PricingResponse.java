package com.ticketmaster.eventservice.dto.response;

import lombok.*;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PricingResponse {
    private UUID id;
    private UUID eventId;
    private UUID sectionId;
    private String sectionName;
    private Integer priceCents;
    private String currency;
}
