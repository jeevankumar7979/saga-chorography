package com.jeevankumar.paymentservice.kafka;

import com.jeevankumar.paymentservice.model.InventoryEvent;
import com.jeevankumar.paymentservice.model.PaymentEntity;
import com.jeevankumar.paymentservice.model.PaymentEvent;
import com.jeevankumar.paymentservice.repo.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@Slf4j
public class InventoryEventListener {

    private static final String PAYMENT_EVENT_TOPIC = "payment-event";

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final PaymentRepository paymentRepository;

    public InventoryEventListener(KafkaTemplate<String, Object> kafkaTemplate, PaymentRepository paymentRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaTemplate.setObservationEnabled(true);
        this.paymentRepository = paymentRepository;
    }

    @KafkaListener(topics = "inventory-event", containerFactory = "inventoryEventKafkaListenerContainerFactory")
    @Transactional
    public void onInventoryEvent(InventoryEvent inventoryEvent) {
        log.info("payment-service received inventory-event {}", inventoryEvent);

        if (inventoryEvent.status() != InventoryEvent.InventoryStatus.OUT_OF_STOCK) {
            return;
        }

        PaymentEntity payment = paymentRepository.findByOrderId(inventoryEvent.orderId()).orElse(null);
        if (payment == null) {
            log.info("No payment found for out-of-stock order {}", inventoryEvent.orderId());
            return;
        }
        if (payment.getStatus() != PaymentEvent.PaymentStatus.PAID) {
            log.info("Payment for order {} is {}, nothing to refund",
                    inventoryEvent.orderId(), payment.getStatus());
            return;
        }

        payment.setStatus(PaymentEvent.PaymentStatus.REFUNDED);
        paymentRepository.save(payment);

        PaymentEvent refundEvent = new PaymentEvent(
                payment.getOrderId(),
                payment.getPaymentId(),
                PaymentEvent.PaymentStatus.REFUNDED,
                Instant.now()
        );
        kafkaTemplate.send(PAYMENT_EVENT_TOPIC, refundEvent.orderId(), refundEvent)
                .whenComplete((result, error) -> {
                    if (error != null) {
                        log.error("Failed to publish refund payment-event {}", refundEvent.orderId(), error);
                    } else {
                        log.info("Published refund payment-event {}", refundEvent.orderId());
                    }
                });
    }
}
