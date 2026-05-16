package com.ticketmaster.paymentservice.service;

import com.ticketmaster.paymentservice.client.BookingClient;
import com.ticketmaster.paymentservice.dto.request.*;
import com.ticketmaster.paymentservice.dto.response.PaymentResponse;
import com.ticketmaster.paymentservice.entity.*;
import com.ticketmaster.paymentservice.enums.PaymentStatus;
import com.ticketmaster.paymentservice.exception.*;
import com.ticketmaster.paymentservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentIdempotencyRepository idempotencyRepository;
    private final ProcessedWebhookRepository webhookRepository;
    private final BookingClient bookingClient;

    // ---- Initiate payment - called by booking-service ----
    @Transactional
    public PaymentResponse initiatePayment(InitiatePaymentRequest request) {
        log.info("Initiating payment - bookingId: {}, amount: {}",
                request.getBookingId(), request.getAmountMinor());

        // Idempotency check
        var existing = idempotencyRepository
                .findByUserIdAndIdempotencyKey(request.getUserId(), request.getIdempotencyKey());

        if (existing.isPresent()) {
            log.info("Duplicate payment initiation - returning existing paymentId: {}",
                    existing.get().getPaymentId());
            Payment payment = paymentRepository.findById(existing.get().getPaymentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
            return mapToResponse(payment);
        }

        // Create payment record
        Payment payment = Payment.builder()
                .userId(request.getUserId())
                .bookingId(request.getBookingId())
                .eventId(request.getEventId())
                .amountMinor(request.getAmountMinor())
                .currency(request.getCurrency() != null ? request.getCurrency() : "INR")
                .status(PaymentStatus.INITIATED)
                .build();

        // Simulate Razorpay provider order creation
        String providerOrderId = "order_" + UUID.randomUUID().toString()
                .replace("-", "").substring(0, 16);
        payment.setProviderOrderId(providerOrderId);
        payment.setStatus(PaymentStatus.PENDING);
        payment = paymentRepository.save(payment);

        // Save idempotency record
        idempotencyRepository.save(PaymentIdempotency.builder()
                .userId(request.getUserId())
                .idempotencyKey(request.getIdempotencyKey())
                .paymentId(payment.getId())
                .build());

        log.info("Payment created - paymentId: {}, providerOrderId: {}",
                payment.getId(), providerOrderId);
        return mapToResponse(payment);
    }

    // ---- Process webhook from Razorpay ----
    @Transactional
    public void processWebhook(WebhookRequest request) {
        log.info("Processing webhook - event: {}, providerEventId: {}",
                request.getEvent(), request.getProviderEventId());

        // Deduplication
        if (webhookRepository.existsByProviderAndProviderEventId(
                "RAZORPAY", request.getProviderEventId())) {
            log.warn("Duplicate webhook - providerEventId: {}", request.getProviderEventId());
            return;
        }

        Payment payment = paymentRepository.findByProviderOrderId(request.getProviderOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found for order: " + request.getProviderOrderId()));

        // Update payment status
        boolean isSuccess = "payment.captured".equals(request.getEvent())
                || "SUCCESS".equalsIgnoreCase(request.getStatus());

        payment.setStatus(isSuccess ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
        if (request.getProviderPaymentId() != null) {
            payment.setProviderPaymentId(request.getProviderPaymentId());
        }
        paymentRepository.save(payment);

        // Save processed webhook
        webhookRepository.save(ProcessedWebhook.builder()
                .provider("RAZORPAY")
                .providerEventId(request.getProviderEventId())
                .eventType(request.getEvent())
                .build());

        // Callback to booking-service
        bookingClient.finalizeBooking(
                payment.getId(),
                payment.getBookingId(),
                isSuccess ? "SUCCESS" : "FAILED");

        log.info("Webhook processed - paymentId: {}, status: {}",
                payment.getId(), payment.getStatus());
    }

    public PaymentResponse getPayment(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + paymentId));
        return mapToResponse(payment);
    }

    private PaymentResponse mapToResponse(Payment p) {
        return PaymentResponse.builder()
                .paymentId(p.getId())
                .bookingId(p.getBookingId())
                .amountMinor(p.getAmountMinor())
                .currency(p.getCurrency())
                .provider(p.getProvider())
                .providerOrderId(p.getProviderOrderId())
                .status(p.getStatus())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
