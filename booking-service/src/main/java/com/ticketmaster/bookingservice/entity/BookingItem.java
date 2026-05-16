package com.ticketmaster.bookingservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "booking_items",
       uniqueConstraints = @UniqueConstraint(columnNames = {"booking_id", "eventSeatId"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BookingItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(nullable = false)
    private UUID eventSeatId;

    @Column(nullable = false)
    private UUID sectionId;

    @Column(nullable = false)
    private String seatCode;

    @Column(nullable = false)
    private Long priceMinor;

    @OneToOne(mappedBy = "bookingItem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Ticket ticket;
}
