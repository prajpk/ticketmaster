package com.ticketmaster.paymentservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "processed_webhooks",
       uniqueConstraints = @UniqueConstraint(columnNames = {"provider", "providerEventId"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProcessedWebhook {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String provider;

    @Column(nullable = false)
    private String providerEventId;

    @Column(nullable = false)
    private String eventType;

    @CreationTimestamp
    private OffsetDateTime processedAt;
}
