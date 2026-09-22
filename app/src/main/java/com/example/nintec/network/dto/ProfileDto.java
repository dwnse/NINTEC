package com.example.nintec.network.dto;

import com.google.gson.annotations.SerializedName;

public class ProfileDto {
    @SerializedName("id")
    public String id;

    @SerializedName("full_name")
    public String fullName;

    @SerializedName("username")
    public String username;

    @SerializedName("email")
    public String email;

    @SerializedName("phone")
    public String phone;

    @SerializedName("avatar_url")
    public String avatarUrl;

    @SerializedName("role")
    public String role;

    @SerializedName("is_active")
    public boolean isActive;
}
