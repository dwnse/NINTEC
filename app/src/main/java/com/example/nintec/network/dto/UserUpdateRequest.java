package com.example.nintec.network.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Request body for updating Supabase Auth user.
 */
public class UserUpdateRequest {

    @SerializedName("email")
    public String email;

    @SerializedName("password")
    public String password;

    @SerializedName("data")
    public UserData data;

    public static class UserData {
        @SerializedName("full_name")
        public String fullName;

        @SerializedName("username")
        public String username;
    }
}
