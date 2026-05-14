package com.ticketmaster.eventservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "event_section_pricing",
       uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "section_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EventSectionPricing {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private VenueSection section;

    @Column(nullable = false)
    private Integer priceCents;

    @Column(nullable = false)
    @Builder.Default
    private String currency = "INR";
}
