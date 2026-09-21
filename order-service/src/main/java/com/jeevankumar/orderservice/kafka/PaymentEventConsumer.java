package com.jeevankumar.orderservice.kafka;

import com.jeevankumar.orderservice.model.OrderEntity;
import com.jeevankumar.orderservice.model.PaymentEvent;
import com.jeevankumar.orderservice.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

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

        OrderEntity.Stage stage = paymentEvent.status() == PaymentEvent.PaymentStatus.PAID
                ? OrderEntity.Stage.PAYMENT_CONFIRMED
                : OrderEntity.Stage.PAYMENT_FAILED;

        orderService.updatePaymentStage(paymentEvent.orderId(), stage);

    }
}
