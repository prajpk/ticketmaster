package com.ticketmaster.bookingservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "booking_fulfillment_requests")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BookingFulfillmentRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID paymentId;

    @Column(nullable = false)
    private UUID bookingId;

    @Column(nullable = false)
    @Builder.Default
    private String status = "PENDING";

    @CreationTimestamp
    private OffsetDateTime createdAt;
}
