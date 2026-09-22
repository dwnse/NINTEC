package com.example.nintec.network.dto;

import com.google.gson.annotations.SerializedName;

public class BranchDto {
    @SerializedName("id")
    public String id;

    @SerializedName("name")
    public String name;

    @SerializedName("address")
    public String address;

    @SerializedName("city")
    public String city;

    @SerializedName("latitude")
    public double latitude;

    @SerializedName("longitude")
    public double longitude;

    @SerializedName("phone")
    public String phone;

    @SerializedName("email")
    public String email;

    @SerializedName("image_url")
    public String imageUrl;

    @SerializedName("is_active")
    public boolean isActive;

    public com.example.nintec.models.Branch toBranch() {
        com.example.nintec.models.Branch b = new com.example.nintec.models.Branch(id, name, address, latitude, longitude, phone);
        b.setCity(city);
        b.setImageUrl(imageUrl);
        return b;
    }
}
