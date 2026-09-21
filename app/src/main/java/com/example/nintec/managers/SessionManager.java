package com.example.nintec.managers;

import com.example.nintec.R;
import com.example.nintec.models.User;

public class SessionManager {
    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {
        // Initial mock user for Stage 11
        currentUser = new User("1", "Usuario NINTECLP", "usuario_nintec", "usuario@ninteclp.com", R.mipmap.ic_launcher_foreground);
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void logout() {
        // In real app, clear persistent storage. Here just reset.
        currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }
}