package com.ticketmaster.eventservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateVenueSectionRequest {
    @NotBlank private String name;
    private Integer sortOrder;
}
