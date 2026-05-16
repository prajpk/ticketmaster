package com.ticketmaster.bookingservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class FinalizeBookingRequest {

    @NotNull
    private UUID paymentId;

    @NotNull
    private UUID bookingId;

    @NotNull
    private String paymentStatus;
}
