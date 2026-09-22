package com.example.nintec.network;

import com.example.nintec.network.dto.AuthResponse;
import com.example.nintec.network.dto.UserUpdateRequest;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

/**
 * Retrofit service interface for Supabase Auth endpoints.
 */
public interface SupabaseAuthService {

    /**
     * Register a new user with email and password.
     * The handle_new_user() trigger automatically creates a profile row.
     */
    @POST("auth/v1/signup")
    Call<AuthResponse> signUp(@Body Map<String, Object> body);

    /**
     * Login with email and password.
     * Returns access_token and refresh_token.
     */
    @POST("auth/v1/token")
    Call<AuthResponse> signIn(
            @Query("grant_type") String grantType,
            @Body Map<String, String> body
    );

    /**
     * Get the currently authenticated user's info.
     */
    @GET("auth/v1/user")
    Call<AuthResponse.AuthUser> getUser(@Header("Authorization") String bearerToken);

    /**
     * Update the user's email, password, or metadata.
     */
    @PUT("auth/v1/user")
    Call<AuthResponse.AuthUser> updateUser(
            @Header("Authorization") String bearerToken,
            @Body UserUpdateRequest body
    );

    /**
     * Logout — invalidate the current session.
     */
    @POST("auth/v1/logout")
    Call<Void> logout(@Header("Authorization") String bearerToken);
}
