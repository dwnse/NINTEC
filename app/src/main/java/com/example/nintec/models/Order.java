package com.example.nintec.models;

import java.util.ArrayList;
import java.util.List;

public class Order {
    private String id;
    private List<CartItem> items;
    private String total;
    private String paymentMethod;
    private OrderStatus status;
    private String createdAt;

    public Order(String id, List<CartItem> items, String total, String paymentMethod, OrderStatus status, String createdAt) {
        this.id = id;
        // Deep copy of items to prevent historical data mutation
        this.items = new ArrayList<>();
        for (CartItem item : items) {
            this.items.add(new CartItem(item.getProduct(), item.getQuantity()));
        }
        this.total = total;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public List<CartItem> getItems() { return items; }
    public String getTotal() { return total; }
    public String getPaymentMethod() { return paymentMethod; }
    public OrderStatus getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
}