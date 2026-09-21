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
        setContentView(R.layout.activity_login);

        // Initialize views
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvGoToRegister = findViewById(R.id.tv_go_to_register);
        btnGoogle = findViewById(R.id.btn_google);
        btnIcloud = findViewById(R.id.btn_icloud);

        // Main Login button listener
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogin();
            }
        });

        // Navigation to RegisterActivity
        tvGoToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                // Do not finish LoginActivity so the user can easily return via back button or link
            }
        });

        // Social login buttons visual feedback / click placeholder
        View.OnClickListener socialClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "Inicio de sesión social disponible próximamente", Toast.LENGTH_SHORT).show();
            }
        };
        btnGoogle.setOnClickListener(socialClickListener);
        btnIcloud.setOnClickListener(socialClickListener);
    }

    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Simple validation checks
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

        // TODO: reemplazar por autenticación real (Firebase, API o Base de Datos)
        // Navegación temporal para probar la aplicación y sus fragmentos
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("Usuario", email);
        startActivity(intent);
        finish(); // Finaliza LoginActivity para que no quede en la pila tras ingresar a la app principal
    }
}