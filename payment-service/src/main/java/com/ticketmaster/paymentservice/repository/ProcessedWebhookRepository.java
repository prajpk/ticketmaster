package com.ticketmaster.paymentservice.repository;

import com.ticketmaster.paymentservice.entity.ProcessedWebhook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProcessedWebhookRepository extends JpaRepository<ProcessedWebhook, UUID> {
    boolean existsByProviderAndProviderEventId(String provider, String providerEventId);
}
