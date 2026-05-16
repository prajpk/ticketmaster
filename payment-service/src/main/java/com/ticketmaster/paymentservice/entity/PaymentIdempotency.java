package com.ticketmaster.paymentservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment_idempotency",
       uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "idempotencyKey"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentIdempotency {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String idempotencyKey;

    @Column(nullable = false)
    private UUID paymentId;

    @CreationTimestamp
    private OffsetDateTime createdAt;
}
