package com.jeevankumar.inventoryservice.kafka;

import com.jeevankumar.inventoryservice.model.InventoryEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
public class InventoryEventProducer {

    private static final String INVENTORY_EVENT_TOPIC = "inventory-event";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public InventoryEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaTemplate.setObservationEnabled(true);
    }

    public void publish(String orderId, InventoryEvent.InventoryStatus status) {
        InventoryEvent inventoryEvent = new InventoryEvent(orderId, status, Instant.now());

        kafkaTemplate.send(INVENTORY_EVENT_TOPIC, inventoryEvent.orderId(), inventoryEvent)
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        log.error("Error while sending inventory-event {}", orderId, throwable);
                    } else {
                        log.info("Inventory event sent inventory-event {}", inventoryEvent.orderId());
                    }
                });
    }

}
