package com.ticketmaster.bookingservice.entity;

import com.ticketmaster.bookingservice.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "bookings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(unique = true)
    private UUID paymentId;

    @Column(nullable = false)
    private UUID eventId;

    private UUID lockId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

    @Column(nullable = false)
    private Long totalAmountMinor;

    @Column(nullable = false)
    @Builder.Default
    private String currency = "INR";

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<BookingItem> items = new ArrayList<>();

    @CreationTimestamp
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    private OffsetDateTime updatedAt;
}
