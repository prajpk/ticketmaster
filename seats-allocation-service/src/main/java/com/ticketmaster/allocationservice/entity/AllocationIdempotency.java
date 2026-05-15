package com.ticketmaster.allocationservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "allocation_idempotency",
       uniqueConstraints = @UniqueConstraint(columnNames = {"eventId", "idempotencyKey"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AllocationIdempotency {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID eventId;

    @Column(nullable = false)
    private String idempotencyKey;

    @Column(nullable = false)
    private String operation; // LOCK, CONFIRM, RELEASE

    @CreationTimestamp
    private OffsetDateTime createdAt;
}
