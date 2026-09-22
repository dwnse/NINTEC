package com.example.nintec.network.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Response from Supabase Auth endpoints (signup/signin).
 */
public class AuthResponse {

    @SerializedName("access_token")
    public String accessToken;

    @SerializedName("refresh_token")
    public String refreshToken;

    @SerializedName("token_type")
    public String tokenType;

    @SerializedName("expires_in")
    public int expiresIn;

    @SerializedName("user")
    public AuthUser user;

    public static class AuthUser {
        @SerializedName("id")
        public String id;

        @SerializedName("email")
        public String email;

        @SerializedName("user_metadata")
        public UserMetadata userMetadata;

        @SerializedName("created_at")
        public String createdAt;
    }

    public static class UserMetadata {
        @SerializedName("full_name")
        public String fullName;

        @SerializedName("username")
        public String username;
    }
}
