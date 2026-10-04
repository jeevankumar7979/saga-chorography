package com.jeevankumar.orderservice.kafka;

import com.jeevankumar.orderservice.model.OrderEntity;
import com.jeevankumar.orderservice.model.PaymentEvent;
import com.jeevankumar.orderservice.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
public class PaymentEventConsumer {
    private final OrderService orderService;

    public PaymentEventConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @KafkaListener(topics = "payment-event", containerFactory = "paymentEventKafkaListenerContainerFactory")
    public void onPaymentEvent(PaymentEvent paymentEvent) {
        log.info("order-service received payment-event {}", paymentEvent);

        OrderEntity.Stage stage = switch (paymentEvent) {
            case PaymentEvent(String orderId, String paymentId, PaymentEvent.PaymentStatus status, Instant timestamp) ->
                    switch (status) {
                        case PAID -> OrderEntity.Stage.PAYMENT_CONFIRMED;
                        case FAILED, REFUNDED -> OrderEntity.Stage.PAYMENT_FAILED;
                    };
            case null -> throw new IllegalArgumentException("Payment event must not be null");
        };

        orderService.updatePaymentStage(paymentEvent.orderId(), stage);

    }
}
