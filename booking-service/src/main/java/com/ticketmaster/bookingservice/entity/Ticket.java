package com.ticketmaster.bookingservice.entity;

import com.ticketmaster.bookingservice.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "tickets")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_item_id", nullable = false, unique = true)
    private BookingItem bookingItem;

    @Column(nullable = false, unique = true)
    private String ticketNumber;

    @Column(nullable = false, unique = true)
    private String ticketCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TicketStatus status = TicketStatus.ISSUED;

    @CreationTimestamp
    private OffsetDateTime createdAt;
}
