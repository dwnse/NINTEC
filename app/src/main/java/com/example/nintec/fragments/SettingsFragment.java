package com.example.nintec.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.nintec.R;
import com.example.nintec.managers.SessionManager;
import com.example.nintec.models.User;

public class SettingsFragment extends Fragment {

    private ImageView btnBack;
    private EditText etName, etUsername, etEmail, etNewPass, etConfirmPass;
    private Button btnSave;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        btnBack = view.findViewById(R.id.btn_settings_back);
        etName = view.findViewById(R.id.et_settings_name);
        etUsername = view.findViewById(R.id.et_settings_username);
        etEmail = view.findViewById(R.id.et_settings_email);
        etNewPass = view.findViewById(R.id.et_settings_new_password);
        etConfirmPass = view.findViewById(R.id.et_settings_confirm_password);
        btnSave = view.findViewById(R.id.btn_settings_save);

        loadUserData();
        setupListeners();

        return view;
    }

    private void loadUserData() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            etName.setText(user.getName());
            etUsername.setText(user.getUsername());
            etEmail.setText(user.getEmail());
        } else {
            String email = SessionManager.getInstance().getUserEmail();
            if (email != null && !email.isEmpty()) {
                etEmail.setText(email);
                String prefix = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
                etName.setText(prefix);
                etUsername.setText(prefix);
            }
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        btnSave.setOnClickListener(v -> handleSave());
    }

    private void handleSave() {
        String name = etName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String newPass = etNewPass.getText().toString().trim();
        String confirmPass = etConfirmPass.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError(getString(R.string.error_required_field));
            return;
        }

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.settings_error_invalid_email));
            return;
        }

        if (!TextUtils.isEmpty(newPass)) {
            if (newPass.length() < 6) {
                etNewPass.setError("La contraseña debe tener al menos 6 caracteres");
                return;
            }
            if (!newPass.equals(confirmPass)) {
                etConfirmPass.setError(getString(R.string.settings_error_password_mismatch));
                return;
            }
        }

        btnSave.setEnabled(false);
        btnSave.setText("Guardando...");

        // Update local session immediately
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) {
            user = new User(SessionManager.getInstance().getUserId(), name, username, email, null);
        } else {
            user.setName(name);
            user.setUsername(username);
            user.setEmail(email);
        }
        SessionManager.getInstance().saveUserLocal(user);

        // Update password if requested
        if (!TextUtils.isEmpty(newPass)) {
            SessionManager.getInstance().updatePassword(newPass, new SessionManager.AuthCallback() {
                @Override
                public void onSuccess() {}
                @Override
                public void onError(String message) {}
            });
        }

        // Sync profile to Supabase
        SessionManager.getInstance().updateProfile(name, username, null, new SessionManager.AuthCallback() {
            @Override
            public void onSuccess() {
                finishAndPop();
            }

            @Override
            public void onError(String message) {
                // Local save already succeeded, don't trap the user
                finishAndPop();
            }
        });
    }

    private void finishAndPop() {
        if (isAdded() && getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Bundle result = new Bundle();
                result.putBoolean("updated", true);
                getParentFragmentManager().setFragmentResult("profile_updated", result);

                Toast.makeText(getContext(), R.string.settings_save_success, Toast.LENGTH_SHORT).show();
                getParentFragmentManager().popBackStack();
            });
        }
    }
}