package com.example.nintec.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.nintec.MainActivity;
import com.example.nintec.R;
import com.example.nintec.managers.SessionManager;

import com.example.nintec.network.SupabaseClient;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvGoToRegister;
    private View btnGoogle;
    private View btnIcloud;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SupabaseClient.init(this);
        SessionManager.init(this);

        // Check if user is already authenticated
        if (SessionManager.getInstance().isLoggedIn()) {
            goToMainActivity();
            return;
        }

        setContentView(R.layout.activity_login);

        // Initialize views
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvGoToRegister = findViewById(R.id.tv_go_to_register);
        btnGoogle = findViewById(R.id.btn_google);
        btnIcloud = findViewById(R.id.btn_icloud);

        // Main Login button listener
        btnLogin.setOnClickListener(v -> handleLogin());

        // Navigation to RegisterActivity
        tvGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Social login buttons feedback
        View.OnClickListener socialClickListener = v ->
                Toast.makeText(LoginActivity.this, "Inicio de sesión social disponible próximamente", Toast.LENGTH_SHORT).show();
        btnGoogle.setOnClickListener(socialClickListener);
        btnIcloud.setOnClickListener(socialClickListener);
    }

    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        boolean hasError = false;

        if (TextUtils.isEmpty(email)) {
            etEmail.setError(getString(R.string.error_email_empty));
            hasError = true;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError(getString(R.string.error_password_empty));
            hasError = true;
        }

        if (hasError) {
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Iniciando sesión...");

        SessionManager.getInstance().login(email, password, new SessionManager.AuthCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, "¡Bienvenido a NINTEC!", Toast.LENGTH_SHORT).show();
                    goToMainActivity();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText(R.string.login_button);
                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}