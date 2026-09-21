package com.example.nintec.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.example.nintec.MainActivity;
import com.example.nintec.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class OrderSuccessFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_success, container, false);

        Button btnHome = view.findViewById(R.id.btn_success_home);
        Button btnHistory = view.findViewById(R.id.btn_success_history);

        btnHome.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).resetToTab(R.id.nav_home);
            }
        });

        btnHistory.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                MainActivity activity = (MainActivity) getActivity();
                activity.resetToTab(R.id.nav_profile);
                
                // Navigate to history from profile
                v.post(() -> {
                    View historyOption = activity.findViewById(R.id.layout_option_history);
                    if (historyOption != null) {
                        historyOption.performClick();
                    }
                });
            }
        });

        return view;
    }
}