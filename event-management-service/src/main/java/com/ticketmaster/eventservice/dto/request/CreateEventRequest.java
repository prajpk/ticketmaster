package com.ticketmaster.eventservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class CreateEventRequest {
    @NotNull  private UUID venueId;
    @NotBlank private String title;
    private String description;
    private String category;
    @NotNull  private OffsetDateTime startsAt;
    @NotNull  private OffsetDateTime endsAt;
}
