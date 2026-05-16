package com.ticketmaster.paymentservice.dto.request;

import lombok.Data;

@Data
public class WebhookRequest {
    private String event;
    private String providerEventId;
    private String providerPaymentId;
    private String providerOrderId;
    private String status;
}
