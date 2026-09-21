package com.jeevankumar.inventoryservice.kafka;

import com.jeevankumar.inventoryservice.model.InventoryEvent;
import com.jeevankumar.inventoryservice.model.InventoryReservationEntity;
import com.jeevankumar.inventoryservice.model.OrderEvent;
import com.jeevankumar.inventoryservice.model.Product;
import com.jeevankumar.inventoryservice.repo.InventoryReservationRepository;
import com.jeevankumar.inventoryservice.repo.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class OrderEventListener {

    private static final String ORDER_EVENT_TOPIC = "order-event";

    private final InventoryReservationRepository inventoryReservationRepository;
    private final ProductRepository productRepository;
    private final InventoryEventProducer inventoryEventProducer;


    public OrderEventListener(InventoryReservationRepository inventoryReservationRepository, ProductRepository productRepository, InventoryEventProducer inventoryEventProducer) {
        this.inventoryEventProducer = inventoryEventProducer;
        this.inventoryReservationRepository = inventoryReservationRepository;
        this.productRepository = productRepository;
    }

    @KafkaListener(topics = ORDER_EVENT_TOPIC, containerFactory = "orderEventKafkaListenerContainerFactory")
    @Transactional
    public void onOrderEvent(OrderEvent orderEvent) {
        log.info("inventory-service received order-event {}", orderEvent);

        if (inventoryReservationRepository.existsByOrderId(orderEvent.orderId())) {
            log.warn("Inventory record already existing with orderId {}", orderEvent.orderId());
            return;
        }

        Optional<Product> maybeProduct = productRepository.findById(orderEvent.productId());
        InventoryEvent.InventoryStatus status = null;

        if (maybeProduct.isPresent()) {
            Product product = maybeProduct.get();
            if (product.hasStock(orderEvent.quantity())) {
                product.reserve(orderEvent.quantity());
                productRepository.save(product);
                status = InventoryEvent.InventoryStatus.RESERVED;
                log.info("Reserved {} units of {} for order {} ({} remaining)",
                        orderEvent.quantity(), orderEvent.productId(), orderEvent.orderId(), product.getAvailableQuantity());
            } else {
                status = InventoryEvent.InventoryStatus.OUT_OF_STOCK;
                log.info("Insufficient stock for product {} (requested {}), orderId={}",
                        orderEvent.productId(), orderEvent.quantity(), orderEvent.orderId());
            }
        } else {
            status = InventoryEvent.InventoryStatus.OUT_OF_STOCK;
            log.info("Insufficient stock for product {} (requested {}), orderId={}",
                    orderEvent.productId(), orderEvent.quantity(), orderEvent.orderId());
        }

        // save to inventory db
        inventoryReservationRepository.save(new InventoryReservationEntity(orderEvent.orderId(), orderEvent.productId(), orderEvent.quantity(), status));

        // publish to inventory-event topic
        inventoryEventProducer.publish(orderEvent.orderId(), status);

    }

}
