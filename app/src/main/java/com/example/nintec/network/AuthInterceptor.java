package com.example.nintec.network;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * OkHttp Interceptor that automatically injects Supabase API key
 * and the user's Bearer token (if authenticated) into every request.
 */
public class AuthInterceptor implements Interceptor {

    private static final String PREFS_NAME = "nintec_auth";
    private static final String KEY_ACCESS_TOKEN = "access_token";

    private final String apiKey;
    private final Context context;

    public AuthInterceptor(Context context, String apiKey) {
        this.context = context.getApplicationContext();
        this.apiKey = apiKey;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();

        Request.Builder builder = original.newBuilder()
                .header("apikey", apiKey)
                .header("Content-Type", "application/json")
                .header("Prefer", "return=representation");

        // Add Bearer token if user is logged in
        String token = getAccessToken();
        if (token != null && !token.isEmpty()) {
            builder.header("Authorization", "Bearer " + token);
        } else {
            // Use anon key as fallback authorization
            builder.header("Authorization", "Bearer " + apiKey);
        }

        return chain.proceed(builder.build());
    }

    private String getAccessToken() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_ACCESS_TOKEN, null);
    }
}
