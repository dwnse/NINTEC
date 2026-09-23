package com.example.nintec.managers;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.nintec.NintecApp;
import com.example.nintec.models.User;
import com.example.nintec.network.SupabaseClient;
import com.example.nintec.network.dto.AuthResponse;
import com.example.nintec.network.dto.ProfileDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Manages user authentication and session state using Supabase Auth.
 * Stores tokens in SharedPreferences for persistence across app restarts.
 */
public class SessionManager {

    private static final String PREFS_NAME = "nintec_auth";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";

    private static SessionManager instance;
    private User currentUser;
    private Context context;

    public interface AuthCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface ProfileCallback {
        void onLoaded(User user);
        void onError(String message);
    }

    private SessionManager(Context context) {
        this.context = context.getApplicationContext();
        SupabaseClient.init(this.context);
        
        // Auto-load profile if token exists
        if (isLoggedIn()) {
            loadUserProfile(new ProfileCallback() {
                @Override
                public void onLoaded(User user) {}
                @Override
                public void onError(String message) {
                    if (message.contains("401")) {
                        logout();
                    }
                }
            });
        }
    }

    public static synchronized void init(Context context) {
        if (instance == null && context != null) {
            instance = new SessionManager(context.getApplicationContext());
        }
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            Context ctx = NintecApp.getAppContext();
            if (ctx != null) {
                instance = new SessionManager(ctx);
            }
        }
        return instance;
    }

    /**
     * Login with email and password via Supabase Auth.
     */
    public void login(String email, String password, AuthCallback callback) {
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        SupabaseClient.getInstance().getAuthService()
                .signIn("password", body)
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            AuthResponse auth = response.body();
                            saveTokens(auth.accessToken, auth.refreshToken);
                            if (auth.user != null) {
                                saveUserId(auth.user.id, auth.user.email);
                            }
                            // Load full profile from profiles table
                            loadUserProfile(new ProfileCallback() {
                                @Override
                                public void onLoaded(User user) {
                                    callback.onSuccess();
                                }

                                @Override
                                public void onError(String message) {
                                    // Login succeeded but profile load failed — still let user in
                                    callback.onSuccess();
                                }
                            });
                        } else {
                            String errorMsg = "Credenciales inválidas";
                            try {
                                if (response.errorBody() != null) {
                                    String errorBody = response.errorBody().string();
                                    if (errorBody.contains("Invalid login")) {
                                        errorMsg = "Email o contraseña incorrectos";
                                    } else if (errorBody.contains("Email not confirmed")) {
                                        errorMsg = "Confirma tu email antes de iniciar sesión";
                                    }
                                }
                            } catch (Exception ignored) {}
                            callback.onError(errorMsg);
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        callback.onError("Error de conexión. Verifica tu internet.");
                    }
                });
    }

    /**
     * Register a new user via Supabase Auth.
     * The handle_new_user() trigger creates the profile automatically.
     */
    public void register(String email, String password, String fullName, String username, AuthCallback callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        Map<String, String> options = new HashMap<>();
        if (fullName != null) options.put("full_name", fullName);
        if (username != null) options.put("username", username);
        body.put("data", options);

        SupabaseClient.getInstance().getAuthService()
                .signUp(body)
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            AuthResponse auth = response.body();
                            if (auth.accessToken != null) {
                                // Auto-confirmed: save tokens and proceed
                                saveTokens(auth.accessToken, auth.refreshToken);
                                if (auth.user != null) {
                                    saveUserId(auth.user.id, auth.user.email);
                                    currentUser = new User(
                                            auth.user.id,
                                            fullName != null ? fullName : "",
                                            username != null ? username : "",
                                            email,
                                            null
                                    );
                                }
                                callback.onSuccess();
                            } else {
                                // Email confirmation required
                                callback.onSuccess();
                            }
                        } else {
                            String errorMsg = "Error al registrar";
                            try {
                                if (response.errorBody() != null) {
                                    String errorBody = response.errorBody().string();
                                    if (errorBody.contains("already registered") || errorBody.contains("already been registered")) {
                                        errorMsg = "Este email ya está registrado";
                                    } else if (errorBody.contains("Password")) {
                                        errorMsg = "La contraseña debe tener al menos 6 caracteres";
                                    }
                                }
                            } catch (Exception ignored) {}
                            callback.onError(errorMsg);
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        callback.onError("Error de conexión. Verifica tu internet.");
                    }
                });
    }

    /**
     * Load the user's full profile from the profiles table.
     */
    public void loadUserProfile(ProfileCallback callback) {
        String userId = getUserId();
        if (userId == null) {
            callback.onError("No user ID");
            return;
        }

        SupabaseClient.getInstance().getDataService()
                .getProfile("eq." + userId, "*")
                .enqueue(new Callback<List<ProfileDto>>() {
                    @Override
                    public void onResponse(Call<List<ProfileDto>> call, Response<List<ProfileDto>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            ProfileDto profile = response.body().get(0);
                            currentUser = new User(
                                    profile.id,
                                    profile.fullName,
                                    profile.username,
                                    profile.email,
                                    profile.avatarUrl
                            );
                            currentUser.setPhone(profile.phone);
                            currentUser.setRole(profile.role);
                            callback.onLoaded(currentUser);
                        } else {
                            callback.onError("Error: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<ProfileDto>> call, Throwable t) {
                        callback.onError("Error al cargar perfil");
                    }
                });
    }

    public void updateProfile(String fullName, String username, String phone, AuthCallback callback) {
        String userId = getUserId();
        if (userId == null) {
            if (callback != null) callback.onError("No hay sesión activa");
            return;
        }

        Map<String, Object> update = new HashMap<>();
        if (fullName != null) update.put("full_name", fullName);
        if (username != null) update.put("username", username);
        if (phone != null) update.put("phone", phone);

        SupabaseClient.getInstance().getDataService()
                .updateProfile("eq." + userId, update)
                .enqueue(new Callback<List<ProfileDto>>() {
                    @Override
                    public void onResponse(Call<List<ProfileDto>> call, Response<List<ProfileDto>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            ProfileDto profile = response.body().get(0);
                            if (currentUser != null) {
                                currentUser.setName(profile.fullName);
                                currentUser.setUsername(profile.username);
                                currentUser.setPhone(profile.phone);
                            }
                            if (callback != null) callback.onSuccess();
                        } else {
                            if (callback != null) callback.onError("Error al actualizar perfil");
                        }
                    }

                    @Override
                    public void onFailure(Call<List<ProfileDto>> call, Throwable t) {
                        if (callback != null) callback.onError("Error de conexión");
                    }
                });
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void logout() {
        // Try to call Supabase logout (fire-and-forget)
        String token = getAccessToken();
        if (token != null) {
            try {
                SupabaseClient.getInstance().getAuthService()
                        .logout("Bearer " + token)
                        .enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {}
                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {}
                        });
            } catch (Exception ignored) {}
        }

        // Clear local state
        currentUser = null;
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

    public boolean isLoggedIn() {
        return getAccessToken() != null;
    }

    public String getAccessToken() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_ACCESS_TOKEN, null);
    }

    public String getUserId() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USER_ID, null);
    }

    private void saveTokens(String accessToken, String refreshToken) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(KEY_ACCESS_TOKEN, accessToken);
        if (refreshToken != null) {
            editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        }
        editor.apply();
    }

    private void saveUserId(String userId, String email) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        if (userId != null) editor.putString(KEY_USER_ID, userId);
        if (email != null) editor.putString(KEY_USER_EMAIL, email);
        editor.apply();
    }
}