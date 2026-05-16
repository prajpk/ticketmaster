package com.ticketmaster.bookingservice.dto.response;

import lombok.*;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CheckoutResponse {
    private UUID bookingId;
    private UUID paymentId;
    private String providerOrderId;
    private Long totalAmountMinor;
    private String currency;
    private String status;
}
