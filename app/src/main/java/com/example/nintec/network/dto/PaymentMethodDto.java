package com.example.nintec.network.dto;

import com.google.gson.annotations.SerializedName;

public class PaymentMethodDto {
    @SerializedName("id")
    public String id;

    @SerializedName("name")
    public String name;

    @SerializedName("code")
    public String code;

    @SerializedName("description")
    public String description;

    @SerializedName("icon_name")
    public String iconName;

    @SerializedName("image_url")
    public String imageUrl;

    @SerializedName("instructions")
    public String instructions;

    @SerializedName("sort_order")
    public int sortOrder;

    @SerializedName("is_active")
    public boolean isActive;

    @SerializedName("requires_proof")
    public boolean requiresProof;
}
