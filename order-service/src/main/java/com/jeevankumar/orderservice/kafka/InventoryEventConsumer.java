package com.jeevankumar.orderservice.kafka;

import com.jeevankumar.orderservice.model.InventoryEvent;
import com.jeevankumar.orderservice.model.OrderEntity;
import com.jeevankumar.orderservice.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class InventoryEventConsumer {

    private final OrderService orderService;

    public InventoryEventConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @KafkaListener(topics = "inventory-event", containerFactory = "inventoryEventKafkaListenerContainerFactory")
    public void onInventoryEvent(InventoryEvent event) {
        log.info("order-service received inventory-event: {}", event);

        OrderEntity.Stage stage = switch (event.status()) {
            case RESERVED -> OrderEntity.Stage.INVENTORY_RESERVED;
            case OUT_OF_STOCK -> OrderEntity.Stage.INVENTORY_FAILED;
            case CANCELLED -> OrderEntity.Stage.INVENTORY_CANCELLED;
        };

        orderService.updateInventoryStage(event.orderId(), stage);
    }
}