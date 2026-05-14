package com.ticketmaster.eventservice.dto.response;

import lombok.*;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class VenueSeatResponse {
    private UUID id;
    private UUID venueId;
    private UUID sectionId;
    private String seatCode;
    private String rowLabel;
    private Integer seatNumber;
}
