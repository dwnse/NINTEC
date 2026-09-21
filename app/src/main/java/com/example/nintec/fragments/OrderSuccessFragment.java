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
                MainActivity activity = (MainActivity) getActivity();
                // Clear the backstack to get rid of Checkout/Confirm/Cart screens
                activity.getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                // Switch to Home tab using the BottomNavigationView
                BottomNavigationView nav = activity.findViewById(R.id.bottom_navigation);
                if (nav != null) {
                    nav.setSelectedItemId(R.id.nav_home);
                }
            }
        });

        btnHistory.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                MainActivity activity = (MainActivity) getActivity();
                activity.getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                
                // First switch to Profile tab
                BottomNavigationView nav = activity.findViewById(R.id.bottom_navigation);
                if (nav != null) {
                    nav.setSelectedItemId(R.id.nav_profile);
                }
                
                // Then open History fragment
                // Since we popped everything, we are now on ProfileFragment.
                // We can use a small delay or a post call to ensure the tab transition is done
                activity.findViewById(R.id.bottom_navigation).post(() -> {
                    // This is a bit hacky but works for this architecture
                    activity.findViewById(R.id.layout_option_history).performClick();
                });
            }
        });

        return view;
    }
}