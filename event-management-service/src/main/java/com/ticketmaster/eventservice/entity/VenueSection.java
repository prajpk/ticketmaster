package com.ticketmaster.eventservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "venue_sections",
       uniqueConstraints = @UniqueConstraint(columnNames = {"venue_id", "name"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VenueSection {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @Column(nullable = false)
    private String name;

    private Integer sortOrder;

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<VenueSeat> seats = new ArrayList<>();
}
