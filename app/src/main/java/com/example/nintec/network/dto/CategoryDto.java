package com.example.nintec.network.dto;

import com.google.gson.annotations.SerializedName;

public class CategoryDto {
    @SerializedName("id")
    public String id;

    @SerializedName("name")
    public String name;

    @SerializedName("slug")
    public String slug;

    @SerializedName("icon_name")
    public String iconName;

    @SerializedName("image_url")
    public String imageUrl;

    @SerializedName("sort_order")
    public int sortOrder;

    @SerializedName("is_active")
    public boolean isActive;
}
