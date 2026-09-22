package com.example.nintec.network;

import android.content.Context;

import com.example.nintec.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Singleton client for all Supabase API communications.
 * Provides Retrofit service instances for Auth and Data endpoints.
 */
public class SupabaseClient {

    private static SupabaseClient instance;

    private final SupabaseAuthService authService;
    private final SupabaseDataService dataService;
    private final String supabaseUrl;

    private SupabaseClient(Context context) {
        supabaseUrl = BuildConfig.SUPABASE_URL;
        String anonKey = BuildConfig.SUPABASE_ANON_KEY;

        // Logging interceptor for debug builds
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Auth interceptor adds apikey + Bearer token to every request
        AuthInterceptor authInterceptor = new AuthInterceptor(context, anonKey);

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(supabaseUrl + "/")
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        authService = retrofit.create(SupabaseAuthService.class);
        dataService = retrofit.create(SupabaseDataService.class);
    }

    /**
     * Initialize the client. Must be called once from Application or first Activity.
     */
    public static synchronized void init(Context context) {
        if (instance == null && context != null) {
            instance = new SupabaseClient(context.getApplicationContext());
        }
    }

    public static synchronized SupabaseClient getInstance() {
        if (instance == null) {
            Context ctx = com.example.nintec.NintecApp.getAppContext();
            if (ctx != null) {
                instance = new SupabaseClient(ctx);
            }
        }
        return instance;
    }

    public SupabaseAuthService getAuthService() {
        return authService;
    }

    public SupabaseDataService getDataService() {
        return dataService;
    }

    public String getSupabaseUrl() {
        return supabaseUrl;
    }

    /**
     * Get the full public URL for a file in Supabase Storage.
     * @param bucket The storage bucket name (e.g., "product-images")
     * @param path The file path within the bucket
     * @return Full public URL
     */
    public String getStorageUrl(String bucket, String path) {
        return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + path;
    }
}
