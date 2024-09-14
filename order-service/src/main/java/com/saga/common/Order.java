package com.saga.common;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Order {

    @JsonProperty("id")
    private String id;

    @JsonProperty("amount")
    private double amount;

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
