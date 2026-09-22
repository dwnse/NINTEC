package com.example.nintec.network.dto;

import com.google.gson.annotations.SerializedName;

public class BannerDto {
    @SerializedName("id")
    public String id;

    @SerializedName("title")
    public String title;

    @SerializedName("subtitle")
    public String subtitle;

    @SerializedName("image_url")
    public String imageUrl;

    @SerializedName("action_type")
    public String actionType;

    @SerializedName("action_value")
    public String actionValue;

    @SerializedName("background_color")
    public String backgroundColor;

    @SerializedName("text_color")
    public String textColor;

    @SerializedName("sort_order")
    public int sortOrder;

    @SerializedName("is_active")
    public boolean isActive;
}
