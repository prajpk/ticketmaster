package com.ticketmaster.bookingservice.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentClient {

    private final RestTemplate restTemplate;

    @Value("${services.payment-url}")
    private String baseUrl;

    public Map<String, Object> initiatePayment(UUID userId, UUID bookingId, UUID eventId,
                                               long amountMinor, String currency,
                                               String idempotencyKey) {
        String url = baseUrl + "/api/v1/internal/payments/initiate";

        Map<String, Object> body = new HashMap<>();
        body.put("userId", userId);
        body.put("bookingId", bookingId);
        body.put("eventId", eventId);
        body.put("amountMinor", amountMinor);
        body.put("currency", currency);
        body.put("idempotencyKey", idempotencyKey);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<>() {});

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalStateException("Payment initiation failed");
        }
        return response.getBody();
    }
}
