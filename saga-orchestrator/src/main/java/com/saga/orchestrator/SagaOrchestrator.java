package com.saga.orchestrator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saga.common.Order;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class SagaOrchestrator {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final KafkaTemplate<String, String> kafkaTemplate;

    public SagaOrchestrator(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "order_topic", groupId = "saga_group")
    public void orchestrateSaga(String message) {
        try {
            Order order = objectMapper.readValue(message, Order.class);
            System.out.println("Orchestrating saga for order: " + order.getId());

            // SagaOrchestrator only coordinates messages
            kafkaTemplate.send("check_stock_topic", message);  // Trigger stock check
        } catch (Exception e) {
            System.out.println("Error in saga process: " + e.getMessage());
        }
    }

    @KafkaListener(topics = "stock_success_topic", groupId = "saga_group")
    public void onStockSuccess(String message) {
        try {
            Order order = objectMapper.readValue(message, Order.class);
            System.out.println("Stock success for order: " + order.getId());
            kafkaTemplate.send("process_payment_topic", message);  // Trigger payment process
        } catch (Exception e) {
            System.out.println("Error in saga process: " + e.getMessage());
        }
    }

    @KafkaListener(topics = "payment_success_topic", groupId = "saga_group")
    public void onPaymentSuccess(String message) {
        try {
            Order order = objectMapper.readValue(message, Order.class);
            System.out.println("Payment success for order: " + order.getId());
            System.out.println("Saga completed successfully for order: " + order.getId());
        } catch (Exception e) {
            System.out.println("Error in saga process: " + e.getMessage());
        }
    }

    @KafkaListener(topics = "stock_failure_topic", groupId = "saga_group")
    public void onStockFailure(String message) {
        // In case of stock failure, trigger rollback
        kafkaTemplate.send("rollback_order_topic", message);
        System.out.println("rollback_order_topic: " + message);
    }

    @KafkaListener(topics = "payment_failure_topic", groupId = "saga_group")
    public void onPaymentFailure(String message) {
        // In case of payment failure, trigger stock rollback
        kafkaTemplate.send("rollback_stock_topic", message);
        System.out.println("rollback_stock_topic: " + message);
    }
}
