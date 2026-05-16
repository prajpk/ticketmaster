package com.ticketmaster.paymentservice.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingClient {

    private final RestTemplate restTemplate;

    @Value("${services.booking-url}")
    private String baseUrl;

    public void finalizeBooking(UUID paymentId, UUID bookingId, String paymentStatus) {
        String url = baseUrl + "/api/v1/internal/bookings/finalize";

        Map<String, Object> body = new HashMap<>();
        body.put("paymentId", paymentId);
        body.put("bookingId", bookingId);
        body.put("paymentStatus", paymentStatus);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Void> response =
                restTemplate.postForEntity(url, entity, Void.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException(
                    "Booking finalize failed with status: " + response.getStatusCode());
        }
        log.info("Booking finalized - bookingId: {}, status: {}", bookingId, paymentStatus);
    }
}
