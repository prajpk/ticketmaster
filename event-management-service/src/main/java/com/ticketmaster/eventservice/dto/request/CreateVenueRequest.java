package com.ticketmaster.eventservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateVenueRequest {
    @NotBlank private String name;
    @NotBlank private String city;
    @NotBlank private String address;
    private String state;
    private String country;
    private Integer capacity;
}
