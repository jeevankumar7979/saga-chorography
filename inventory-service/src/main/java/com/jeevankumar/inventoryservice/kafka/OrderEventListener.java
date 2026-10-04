package com.jeevankumar.inventoryservice.kafka;

import com.jeevankumar.inventoryservice.model.InventoryEvent;
import com.jeevankumar.inventoryservice.model.InventoryReservationEntity;
import com.jeevankumar.inventoryservice.model.OrderEvent;
import com.jeevankumar.inventoryservice.model.Product;
import com.jeevankumar.inventoryservice.repo.InventoryReservationRepository;
import com.jeevankumar.inventoryservice.repo.ProductRepository;
import com.jeevankumar.inventoryservice.service.RecentOrderTracker;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

@Slf4j
@Component
public class OrderEventListener {

    private static final String ORDER_EVENT_TOPIC = "order-event";

    private final InventoryReservationRepository inventoryReservationRepository;
    private final ProductRepository productRepository;
    private final InventoryEventProducer inventoryEventProducer;
    private final RecentOrderTracker recentOrderTracker;


    public OrderEventListener(InventoryReservationRepository inventoryReservationRepository, ProductRepository productRepository,
                             InventoryEventProducer inventoryEventProducer, RecentOrderTracker recentOrderTracker) {
        this.inventoryEventProducer = inventoryEventProducer;
        this.inventoryReservationRepository = inventoryReservationRepository;
        this.productRepository = productRepository;
        this.recentOrderTracker = recentOrderTracker;
    }

    @KafkaListener(topics = ORDER_EVENT_TOPIC, containerFactory = "orderEventKafkaListenerContainerFactory")
    @Transactional
    public void onOrderEvent(OrderEvent orderEvent) {
        log.info("inventory-service received order-event {}", orderEvent);

        String orderId;
        String productId;
        Integer quantity;
        switch (orderEvent) {
            case OrderEvent(String id, String customerId, String product, Integer units, BigDecimal amount, Instant timestamp) -> {
                orderId = id;
                productId = product;
                quantity = units;
            }
            case null -> throw new IllegalArgumentException("Order event must not be null");
        }

        if (inventoryReservationRepository.existsByOrderId(orderId)) {
            log.warn("Inventory record already existing with orderId {}", orderId);
            return;
        }

        recentOrderTracker.record(orderId);
        Optional<Product> maybeProduct = productRepository.findById(productId);
        InventoryEvent.InventoryStatus status;

        if (maybeProduct.isPresent()) {
            Product product = maybeProduct.get();
            if (product.hasStock(quantity)) {
                product.reserve(quantity);
                productRepository.save(product);
                status = InventoryEvent.InventoryStatus.RESERVED;
                log.info("Reserved {} units of {} for order {} ({} remaining)",
                        quantity, productId, orderId, product.getAvailableQuantity());
            } else {
                status = InventoryEvent.InventoryStatus.OUT_OF_STOCK;
                log.info("Insufficient stock for product {} (requested {}), orderId={}",
                        productId, quantity, orderId);
            }
        } else {
            status = InventoryEvent.InventoryStatus.OUT_OF_STOCK;
            log.info("Insufficient stock for product {} (requested {}), orderId={}",
                    productId, quantity, orderId);
        }

        // save to inventory db
        inventoryReservationRepository.save(new InventoryReservationEntity(orderId, productId, quantity, status));

        // publish to inventory-event topic
        inventoryEventProducer.publish(orderId, status);

    }

}
