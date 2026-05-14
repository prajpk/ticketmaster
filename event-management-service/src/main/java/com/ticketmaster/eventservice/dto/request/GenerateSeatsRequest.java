package com.ticketmaster.eventservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class GenerateSeatsRequest {
    @NotEmpty
    private List<String> rows;

    @Min(1)
    private int seatsPerRow;
}
