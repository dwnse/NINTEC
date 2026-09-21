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
        setContentView(R.layout.activity_register);

        // Initialize views
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnRegister = findViewById(R.id.btn_register);
        tvGoToLogin = findViewById(R.id.tv_go_to_login);
        btnGoogle = findViewById(R.id.btn_google);
        btnIcloud = findViewById(R.id.btn_icloud);

        // Main Register submit button listener
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleRegister();
            }
        });

        // Navigation back to LoginActivity
        tvGoToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // finish() returns cleanly to the existing LoginActivity without accumulating stack instances
                finish();
            }
        });

        // Social login buttons placeholders
        View.OnClickListener socialClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(RegisterActivity.this, "Registro social disponible próximamente", Toast.LENGTH_SHORT).show();
            }
        };
        btnGoogle.setOnClickListener(socialClickListener);
        btnIcloud.setOnClickListener(socialClickListener);
    }

    private void handleRegister() {
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

        // TODO: reemplazar por persistencia/registro real (Firebase, API o Base de Datos)
        Toast.makeText(this, "¡Registro completado con éxito!", Toast.LENGTH_LONG).show();

        // Tras registrarse con éxito, entra a la app principal temporalmente
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("Usuario", email);
        startActivity(intent);
        
        // Finaliza tanto RegisterActivity como la pila anterior para iniciar sesión limpia
        finish();
    }
}