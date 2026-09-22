package com.example.nintec;

import android.app.Application;
import android.content.Context;
import com.example.nintec.managers.SessionManager;
import com.example.nintec.network.SupabaseClient;

public class NintecApp extends Application {
    private static NintecApp instance;

    public static NintecApp getInstance() {
        return instance;
    }

    public static Context getAppContext() {
        return instance != null ? instance.getApplicationContext() : null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        SupabaseClient.init(this);
        SessionManager.init(this);
    }
}
