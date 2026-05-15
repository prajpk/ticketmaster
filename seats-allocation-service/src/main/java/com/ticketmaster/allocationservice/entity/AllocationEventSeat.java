package com.ticketmaster.allocationservice.entity;

import com.ticketmaster.allocationservice.enums.SeatStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "event_seats",
       uniqueConstraints = @UniqueConstraint(columnNames = {"eventId", "venueSeatId"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AllocationEventSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID eventId;

    @Column(nullable = false)
    private UUID venueSeatId;

    @Column(nullable = false)
    private UUID sectionId;

    @Column(nullable = false)
    private String seatCode;

    @Column(nullable = false)
    private String rowLabel;

    @Column(nullable = false)
    private Integer seatNumber;

    @Column(nullable = false)
    private Integer priceCents;

    @Column(nullable = false)
    @Builder.Default
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SeatStatus status = SeatStatus.AVAILABLE;

    // Set when LOCKED
    private UUID lockedBy;
    private OffsetDateTime lockedAt;
    private OffsetDateTime lockExpiresAt;

    // Set when BOOKED
    private UUID bookingId;

    @CreationTimestamp
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    private OffsetDateTime updatedAt;
}
