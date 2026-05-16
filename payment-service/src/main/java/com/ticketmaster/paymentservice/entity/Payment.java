package com.ticketmaster.paymentservice.entity;

import com.ticketmaster.paymentservice.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private UUID bookingId;

    @Column(nullable = false)
    private UUID eventId;

    @Column(nullable = false)
    private Long amountMinor;

    @Column(nullable = false)
    @Builder.Default
    private String currency = "INR";

    @Column(nullable = false)
    @Builder.Default
    private String provider = "RAZORPAY";

    private String providerOrderId;
    private String providerPaymentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.INITIATED;

    @CreationTimestamp
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    private OffsetDateTime updatedAt;
}
