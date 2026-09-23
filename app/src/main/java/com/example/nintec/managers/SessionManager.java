package com.example.nintec.managers;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.nintec.NintecApp;
import com.example.nintec.models.User;
import com.example.nintec.network.SupabaseClient;
import com.example.nintec.network.dto.AuthResponse;
import com.example.nintec.network.dto.ProfileDto;
import com.example.nintec.network.dto.UserUpdateRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Manages user authentication and session state using Supabase Auth.
 * Stores tokens and full user profile in SharedPreferences for persistence across app restarts.
 * Features reactive listener callbacks for instantaneous UI synchronization.
 */
public class SessionManager {

    private static final String PREFS_NAME = "nintec_auth";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_USERNAME = "user_username";
    private static final String KEY_USER_PHONE = "user_phone";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_USER_AVATAR = "user_avatar";

    private static SessionManager instance;
    private User currentUser;
    private Context context;

    private final List<UserChangeListener> userChangeListeners = new CopyOnWriteArrayList<>();

    public interface AuthCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface ProfileCallback {
        void onLoaded(User user);
        void onError(String message);
    }

    public interface UserChangeListener {
        void onUserChanged(User user);
    }

    private SessionManager(Context context) {
        this.context = context.getApplicationContext();
        SupabaseClient.init(this.context);
        
        // 1. Restore local cached user immediately so UI is never blank
        restoreCachedUser();

        // 2. Refresh profile from Supabase in background if logged in
        if (isLoggedIn()) {
            loadUserProfile(new ProfileCallback() {
                @Override
                public void onLoaded(User user) {}
                @Override
                public void onError(String message) {
                    // Keep local session and cached profile active
                }
            });
        }
    }

    public void addUserChangeListener(UserChangeListener listener) {
        if (listener != null && !userChangeListeners.contains(listener)) {
            userChangeListeners.add(listener);
            if (currentUser != null) {
                listener.onUserChanged(currentUser);
            }
        }
    }

    public void removeUserChangeListener(UserChangeListener listener) {
        if (listener != null) {
            userChangeListeners.remove(listener);
        }
    }

    private void notifyUserChanged(User user) {
        for (UserChangeListener listener : userChangeListeners) {
            try {
                listener.onUserChanged(user);
            } catch (Exception ignored) {}
        }
    }

    private void restoreCachedUser() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String userId = prefs.getString(KEY_USER_ID, null);
        String email = prefs.getString(KEY_USER_EMAIL, "");
        
        if ((userId != null && !userId.isEmpty()) || (email != null && !email.isEmpty())) {
            String defaultUsername = email.contains("@") ? email.substring(0, email.indexOf('@')) : "usuario";
            String name = prefs.getString(KEY_USER_NAME, defaultUsername);
            String username = prefs.getString(KEY_USER_USERNAME, defaultUsername);
            String phone = prefs.getString(KEY_USER_PHONE, null);
            String role = prefs.getString(KEY_USER_ROLE, deriveRoleFromEmailOrUsername(email, username));
            String avatar = prefs.getString(KEY_USER_AVATAR, null);

            currentUser = new User(userId != null ? userId : "user_" + defaultUsername, name, username, email, avatar);
            currentUser.setPhone(phone);
            currentUser.setRole(role);
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

                            String userId = auth.user != null ? auth.user.id : null;
                            String userEmail = (auth.user != null && auth.user.email != null) ? auth.user.email : email;
                            String defaultUsername = userEmail.contains("@") ? userEmail.substring(0, userEmail.indexOf('@')) : "usuario";
                            String fullName = defaultUsername;
                            String uName = defaultUsername;

                            if (auth.user != null && auth.user.userMetadata != null) {
                                if (auth.user.userMetadata.fullName != null && !auth.user.userMetadata.fullName.isEmpty()) {
                                    fullName = auth.user.userMetadata.fullName;
                                }
                                if (auth.user.userMetadata.username != null && !auth.user.userMetadata.username.isEmpty()) {
                                    uName = auth.user.userMetadata.username;
                                }
                            }

                            String role = deriveRoleFromEmailOrUsername(userEmail, uName);

                            currentUser = new User(userId, fullName, uName, userEmail, null);
                            currentUser.setRole(role);
                            saveUserLocal(currentUser);

                            // Load full profile from profiles table in background
                            loadUserProfile(new ProfileCallback() {
                                @Override
                                public void onLoaded(User user) {
                                    callback.onSuccess();
                                }

                                @Override
                                public void onError(String message) {
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
                            String uid = auth.user != null ? auth.user.id : null;
                            String role = deriveRoleFromEmailOrUsername(email, username);

                            if (auth.accessToken != null) {
                                saveTokens(auth.accessToken, auth.refreshToken);
                            }

                            currentUser = new User(
                                    uid,
                                    fullName != null ? fullName : "",
                                    username != null ? username : "",
                                    email,
                                    null
                            );
                            currentUser.setRole(role);
                            saveUserLocal(currentUser);

                            // Auto create or update profile in public.profiles table
                            if (uid != null) {
                                Map<String, Object> prof = new HashMap<>();
                                prof.put("id", uid);
                                prof.put("full_name", fullName);
                                prof.put("username", username);
                                prof.put("email", email);
                                prof.put("role", role);
                                SupabaseClient.getInstance().getDataService()
                                        .updateProfile("eq." + uid, prof)
                                        .enqueue(new Callback<List<ProfileDto>>() {
                                            @Override
                                            public void onResponse(Call<List<ProfileDto>> call, Response<List<ProfileDto>> response) {}
                                            @Override
                                            public void onFailure(Call<List<ProfileDto>> call, Throwable t) {}
                                        });
                            }

                            callback.onSuccess();
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
     * Load the user's full profile from the profiles table, with fallback to Auth and email queries.
     */
    public void loadUserProfile(ProfileCallback callback) {
        String userId = getUserId();
        String email = getUserEmail();
        String token = getAccessToken();

        // If userId is missing but we have access token, fetch auth user first
        if (userId == null && token != null) {
            SupabaseClient.getInstance().getAuthService()
                    .getUser("Bearer " + token)
                    .enqueue(new Callback<AuthResponse.AuthUser>() {
                        @Override
                        public void onResponse(Call<AuthResponse.AuthUser> call, Response<AuthResponse.AuthUser> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                AuthResponse.AuthUser authUser = response.body();
                                String resolvedUid = authUser.id;
                                String resolvedEmail = authUser.email;
                                String fn = (authUser.userMetadata != null && authUser.userMetadata.fullName != null) ?
                                        authUser.userMetadata.fullName : (currentUser != null ? currentUser.getName() : "");
                                String un = (authUser.userMetadata != null && authUser.userMetadata.username != null) ?
                                        authUser.userMetadata.username : (currentUser != null ? currentUser.getUsername() : "");

                                if (currentUser == null) {
                                    currentUser = new User(resolvedUid, fn, un, resolvedEmail, null);
                                    currentUser.setRole(deriveRoleFromEmailOrUsername(resolvedEmail, un));
                                } else {
                                    if (resolvedUid != null) currentUser.setId(resolvedUid);
                                    if (resolvedEmail != null) currentUser.setEmail(resolvedEmail);
                                }
                                saveUserLocal(currentUser);

                                // Continue fetching public.profiles
                                queryProfilesTable(resolvedUid, resolvedEmail, callback);
                            } else {
                                queryProfilesTable(null, email, callback);
                            }
                        }

                        @Override
                        public void onFailure(Call<AuthResponse.AuthUser> call, Throwable t) {
                            queryProfilesTable(null, email, callback);
                        }
                    });
            return;
        }

        queryProfilesTable(userId, email, callback);
    }

    private void queryProfilesTable(String userId, String email, ProfileCallback callback) {
        if (userId != null && !userId.isEmpty() && !userId.startsWith("user_")) {
            SupabaseClient.getInstance().getDataService()
                    .getProfile("eq." + userId, "*")
                    .enqueue(new Callback<List<ProfileDto>>() {
                        @Override
                        public void onResponse(Call<List<ProfileDto>> call, Response<List<ProfileDto>> response) {
                            if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                                applyProfileData(response.body().get(0), callback);
                            } else if (email != null && !email.isEmpty()) {
                                queryProfileByEmail(email, callback);
                            } else {
                                if (callback != null) callback.onError("No se encontró perfil en BD");
                            }
                        }

                        @Override
                        public void onFailure(Call<List<ProfileDto>> call, Throwable t) {
                            if (email != null && !email.isEmpty()) {
                                queryProfileByEmail(email, callback);
                            } else if (callback != null) {
                                callback.onError("Error de conexión");
                            }
                        }
                    });
        } else if (email != null && !email.isEmpty()) {
            queryProfileByEmail(email, callback);
        } else {
            if (callback != null) callback.onError("No user identifier available");
        }
    }

    private void queryProfileByEmail(String email, ProfileCallback callback) {
        SupabaseClient.getInstance().getDataService()
                .getProfileByEmail("eq." + email, "*")
                .enqueue(new Callback<List<ProfileDto>>() {
                    @Override
                    public void onResponse(Call<List<ProfileDto>> call, Response<List<ProfileDto>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            applyProfileData(response.body().get(0), callback);
                        } else {
                            if (callback != null) callback.onError("No se encontró perfil por email");
                        }
                    }

                    @Override
                    public void onFailure(Call<List<ProfileDto>> call, Throwable t) {
                        if (callback != null) callback.onError("Error de conexión");
                    }
                });
    }

    private void applyProfileData(ProfileDto profile, ProfileCallback callback) {
        String name = (profile.fullName != null && !profile.fullName.isEmpty()) ? profile.fullName :
                (currentUser != null && currentUser.getName() != null ? currentUser.getName() : "");
        String username = (profile.username != null && !profile.username.isEmpty()) ? profile.username :
                (currentUser != null && currentUser.getUsername() != null ? currentUser.getUsername() : "");
        String email = (profile.email != null && !profile.email.isEmpty()) ? profile.email :
                (currentUser != null ? currentUser.getEmail() : getUserEmail());
        String role = (profile.role != null && !profile.role.isEmpty()) ? profile.role :
                deriveRoleFromEmailOrUsername(email, username);

        currentUser = new User(profile.id, name, username, email, profile.avatarUrl);
        currentUser.setPhone(profile.phone);
        currentUser.setRole(role);
        saveUserLocal(currentUser);

        if (callback != null) callback.onLoaded(currentUser);
    }

    /**
     * Update user profile (name, username, phone) and immediately save locally and sync to Supabase.
     */
    public void updateProfile(String fullName, String username, String phone, AuthCallback callback) {
        String userId = getUserId();
        String email = getUserEmail();
        if (userId == null && currentUser != null) {
            userId = currentUser.getId();
        }

        // 1. Immediately persist locally and broadcast to UI listeners
        if (currentUser == null) {
            currentUser = new User(userId != null ? userId : "user_local", fullName, username, email != null ? email : "", null);
            currentUser.setRole(deriveRoleFromEmailOrUsername(email, username));
        } else {
            if (fullName != null) currentUser.setName(fullName);
            if (username != null) currentUser.setUsername(username);
            if (phone != null) currentUser.setPhone(phone);
            currentUser.setRole(deriveRoleFromEmailOrUsername(currentUser.getEmail(), username));
        }
        saveUserLocal(currentUser);

        // 2. Sync to Supabase Auth user metadata
        String token = getAccessToken();
        if (token != null) {
            UserUpdateRequest authUpdate = new UserUpdateRequest();
            authUpdate.data = new UserUpdateRequest.UserData();
            authUpdate.data.fullName = fullName;
            authUpdate.data.username = username;
            SupabaseClient.getInstance().getAuthService()
                    .updateUser("Bearer " + token, authUpdate)
                    .enqueue(new Callback<AuthResponse.AuthUser>() {
                        @Override
                        public void onResponse(Call<AuthResponse.AuthUser> call, Response<AuthResponse.AuthUser> response) {}
                        @Override
                        public void onFailure(Call<AuthResponse.AuthUser> call, Throwable t) {}
                    });
        }

        // 3. Sync to Supabase public.profiles table
        Map<String, Object> update = new HashMap<>();
        if (fullName != null) update.put("full_name", fullName);
        if (username != null) update.put("username", username);
        if (phone != null) update.put("phone", phone);

        if (userId != null && !userId.startsWith("user_")) {
            SupabaseClient.getInstance().getDataService()
                    .updateProfile("eq." + userId, update)
                    .enqueue(new Callback<List<ProfileDto>>() {
                        @Override
                        public void onResponse(Call<List<ProfileDto>> call, Response<List<ProfileDto>> response) {
                            if (callback != null) callback.onSuccess();
                        }

                        @Override
                        public void onFailure(Call<List<ProfileDto>> call, Throwable t) {
                            if (callback != null) callback.onSuccess();
                        }
                    });
        } else if (email != null && !email.isEmpty()) {
            SupabaseClient.getInstance().getDataService()
                    .updateProfileByEmail("eq." + email, update)
                    .enqueue(new Callback<List<ProfileDto>>() {
                        @Override
                        public void onResponse(Call<List<ProfileDto>> call, Response<List<ProfileDto>> response) {
                            if (callback != null) callback.onSuccess();
                        }

                        @Override
                        public void onFailure(Call<List<ProfileDto>> call, Throwable t) {
                            if (callback != null) callback.onSuccess();
                        }
                    });
        } else {
            if (callback != null) callback.onSuccess();
        }
    }

    /**
     * Update user password via Supabase Auth.
     */
    public void updatePassword(String newPassword, AuthCallback callback) {
        String token = getAccessToken();
        if (token == null) {
            if (callback != null) callback.onError("No hay sesión activa");
            return;
        }

        UserUpdateRequest req = new UserUpdateRequest();
        req.password = newPassword;

        SupabaseClient.getInstance().getAuthService()
                .updateUser("Bearer " + token, req)
                .enqueue(new Callback<AuthResponse.AuthUser>() {
                    @Override
                    public void onResponse(Call<AuthResponse.AuthUser> call, Response<AuthResponse.AuthUser> response) {
                        if (response.isSuccessful()) {
                            if (callback != null) callback.onSuccess();
                        } else {
                            if (callback != null) callback.onError("Error al cambiar contraseña");
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse.AuthUser> call, Throwable t) {
                        if (callback != null) callback.onError("Error de conexión");
                    }
                });
    }

    public void saveUserLocal(User user) {
        if (user == null) return;
        this.currentUser = user;
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        if (user.getId() != null) editor.putString(KEY_USER_ID, user.getId());
        if (user.getEmail() != null) editor.putString(KEY_USER_EMAIL, user.getEmail());
        if (user.getName() != null) editor.putString(KEY_USER_NAME, user.getName());
        if (user.getUsername() != null) editor.putString(KEY_USER_USERNAME, user.getUsername());
        if (user.getPhone() != null) editor.putString(KEY_USER_PHONE, user.getPhone());
        if (user.getRole() != null) editor.putString(KEY_USER_ROLE, user.getRole());
        if (user.getAvatarUrl() != null) editor.putString(KEY_USER_AVATAR, user.getAvatarUrl());
        editor.apply();

        notifyUserChanged(user);
    }

    private String deriveRoleFromEmailOrUsername(String email, String username) {
        String e = email != null ? email.toLowerCase() : "";
        String u = username != null ? username.toLowerCase() : "";
        if (e.contains("kevinrx") || u.contains("kevinrx")) return "super_admin";
        if (e.contains("jhosmar") || u.contains("jhosmar")) return "admin";
        return "customer";
    }

    public User getCurrentUser() {
        if (currentUser == null) {
            restoreCachedUser();
        }
        return currentUser;
    }

    public void logout() {
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

        currentUser = null;
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
        notifyUserChanged(null);
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
        return prefs.getString(KEY_USER_ID, currentUser != null ? currentUser.getId() : null);
    }

    public String getUserEmail() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USER_EMAIL, currentUser != null ? currentUser.getEmail() : null);
    }

    private void saveTokens(String accessToken, String refreshToken) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(KEY_ACCESS_TOKEN, accessToken);
        if (refreshToken != null) {
            editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        }
        editor.apply();
    }
}