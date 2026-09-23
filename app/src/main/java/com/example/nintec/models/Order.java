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

    // Branch info
    private String branchId;
    private String branchName;
    private String branchCity;
    private String branchAddress;

    // Payment details
    private String paymentDetails;
    private String notes;

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
    public void setId(String id) { this.id = id; }
    public String getOrderNumber() { return orderNumber != null ? orderNumber : id; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public List<CartItem> getItems() { return items; }

    public String getTotal() { return Product.formatPrice(total); }
    public double getTotalValue() { return total; }

    public String getPaymentMethod() { return paymentMethod; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public String getCreatedAt() { return createdAt; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }

    public String getBranchId() { return branchId; }
    public void setBranchId(String branchId) { this.branchId = branchId; }
    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }
    public String getBranchCity() { return branchCity; }
    public void setBranchCity(String branchCity) { this.branchCity = branchCity; }
    public String getBranchAddress() { return branchAddress; }
    public void setBranchAddress(String branchAddress) { this.branchAddress = branchAddress; }

    public String getPaymentDetails() { return paymentDetails; }
    public void setPaymentDetails(String paymentDetails) { this.paymentDetails = paymentDetails; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    /** Returns a short summary of items for list display, e.g. "MacBook Pro + 2 más" */
    public String getItemsSummary() {
        if (items == null || items.isEmpty()) return "Sin productos";
        String first = items.get(0).getProduct().getName();
        if (items.size() == 1) return first;
        return first + " + " + (items.size() - 1) + " más";
    }

    /** Returns total item count */
    public int getTotalItemCount() {
        if (items == null) return 0;
        int count = 0;
        for (CartItem item : items) {
            count += item.getQuantity();
        }
        return count;
    }
}