package com.example.nintec.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.nintec.R;

public class AdminFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin, container, false);

        ImageView btnBack = view.findViewById(R.id.btn_admin_back);
        Button btnProducts = view.findViewById(R.id.btn_manage_products);
        Button btnCategories = view.findViewById(R.id.btn_manage_categories);

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        
        btnProducts.setOnClickListener(v -> openManageProducts());
        btnCategories.setOnClickListener(v -> openManageCategories());

        return view;
    }

    private void openManageProducts() {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new AdminProductsFragment())
                .addToBackStack(null)
                .commit();
    }

    private void openManageCategories() {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new AdminCategoriesFragment())
                .addToBackStack(null)
                .commit();
    }
}
