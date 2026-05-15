package com.ticketmaster.allocationservice.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ConfirmSeatsRequest {

    @NotNull(message = "bookingId is required")
    private UUID bookingId;

    @NotNull(message = "eventId is required")
    private UUID eventId;

    @NotEmpty(message = "seatIds must not be empty")
    private List<UUID> seatIds;

    @NotNull(message = "idempotencyKey is required")
    private String idempotencyKey;
}
