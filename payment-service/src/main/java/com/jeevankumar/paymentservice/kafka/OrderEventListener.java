package com.jeevankumar.paymentservice.kafka;

import com.jeevankumar.paymentservice.model.OrderEvent;
import com.jeevankumar.paymentservice.model.PaymentEntity;
import com.jeevankumar.paymentservice.model.PaymentEvent;
import com.jeevankumar.paymentservice.repo.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import com.jeevankumar.paymentservice.service.RecentOrderTracker;
import java.util.UUID;

@Component
@Slf4j
public class OrderEventListener {

    private static final String PAYMENT_EVENT_TOPIC = "payment-event";
    private  static final String ORDER_EVENT_TOPIC = "order-event";
    private static final BigDecimal FAILURE_THRESHOLD = new BigDecimal("1000");

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final PaymentRepository paymentRepository;
    private final RecentOrderTracker recentOrderTracker;

    public OrderEventListener(KafkaTemplate<String, Object> kafkaTemplate, PaymentRepository paymentRepository,
                              RecentOrderTracker recentOrderTracker) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaTemplate.setObservationEnabled(true);
        this.paymentRepository = paymentRepository;
        this.recentOrderTracker = recentOrderTracker;
    }

    @KafkaListener(topics = ORDER_EVENT_TOPIC, containerFactory = "orderEventKafkaListenerContainerFactory")
    public void onOrderEvent(OrderEvent orderEvent) {
        log.info("payment-service received order-event {}", orderEvent);

        // idempotency check here
        String orderId;
        String customerId;
        String productId;
        Integer quantity;
        BigDecimal amount;
        switch (orderEvent) {
            case OrderEvent(String id, String customer, String product, Integer units, BigDecimal total, Instant timestamp) -> {
                orderId = id;
                customerId = customer;
                productId = product;
                quantity = units;
                amount = total;
            }
            case null -> throw new IllegalArgumentException("Order event must not be null");
        }

        if (paymentRepository.existsByOrderId(orderId)) {
            log.warn("payment-service received order-id {} already exists", orderId);
            return;
        }

        recentOrderTracker.record(orderId);
        PaymentEvent.PaymentStatus status = switch (amount.compareTo(FAILURE_THRESHOLD)) {
            case -1, 0 -> PaymentEvent.PaymentStatus.PAID;
            default -> PaymentEvent.PaymentStatus.FAILED;
        };
        String paymentId = UUID.randomUUID().toString();

        // save to payment db
        paymentRepository.save(new PaymentEntity(orderId, paymentId, amount, status));

        PaymentEvent paymentEvent = new PaymentEvent(
                orderId,
                paymentId,
                status,
                Instant.now()
        );

        kafkaTemplate.send(PAYMENT_EVENT_TOPIC, paymentEvent.orderId(), paymentEvent)
                .whenComplete((result, error) -> {
                    if(error != null) {
                        log.error("Failed published payment-event {}", paymentEvent.orderId(), error);
                    }else  {
                        log.info("published successfully to payment-event {}", paymentEvent.orderId());
                    }
                });



    }

}
