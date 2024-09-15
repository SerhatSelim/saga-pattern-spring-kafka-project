package com.saga.paymentservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saga.common.Order;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final KafkaTemplate<String, String> kafkaTemplate;

    public PaymentService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "process_payment_topic", groupId = "payment_group")
    public void processPayment(String message) {
        try {
            Order order = objectMapper.readValue(message, Order.class);
            System.out.println("Processing payment for order: " + order.getId());

            // Simulate payment processing logic
            boolean paymentSuccess = processPaymentForOrder(order);

            if (paymentSuccess) {
                // Notify SagaOrchestrator about payment success
                kafkaTemplate.send("payment_success_topic", message);
                System.out.println("Payment is successful for order: " + order.getId());
            } else {
                // Notify SagaOrchestrator about payment failure
                kafkaTemplate.send("payment_failure_topic", message);
                System.out.println("Payment failed for order: " + order.getId());
            }
        } catch (Exception e) {
            // Notify SagaOrchestrator about payment failure if there is any exception
            kafkaTemplate.send("payment_failure_topic", message);
            System.out.println("Error while processing payment: " + e.getMessage());
        }
    }

    private boolean processPaymentForOrder(Order order) {
        // Simulate actual payment processing logic
        // For example, call a payment gateway or bank API
        return "12345".equals(order.getId()) || "125561".equals(order.getId()); // Assume payment succeeds for this example
    }

    @KafkaListener(topics = "rollback_payment_topic", groupId = "payment_group")
    public void rollbackPayment(String message) {
        try {
            Order order = objectMapper.readValue(message, Order.class);
            System.out.println("Rolling back payment for order: " + order.getId());
            // Implement logic to refund or undo the payment here
        } catch (Exception e) {
            System.out.println("Error during payment rollback: " + e.getMessage());
        }
    }
}
