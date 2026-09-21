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

public class ProfileFragment extends Fragment {

    private TextView tvName;
    private LinearLayout layoutHistory, layoutSettings, layoutLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvName = view.findViewById(R.id.tv_profile_name);
        layoutHistory = view.findViewById(R.id.layout_option_history);
        layoutSettings = view.findViewById(R.id.layout_option_settings);
        layoutLogout = view.findViewById(R.id.layout_option_logout);

        setupListeners();
        updateUI();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateUI() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            tvName.setText(user.getName());
        }
    }

    private void setupListeners() {
        layoutHistory.setOnClickListener(v -> {
            openOrderHistory();
        });

        layoutSettings.setOnClickListener(v -> {
            openSettings();
        });

        layoutLogout.setOnClickListener(v -> {
            showLogoutConfirmation();
        });
    }

    private void openOrderHistory() {
        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            // activity.hideBottomNavigation();
            
            OrderHistoryFragment historyFragment = new OrderHistoryFragment();
            getParentFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, historyFragment, "HISTORY")
                    .addToBackStack("HISTORY_TRANS")
                    .commit();
        }
    }

    private void openSettings() {
        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            // activity.hideBottomNavigation();
            
            SettingsFragment settingsFragment = new SettingsFragment();
            getParentFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, settingsFragment, "SETTINGS")
                    .addToBackStack("SETTINGS_TRANS")
                    .commit();
        }
    }

    private void showLogoutConfirmation() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.logout_dialog_title)
                .setMessage(R.string.logout_dialog_message)
                .setNegativeButton(R.string.btn_cancel, null)
                .setPositiveButton(R.string.btn_logout, (dialog, which) -> {
                    performLogout();
                })
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