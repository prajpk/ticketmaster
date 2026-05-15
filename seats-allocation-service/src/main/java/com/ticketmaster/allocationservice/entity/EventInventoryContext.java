package com.ticketmaster.allocationservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "event_inventory_context",
       uniqueConstraints = @UniqueConstraint(columnNames = "eventId"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EventInventoryContext {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID eventId;

    @Column(nullable = false)
    private UUID venueId;

    @Column(nullable = false)
    private String eventTitle;

    @Column(nullable = false)
    private OffsetDateTime startsAt;

    @Builder.Default
    private String status = "ACTIVE";

    @CreationTimestamp
    private OffsetDateTime createdAt;
}
