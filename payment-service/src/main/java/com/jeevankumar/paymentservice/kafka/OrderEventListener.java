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
import java.util.UUID;

@Component
@Slf4j
public class OrderEventListener {

    private static final String PAYMENT_EVENT_TOPIC = "payment-event";
    private  static final String ORDER_EVENT_TOPIC = "order-event";
    private static final BigDecimal FAILURE_THRESHOLD = new BigDecimal("1000");

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final PaymentRepository paymentRepository;

    public OrderEventListener(KafkaTemplate<String, Object> kafkaTemplate, PaymentRepository paymentRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaTemplate.setObservationEnabled(true);
        this.paymentRepository = paymentRepository;
    }

    @KafkaListener(topics = ORDER_EVENT_TOPIC, containerFactory = "orderEventKafkaListenerContainerFactory")
    public void onOrderEvent(OrderEvent orderEvent) {
        log.info("payment-service received order-event {}", orderEvent);

        // idempotency check here
        if(paymentRepository.existsByOrderId(orderEvent.orderId())) {
            log.warn("payment-service received order-id {} already exists", orderEvent.orderId());
            return;
        }

        boolean isApproved = orderEvent.amount().compareTo(FAILURE_THRESHOLD) <= 0;
        PaymentEvent.PaymentStatus status = isApproved ? PaymentEvent.PaymentStatus.PAID
                : PaymentEvent.PaymentStatus.FAILED;
        String paymentId = UUID.randomUUID().toString();

        // save to payment db
        paymentRepository.save(new PaymentEntity(orderEvent.orderId(), paymentId, orderEvent.amount(), status));

        PaymentEvent paymentEvent = new PaymentEvent(
                orderEvent.orderId(),
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
