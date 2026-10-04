package com.jeevankumar.orderservice.controller;


import com.jeevankumar.orderservice.model.OrderEntity;
import com.jeevankumar.orderservice.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    public record PlaceOrderRequest(
            String customerId,
            @NotBlank String productId,
            @NotNull @Positive Integer quantity,
            @NotNull @DecimalMin("0.0") BigDecimal amount) {}

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
    public ResponseEntity<OrderStatusResponse> placeOrder(@Valid @RequestBody PlaceOrderRequest request,
                                                          Authentication authentication) {
        OrderEntity order = orderService.placeOrder(
                authentication.getName(), request.productId(), request.quantity(), request.amount());
        return ResponseEntity.ok(toResponse(order));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderStatusResponse> getOrder(@PathVariable String orderId,
                                                        Authentication authentication) {
        OrderEntity order = orderService.getOrder(orderId, authentication.getName(), isAdmin(authentication));
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toResponse(order));
    }

    @GetMapping("/recent")
    public List<String> recentOrders(Authentication authentication) {
        return orderService.recentOrderIds(authentication.getName(), isAdmin(authentication));
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
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
