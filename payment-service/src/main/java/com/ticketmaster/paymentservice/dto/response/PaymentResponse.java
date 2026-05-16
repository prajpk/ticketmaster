package com.ticketmaster.paymentservice.dto.response;

import com.ticketmaster.paymentservice.enums.PaymentStatus;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PaymentResponse {
    private UUID paymentId;
    private UUID bookingId;
    private Long amountMinor;
    private String currency;
    private String provider;
    private String providerOrderId;
    private PaymentStatus status;
    private OffsetDateTime createdAt;
}
