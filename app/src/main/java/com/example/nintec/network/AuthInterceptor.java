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
        String url = original.url().toString();

        Request.Builder builder = original.newBuilder()
                .header("apikey", apiKey)
                .header("Content-Type", "application/json");

        // Prefer header should only be used for mutations (POST, PATCH)
        String method = original.method();
        if (method.equals("POST") || method.equals("PATCH")) {
            builder.header("Prefer", "return=representation");
        }

        String token = getAccessToken();
        if (token != null && !token.trim().isEmpty()) {
            // User session token
            builder.header("Authorization", "Bearer " + token.trim());
        } else {
            // Always fallback to anon key for public and data access if not logged in
            builder.header("Authorization", "Bearer " + apiKey);
        }

        Response response = chain.proceed(builder.build());

        // If we get a 401 Unauthorized, token might be expired, invalid or anonymous
        if (response.code() == 401) {
            // Auto-retry with anon public key for reading data, banners, profiles, or cart items so app never bugs out
            if ("GET".equalsIgnoreCase(method) || 
                url.contains("/rest/v1/products") || 
                url.contains("/rest/v1/categories") || 
                url.contains("/rest/v1/banners") || 
                url.contains("/rest/v1/branches") || 
                url.contains("/rest/v1/profiles") || 
                url.contains("/rest/v1/cart_items")) {
                response.close();
                Request.Builder retryBuilder = original.newBuilder()
                        .header("apikey", apiKey)
                        .header("Authorization", "Bearer " + apiKey)
                        .header("Content-Type", "application/json");
                if (method.equals("POST") || method.equals("PATCH")) {
                    retryBuilder.header("Prefer", "return=representation");
                }
                return chain.proceed(retryBuilder.build());
            }
        }

        return response;
    }

    private String getAccessToken() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_ACCESS_TOKEN, null);
    }
}
