package com.example.nintec.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.nintec.MainActivity;
import com.example.nintec.R;

public class OrderSuccessFragment extends Fragment {

    private View viewPulseBg;
    private ImageView imgSuccessIcon;
    private TextView tvTitle;
    private TextView tvDesc;
    private Button btnHome;
    private Button btnHistory;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_success, container, false);

        viewPulseBg = view.findViewById(R.id.view_pulse_bg);
        imgSuccessIcon = view.findViewById(R.id.img_success_icon);
        tvTitle = view.findViewById(R.id.tv_success_title);
        tvDesc = view.findViewById(R.id.tv_success_desc);
        btnHome = view.findViewById(R.id.btn_success_home);
        btnHistory = view.findViewById(R.id.btn_success_history);

        setupButtons();
        startCelebrationAnimation();

        return view;
    }

    private void startCelebrationAnimation() {
        // Initial invisible & displaced state
        if (imgSuccessIcon != null) {
            imgSuccessIcon.setScaleX(0f);
            imgSuccessIcon.setScaleY(0f);
            imgSuccessIcon.setAlpha(0f);
        }
        if (viewPulseBg != null) {
            viewPulseBg.setScaleX(0.2f);
            viewPulseBg.setScaleY(0.2f);
            viewPulseBg.setAlpha(0f);
        }
        if (tvTitle != null) {
            tvTitle.setAlpha(0f);
            tvTitle.setTranslationY(40f);
        }
        if (tvDesc != null) {
            tvDesc.setAlpha(0f);
            tvDesc.setTranslationY(40f);
        }
        if (btnHome != null) {
            btnHome.setAlpha(0f);
            btnHome.setTranslationY(40f);
        }
        if (btnHistory != null) {
            btnHistory.setAlpha(0f);
            btnHistory.setTranslationY(40f);
        }

        // Animate Checkmark & Pulse Background
        if (imgSuccessIcon != null) {
            imgSuccessIcon.animate()
                    .scaleX(1f).scaleY(1f)
                    .alpha(1f)
                    .setDuration(500)
                    .setInterpolator(new OvershootInterpolator(2.2f))
                    .start();
        }

        if (viewPulseBg != null) {
            viewPulseBg.animate()
                    .scaleX(1.15f).scaleY(1.15f)
                    .alpha(0.85f)
                    .setDuration(550)
                    .setInterpolator(new DecelerateInterpolator())
                    .withEndAction(() -> {
                        if (viewPulseBg != null) {
                            viewPulseBg.animate()
                                    .scaleX(1.0f).scaleY(1.0f)
                                    .setDuration(300)
                                    .start();
                        }
                    })
                    .start();
        }

        // Staggered Text Animations
        if (tvTitle != null) {
            tvTitle.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(200)
                    .setDuration(350)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }

        if (tvDesc != null) {
            tvDesc.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(320)
                    .setDuration(350)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }

        if (btnHome != null) {
            btnHome.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(420)
                    .setDuration(350)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }

        if (btnHistory != null) {
            btnHistory.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(500)
                    .setDuration(350)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }
    }

    private void setupButtons() {
        if (btnHome != null) {
            btnHome.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).resetToTab(R.id.nav_home);
                }
            });
        }

        if (btnHistory != null) {
            btnHistory.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    MainActivity activity = (MainActivity) getActivity();
                    activity.resetToTab(R.id.nav_profile);

                    v.postDelayed(() -> {
                        View historyOption = activity.findViewById(R.id.layout_option_history);
                        if (historyOption != null) {
                            historyOption.performClick();
                        }
                    }, 150);
                }
            });
        }
    }
}