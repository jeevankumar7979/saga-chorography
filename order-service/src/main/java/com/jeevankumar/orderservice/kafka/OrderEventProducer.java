package com.jeevankumar.orderservice.kafka;

import com.jeevankumar.orderservice.model.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderEventProducer {
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    private static final String ORDER_EVENT_TOPIC = "order-event";

    public OrderEventProducer(KafkaTemplate<String, OrderEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaTemplate.setObservationEnabled(true);
    }

    public void publish(OrderEvent event) {
        kafkaTemplate.send(ORDER_EVENT_TOPIC, event.orderId(), event)
                .whenComplete((result, error) -> {
                    if (error != null) {
                        log.error("Error while publishing order event", error);
                    }else  {
                        log.info("Order event published successfully {}", event.orderId());
                    }
                });

    }
}
