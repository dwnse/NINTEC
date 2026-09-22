package com.example.nintec.network.dto;

import com.google.gson.annotations.SerializedName;

public class CartItemDto {
    @SerializedName("id")
    public String id;

    @SerializedName("user_id")
    public String userId;

    @SerializedName("product_id")
    public String productId;

    @SerializedName("quantity")
    public int quantity;

    // Embedded via PostgREST select
    @SerializedName("products")
    public ProductDto product;
}
