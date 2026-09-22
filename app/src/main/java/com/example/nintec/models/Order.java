package com.example.nintec.models;

import java.util.ArrayList;
import java.util.List;

public class Order {
    private String id;
    private String orderNumber;
    private List<CartItem> items;
    private double total;
    private String paymentMethod;
    private OrderStatus status;
    private String createdAt;
    private double subtotal;
    private double discount;

    public Order(String id, List<CartItem> items, double total,
                 String paymentMethod, OrderStatus status, String createdAt) {
        this(id, id, items, total, paymentMethod, status, createdAt);
    }

    public Order(String id, String orderNumber, List<CartItem> items, double total,
                 String paymentMethod, OrderStatus status, String createdAt) {
        this.id = id;
        this.orderNumber = orderNumber;
        // Deep copy of items to prevent historical data mutation
        this.items = new ArrayList<>();
        if (items != null) {
            for (CartItem item : items) {
                this.items.add(new CartItem(item.getProduct(), item.getQuantity()));
            }
        }
        this.total = total;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getOrderNumber() { return orderNumber != null ? orderNumber : id; }
    public List<CartItem> getItems() { return items; }

    /**
     * Returns total formatted for display.
     */
    public String getTotal() { return Product.formatPrice(total); }
    public double getTotalValue() { return total; }

    public String getPaymentMethod() { return paymentMethod; }
    public OrderStatus getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }
}