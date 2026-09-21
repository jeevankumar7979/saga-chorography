package com.jeevankumar.inventoryservice.kafka;

import com.jeevankumar.inventoryservice.model.InventoryEvent;
import com.jeevankumar.inventoryservice.model.InventoryReservationEntity;
import com.jeevankumar.inventoryservice.model.PaymentEvent;
import com.jeevankumar.inventoryservice.repo.InventoryReservationRepository;
import com.jeevankumar.inventoryservice.repo.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class PaymentEventListener {

    private static final String PAYMENT_EVENT_TOPIC = "payment-event";
    private final InventoryEventProducer inventoryEventProducer;
    private final ProductRepository productRepository;
    private final InventoryReservationRepository inventoryReservationRepository;

    public PaymentEventListener(InventoryEventProducer inventoryEventProducer, ProductRepository productRepository, InventoryReservationRepository inventoryReservationRepository) {
        this.inventoryEventProducer = inventoryEventProducer;
        this.productRepository = productRepository;
        this.inventoryReservationRepository = inventoryReservationRepository;
    }

    @KafkaListener(topics = PAYMENT_EVENT_TOPIC, containerFactory = "paymentEventKafkaListenerContainerFactory")
    public void onPaymentEvent(PaymentEvent paymentEvent) {
        log.info("inventory-service received payment-event {}", paymentEvent.orderId());

        // if payment status is paid , which means no need to revert stock in product service
        if(paymentEvent.status() != PaymentEvent.PaymentStatus.FAILED) {
            return;
        }
        log.info("inventory-service received payment-event FAILED, checking for a reservation to release: {}", paymentEvent);

        Optional<InventoryReservationEntity> maybeInventoryReserved = inventoryReservationRepository.findByOrderId(paymentEvent.orderId());
        if(maybeInventoryReserved.isEmpty()) {
            log.info("inventory-reservations not found for order {}", paymentEvent.orderId());
            return;
        }

        InventoryReservationEntity reservation = maybeInventoryReserved.get();
        if(reservation.getStatus() != InventoryEvent.InventoryStatus.RESERVED){
            log.info("Reservation for orderId={} is {} (not RESERVED), nothing to release",
                    paymentEvent.orderId(), reservation.getStatus());
            return;
        }

        // Here quantity is decreased...
        productRepository.findById(reservation.getProductId()).ifPresent((product) -> {
            product.release(reservation.getQuantity());
            productRepository.save(product);
            log.info("Released {} units of {} back to stock for cancelled order {} ({} now available)",
                    reservation.getQuantity(), reservation.getProductId(), paymentEvent.orderId(), product.getAvailableQuantity());
        });

        reservation.setStatus(InventoryEvent.InventoryStatus.CANCELLED);
        inventoryReservationRepository.save(reservation);

        //  publish inventory-event to inventory-event topic
        inventoryEventProducer.publish(paymentEvent.orderId(), InventoryEvent.InventoryStatus.CANCELLED);




    }
}
