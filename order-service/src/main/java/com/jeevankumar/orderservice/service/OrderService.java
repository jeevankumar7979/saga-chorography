package com.jeevankumar.orderservice.service;

import com.jeevankumar.orderservice.kafka.OrderEventProducer;
import com.jeevankumar.orderservice.model.OrderEntity;
import com.jeevankumar.orderservice.model.OrderEvent;
import com.jeevankumar.orderservice.repo.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
public class OrderService {

    private final OrderEventProducer orderEventProducer;
    private final OrderRepository orderRepository;

    public OrderService(OrderEventProducer orderEventProducer, OrderRepository orderRepository) {
        this.orderEventProducer = orderEventProducer;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public OrderEntity placeOrder(String customerId, String productId, int quantity, BigDecimal amount) {
        String orderId = UUID.randomUUID().toString();
        OrderEntity order = new OrderEntity(orderId, customerId, productId, quantity, amount);
        orderRepository.save(order);

        orderEventProducer.publish(OrderEvent.of(orderId, customerId, productId, quantity, amount));
        return order;
    }

    @Transactional
    public void updatePaymentStage(String orderId, OrderEntity.Stage stage) {
        int updated = orderRepository.updatePaymentStage(orderId, stage);
        if (updated == 0) {
            log.warn("Received payment update for unknown orderId={}", orderId);
            return;
        }
        logCurrentStatus(orderId);
    }

    @Transactional
    public void updateInventoryStage(String orderId, OrderEntity.Stage stage) {
        int updated = orderRepository.updateInventoryStage(orderId, stage);
        if (updated == 0) {
            log.warn("Received inventory update for unknown orderId={}", orderId);
            return;
        }
        logCurrentStatus(orderId);
    }

    private void logCurrentStatus(String orderId) {
        // Purely informational — a fresh read just for the log line. The two
        // updates above are already safely persisted regardless of what this
        // read sees (it could still race with a concurrent update and print
        // a stale overallStatus() momentarily, but it can never corrupt data,
        // since it's read-only).
        orderRepository.findById(orderId)
                .ifPresent(order -> log.info("Order {} overall status now: {}", orderId, order.overallStatus()));
    }

    @Transactional
    public OrderEntity getOrder(String orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }

}