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

public class RegisterActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;
    private Button btnRegister;
    private TextView tvGoToLogin;
    private View btnGoogle;
    private View btnIcloud;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.example.nintec.network.SupabaseClient.init(this);
        SessionManager.init(this);
        setContentView(R.layout.activity_register);

        // Initialize views
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnRegister = findViewById(R.id.btn_register);
        tvGoToLogin = findViewById(R.id.tv_go_to_login);
        btnGoogle = findViewById(R.id.btn_google);
        btnIcloud = findViewById(R.id.btn_icloud);

        // Main Register submit button listener
        btnRegister.setOnClickListener(v -> handleRegister());

        // Navigation back to LoginActivity
        tvGoToLogin.setOnClickListener(v -> finish());

        // Social login buttons placeholders
        View.OnClickListener socialClickListener = v ->
                Toast.makeText(RegisterActivity.this, "Registro social disponible próximamente", Toast.LENGTH_SHORT).show();
        btnGoogle.setOnClickListener(socialClickListener);
        btnIcloud.setOnClickListener(socialClickListener);
    }

    private void handleRegister() {
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
        } else if (password.length() < 6) {
            etPassword.setError("La contraseña debe tener al menos 6 caracteres");
            hasError = true;
        }

        if (hasError) {
            return;
        }

        btnRegister.setEnabled(false);
        btnRegister.setText("Creando cuenta...");

        // Generate default display name and username from email prefix
        String username = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        String fullName = username;

        SessionManager.getInstance().register(email, password, fullName, username, new SessionManager.AuthCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(RegisterActivity.this, "¡Cuenta creada exitosamente!", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    btnRegister.setEnabled(true);
                    btnRegister.setText(R.string.register_button);
                    Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}