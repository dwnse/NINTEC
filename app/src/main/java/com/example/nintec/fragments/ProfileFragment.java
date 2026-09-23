package com.example.nintec.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.nintec.MainActivity;
import com.example.nintec.R;
import com.example.nintec.activities.LoginActivity;
import com.example.nintec.managers.CartManager;
import com.example.nintec.managers.SessionManager;
import com.example.nintec.models.User;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ProfileFragment extends Fragment implements SessionManager.UserChangeListener {

    private TextView tvName, tvUsername, tvEmail, tvRole;
    private LinearLayout layoutHistory, layoutSettings, layoutLogout, layoutAdmin;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvName = view.findViewById(R.id.tv_profile_name);
        tvUsername = view.findViewById(R.id.tv_profile_username);
        tvEmail = view.findViewById(R.id.tv_profile_email);
        tvRole = view.findViewById(R.id.tv_profile_role);
        layoutHistory = view.findViewById(R.id.layout_option_history);
        layoutSettings = view.findViewById(R.id.layout_option_settings);
        layoutLogout = view.findViewById(R.id.layout_option_logout);
        layoutAdmin = view.findViewById(R.id.layout_option_admin);

        setupListeners();

        // Listen for settings save result when popped from backstack
        getParentFragmentManager().setFragmentResultListener("profile_updated", getViewLifecycleOwner(), (requestKey, bundle) -> {
            updateUI();
        });

        // Register for real-time user change notifications
        SessionManager.getInstance().addUserChangeListener(this);

        updateUI();

        // Synchronize full profile in background
        SessionManager.getInstance().loadUserProfile(null);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        SessionManager.getInstance().removeUserChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            updateUI();
            SessionManager.getInstance().loadUserProfile(null);
        }
    }

    @Override
    public void onUserChanged(User user) {
        if (isAdded() && getActivity() != null) {
            getActivity().runOnUiThread(this::updateUI);
        }
    }

    private void updateUI() {
        User user = SessionManager.getInstance().getCurrentUser();
        String fullName = null;
        String username = null;
        String email = null;
        String role = "customer";

        if (user != null) {
            fullName = user.getName();
            username = user.getUsername();
            email = user.getEmail();
            role = user.getRole() != null ? user.getRole() : "customer";
        } else {
            email = SessionManager.getInstance().getUserEmail();
            if (email != null && !email.isEmpty()) {
                String prefix = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
                fullName = prefix;
                username = prefix;
            }
        }

        // 1. Display Full Name
        if (tvName != null) {
            if (fullName != null && !fullName.trim().isEmpty()) {
                tvName.setText(fullName);
            } else if (username != null && !username.trim().isEmpty()) {
                tvName.setText(username);
            } else {
                tvName.setText("Usuario NINTEC");
            }
        }

        // 2. Display @username
        if (tvUsername != null) {
            if (username != null && !username.trim().isEmpty()) {
                tvUsername.setText("@" + username.replace("@", ""));
                tvUsername.setVisibility(View.VISIBLE);
            } else {
                tvUsername.setVisibility(View.GONE);
            }
        }

        // 3. Display Email
        if (tvEmail != null) {
            if (email != null && !email.trim().isEmpty()) {
                tvEmail.setText(email);
                tvEmail.setVisibility(View.VISIBLE);
            } else {
                tvEmail.setVisibility(View.GONE);
            }
        }

        // 4. Display Role and Admin Navigation
        if (tvRole != null) {
            if (role.equalsIgnoreCase("super_admin")) {
                tvRole.setText("Super Administrador");
                if (layoutAdmin != null) layoutAdmin.setVisibility(View.VISIBLE);
            } else if (role.equalsIgnoreCase("admin")) {
                tvRole.setText("Administrador");
                if (layoutAdmin != null) layoutAdmin.setVisibility(View.VISIBLE);
            } else {
                tvRole.setText("Cliente");
                if (layoutAdmin != null) layoutAdmin.setVisibility(View.GONE);
            }
        }
    }

    private void setupListeners() {
        layoutHistory.setOnClickListener(v -> openOrderHistory());
        layoutSettings.setOnClickListener(v -> openSettings());
        layoutLogout.setOnClickListener(v -> showLogoutConfirmation());
        
        if (layoutAdmin != null) {
            layoutAdmin.setOnClickListener(v -> openAdminPanel());
        }
    }

    private void openAdminPanel() {
        AdminFragment adminFragment = new AdminFragment();
        getParentFragmentManager().beginTransaction()
                .add(R.id.fragment_container, adminFragment, "ADMIN")
                .addToBackStack("ADMIN_TRANS")
                .commit();
    }

    private void openOrderHistory() {
        OrderHistoryFragment historyFragment = new OrderHistoryFragment();
        getParentFragmentManager().beginTransaction()
                .add(R.id.fragment_container, historyFragment, "HISTORY")
                .addToBackStack("HISTORY_TRANS")
                .commit();
    }

    private void openSettings() {
        SettingsFragment settingsFragment = new SettingsFragment();
        getParentFragmentManager().beginTransaction()
                .add(R.id.fragment_container, settingsFragment, "SETTINGS")
                .addToBackStack("SETTINGS_TRANS")
                .commit();
    }

    private void showLogoutConfirmation() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.logout_dialog_title)
                .setMessage(R.string.logout_dialog_message)
                .setNegativeButton(R.string.btn_cancel, null)
                .setPositiveButton(R.string.btn_logout, (dialog, which) -> performLogout())
                .show();
    }

    private void performLogout() {
        CartManager.getInstance().clear();
        SessionManager.getInstance().logout();

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}
