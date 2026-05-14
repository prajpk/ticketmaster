package com.ticketmaster.eventservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "venue_seats",
       uniqueConstraints = @UniqueConstraint(columnNames = {"venue_id", "seat_code"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VenueSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private VenueSection section;

    @Column(name = "seat_code", nullable = false)
    private String seatCode;

    @Column(nullable = false)
    private String rowLabel;

    @Column(nullable = false)
    private Integer seatNumber;
}
