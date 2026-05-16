package com.ticketmaster.paymentservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class InitiatePaymentRequest {

    @NotNull private UUID userId;
    @NotNull private UUID bookingId;
    @NotNull private UUID eventId;
    @NotNull private Long amountMinor;
    private String currency = "INR";
    @NotNull private String idempotencyKey;
}
