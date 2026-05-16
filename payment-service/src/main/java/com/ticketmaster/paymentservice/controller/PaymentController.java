package com.ticketmaster.paymentservice.controller;

import com.ticketmaster.paymentservice.dto.request.*;
import com.ticketmaster.paymentservice.dto.response.PaymentResponse;
import com.ticketmaster.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // Internal: called by booking-service during checkout
    @PostMapping("/internal/payments/initiate")
    public ResponseEntity<PaymentResponse> initiatePayment(
            @Valid @RequestBody InitiatePaymentRequest request) {
        return ResponseEntity.ok(paymentService.initiatePayment(request));
    }

    // Webhook: called by Razorpay (or stub) after payment captured
    @PostMapping("/internal/payments/webhook")
    public ResponseEntity<Void> processWebhook(@RequestBody WebhookRequest request) {
        paymentService.processWebhook(request);
        return ResponseEntity.ok().build();
    }

    // Get payment details
    @GetMapping("/payments/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable UUID paymentId) {
        return ResponseEntity.ok(paymentService.getPayment(paymentId));
    }
}
