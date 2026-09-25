package com.example.nintec.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.example.nintec.R;

public class AdminFragment extends Fragment {

    private static final String ADMIN_WEB_URL = "http://localhost:3000";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin, container, false);

        ImageView btnBack = view.findViewById(R.id.btn_admin_back);
        View btnProducts = view.findViewById(R.id.btn_manage_products);
        View btnCategories = view.findViewById(R.id.btn_manage_categories);
        View btnBranches = view.findViewById(R.id.btn_manage_branches);
        View btnBanners = view.findViewById(R.id.btn_manage_banners);
        View btnOffers = view.findViewById(R.id.btn_manage_offers);
        View btnWebPanel = view.findViewById(R.id.btn_open_web_panel);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (isAdded() && getParentFragmentManager() != null) {
                    getParentFragmentManager().popBackStack();
                }
            });
        }

        if (btnProducts != null) {
            btnProducts.setOnClickListener(v -> openManageProducts());
        }

        if (btnCategories != null) {
            btnCategories.setOnClickListener(v -> openManageCategories());
        }

        if (btnBranches != null) {
            btnBranches.setOnClickListener(v -> showFeatureWebRedirectDialog(
                    "Gestión de Sucursales",
                    "La creación, edición de nombres, coordenadas GPS, ciudades y horarios de atención de las sucursales se gestiona en tiempo real desde la consola Web NINTEC."
            ));
        }

        if (btnBanners != null) {
            btnBanners.setOnClickListener(v -> showFeatureWebRedirectDialog(
                    "Gestión de Hero Banners",
                    "La administración, carga de imágenes y rotación de banners promocionales de la pantalla de inicio se gestiona en tiempo real desde la consola Web NINTEC."
            ));
        }

        if (btnOffers != null) {
            btnOffers.setOnClickListener(v -> openManageOffers());
        }

        if (btnWebPanel != null) {
            btnWebPanel.setOnClickListener(v -> launchWebAdminPanel());
        }

        return view;
    }

    private void openManageProducts() {
        if (!isAdded()) return;
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new AdminProductsFragment())
                .addToBackStack(null)
                .commit();
    }

    private void openManageCategories() {
        if (!isAdded()) return;
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new AdminCategoriesFragment())
                .addToBackStack(null)
                .commit();
    }



    private void openManageOffers() {
        if (!isAdded()) return;
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new CatalogFragment())
                .addToBackStack(null)
                .commit();
    }

    private void launchWebAdminPanel() {
        Context context = getContext();
        if (context == null) return;

        try {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                ClipData clip = ClipData.newPlainText("NINTEC Web Admin", ADMIN_WEB_URL);
                clipboard.setPrimaryClip(clip);
            }

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ADMIN_WEB_URL));
            startActivity(browserIntent);
            Toast.makeText(context, "Abriendo panel web (" + ADMIN_WEB_URL + ")", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context, "Enlace copiado al portapapeles: " + ADMIN_WEB_URL, Toast.LENGTH_LONG).show();
        }
    }

    private void showFeatureWebRedirectDialog(String title, String message) {
        if (getContext() == null) return;
        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Abrir Panel Web", (dialog, which) -> launchWebAdminPanel())
                .setNegativeButton("Cerrar", null)
                .show();
    }
}

