package com.ticketmaster.eventservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ConfigurePricingRequest {
    @NotNull private UUID sectionId;
    @Min(0)  private int priceCents;
    private String currency;
}
