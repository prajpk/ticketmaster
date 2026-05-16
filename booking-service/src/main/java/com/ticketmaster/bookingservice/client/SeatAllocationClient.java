package com.ticketmaster.bookingservice.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class SeatAllocationClient {

    private final RestTemplate restTemplate;

    @Value("${services.seats-allocation-url}")
    private String baseUrl;

    public Map lockSeats(UUID eventId, UUID userId, List<UUID> seatIds, String idempotencyKey) {
        String url = baseUrl + "/api/v1/internal/seats/" + eventId + "/locks";
        Map<String, Object> body = new HashMap<>();
        body.put("userId", userId.toString());
        body.put("seatIds", seatIds.stream().map(UUID::toString).toList());
        body.put("idempotencyKey", idempotencyKey);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("Seat lock failed - status:{} body:{}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Seat lock failed: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Seat lock error: {}", e.getMessage());
            throw new RuntimeException("Seat lock failed: " + e.getMessage());
        }
    }

    public void confirmSeats(UUID eventId, UUID bookingId, List<UUID> seatIds, String idempotencyKey) {
        String url = baseUrl + "/api/v1/internal/seats/confirm";
        Map<String, Object> body = new HashMap<>();
        body.put("eventId", eventId.toString());
        body.put("bookingId", bookingId.toString());
        body.put("seatIds", seatIds.stream().map(UUID::toString).toList());
        body.put("idempotencyKey", idempotencyKey);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(url, entity, Map.class);
        } catch (Exception e) {
            log.error("Confirm seats failed: {}", e.getMessage());
        }
    }

    public void releaseSeats(UUID eventId, List<UUID> seatIds, String idempotencyKey) {
        String url = baseUrl + "/api/v1/internal/seats/release";
        Map<String, Object> body = new HashMap<>();
        body.put("eventId", eventId.toString());
        body.put("seatIds", seatIds.stream().map(UUID::toString).toList());
        body.put("idempotencyKey", idempotencyKey);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(url, entity, Map.class);
        } catch (Exception e) {
            log.error("Release seats failed: {}", e.getMessage());
        }
    }
}