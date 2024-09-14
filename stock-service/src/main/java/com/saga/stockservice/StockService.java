package com.saga.stockservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saga.common.Order;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class StockService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final KafkaTemplate<String, String> kafkaTemplate;

    public StockService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "check_stock_topic", groupId = "stock_group")
    public void checkStock(String message) {
        try {
            Order order = objectMapper.readValue(message, Order.class);
            System.out.println("Checking stock for order: " + order.getId());

            // Simulate stock checking logic
            boolean stockAvailable = checkStockForOrder(order);

            if (stockAvailable) {
                // Notify SagaOrchestrator about stock success
                kafkaTemplate.send("stock_success_topic", message);
                System.out.println("Stock is available for order: " + order.getId());
            } else {
                // Notify SagaOrchestrator about stock failure
                kafkaTemplate.send("stock_failure_topic", message);
                System.out.println("Stock is not available for order: " + order.getId());
            }
        } catch (Exception e) {
            // Notify SagaOrchestrator about stock failure if there is any exception
            kafkaTemplate.send("stock_failure_topic", message);
            System.out.println("Error while checking stock: " + e.getMessage());
        }
    }

    private boolean checkStockForOrder(Order order) {
        // Simulate actual stock checking logic
        // For example, you can query a database or inventory system
        return "123456".equals(order.getId()); // Assume stock is available for this example
    }

    @KafkaListener(topics = "rollback_stock_topic", groupId = "stock_group")
    public void rollbackStock(String message) {
        try {
            Order order = objectMapper.readValue(message, Order.class);
            System.out.println("Rolling back stock for order: " + order.getId());
            // Implement logic to roll back the stock allocation here
        } catch (Exception e) {
            System.out.println("Error during stock rollback: " + e.getMessage());
        }
    }
}
