package com.saga.orderservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saga.common.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private static final String TOPIC = "order_topic";

    @PostMapping("/create")
    public String createOrder(@RequestBody Order order) throws JsonProcessingException {
        String orderJson = new ObjectMapper().writeValueAsString(order);
        kafkaTemplate.send(TOPIC, orderJson);
        return "Order created successfully";
    }
}