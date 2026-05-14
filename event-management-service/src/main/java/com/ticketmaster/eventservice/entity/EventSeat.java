package com.ticketmaster.eventservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "event_seats",
       uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "venue_seat_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EventSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_seat_id", nullable = false)
    private VenueSeat venueSeat;
}
