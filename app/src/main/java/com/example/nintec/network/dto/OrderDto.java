package com.example.nintec.network.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OrderDto {
    @SerializedName("id")
    public String id;

    @SerializedName("order_number")
    public String orderNumber;

    @SerializedName("status")
    public String status;

    @SerializedName("payment_method")
    public String paymentMethod;

    @SerializedName("payment_status")
    public String paymentStatus;

    @SerializedName("subtotal")
    public double subtotal;

    @SerializedName("discount")
    public double discount;

    @SerializedName("tax")
    public double tax;

    @SerializedName("total")
    public double total;

    @SerializedName("notes")
    public String notes;

    @SerializedName("created_at")
    public String createdAt;

    // Embedded via PostgREST select
    @SerializedName("order_items")
    public List<OrderItemDto> items;

    public static class OrderItemDto {
        @SerializedName("id")
        public String id;

        @SerializedName("product_id")
        public String productId;

        @SerializedName("product_name")
        public String productName;

        @SerializedName("product_image")
        public String productImage;

        @SerializedName("unit_price")
        public double unitPrice;

        @SerializedName("quantity")
        public int quantity;

        @SerializedName("line_total")
        public double lineTotal;
    }

    public com.example.nintec.models.Order toOrder() {
        com.example.nintec.models.OrderStatus orderStatus = com.example.nintec.models.OrderStatus.PENDING;
        if ("completed".equalsIgnoreCase(status) || "delivered".equalsIgnoreCase(status)) {
            orderStatus = com.example.nintec.models.OrderStatus.COMPLETED;
        } else if ("cancelled".equalsIgnoreCase(status)) {
            orderStatus = com.example.nintec.models.OrderStatus.REJECTED;
        } else {
            orderStatus = com.example.nintec.models.OrderStatus.PENDING;
        }

        java.util.List<com.example.nintec.models.CartItem> cartItems = new java.util.ArrayList<>();
        if (items != null) {
            for (OrderItemDto item : items) {
                com.example.nintec.models.Product p = new com.example.nintec.models.Product(
                        item.productId != null ? item.productId : "",
                        item.productName != null ? item.productName : "Producto",
                        "",
                        item.unitPrice,
                        null,
                        item.productImage,
                        false,
                        10
                );
                cartItems.add(new com.example.nintec.models.CartItem(p, item.quantity));
            }
        }
        com.example.nintec.models.Order order = new com.example.nintec.models.Order(
                id,
                orderNumber,
                cartItems,
                total,
                paymentMethod != null ? paymentMethod : "Efectivo",
                orderStatus,
                createdAt != null ? createdAt.substring(0, Math.min(createdAt.length(), 10)) : ""
        );
        order.setSubtotal(subtotal);
        order.setDiscount(discount);
        return order;
    }
}
