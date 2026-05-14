package com.ticketmaster.eventservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventInventoryProducer {

    private final KafkaTemplate<String, EventInventoryMessage> kafkaTemplate;

    @Value("${kafka.topics.event-inventory}")
    private String topic;

    public void publishInventory(EventInventoryMessage message) {
        kafkaTemplate.send(topic, message.getEventId().toString(), message)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish inventory for event {}: {}",
                                message.getEventId(), ex.getMessage());
                    } else {
                        log.info("Published inventory for event {} with {} seats",
                                message.getEventId(), message.getSeats().size());
                    }
                });
    }
}
