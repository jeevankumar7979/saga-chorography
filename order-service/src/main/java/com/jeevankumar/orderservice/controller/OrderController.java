package com.jeevankumar.orderservice.controller;


import com.jeevankumar.orderservice.model.OrderEntity;
import com.jeevankumar.orderservice.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    public record PlaceOrderRequest(String customerId, String productId, Integer quantity, BigDecimal amount) {}

    public record OrderStatusResponse(
            String orderId,
            String customerId,
            String productId,
            Integer quantity,
            BigDecimal amount,
            String paymentStage,
            String inventoryStage,
            String overallStatus
    ) {}

    @PostMapping
    public ResponseEntity<OrderStatusResponse> placeOrder(@RequestBody PlaceOrderRequest request) {
        OrderEntity order = orderService.placeOrder(
                request.customerId(), request.productId(), request.quantity(), request.amount());
        return ResponseEntity.ok(toResponse(order));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderStatusResponse> getOrder(@PathVariable String orderId) {
        OrderEntity order = orderService.getOrder(orderId);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toResponse(order));
    }

    private OrderStatusResponse toResponse(OrderEntity order) {
        return new OrderStatusResponse(
                order.getOrderId(),
                order.getCustomerId(),
                order.getProductId(),
                order.getQuantity(),
                order.getAmount(),
                order.getPaymentStage().name(),
                order.getInventoryStage().name(),
                order.overallStatus().name()
        );
    }
}
