package com.example.nintec.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.nintec.R;
import com.example.nintec.models.Category;
import com.example.nintec.repositories.ProductRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminCategoryEditFragment extends Fragment {

    private static final String ARG_CATEGORY_ID = "category_id";

    private String categoryId;
    private EditText edtName, edtSlug, edtIconName, edtSortOrder, edtImageUrl;
    private ImageView imgPreview;
    private CheckBox cbIsActive;

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null && getContext() != null) {
                    imgPreview.setVisibility(View.VISIBLE);
                    Glide.with(this).load(uri).into(imgPreview);
                    // Mock upload by setting a default placeholder URL if empty
                    if (edtImageUrl.getText().toString().trim().isEmpty()) {
                        edtImageUrl.setText("https://images.unsplash.com/photo-1511556532299-8f662fc26c06?w=500");
                    }
                }
            }
    );

    public static AdminCategoryEditFragment newInstance(@Nullable String categoryId) {
        AdminCategoryEditFragment fragment = new AdminCategoryEditFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY_ID, categoryId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getString(ARG_CATEGORY_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_category_edit, container, false);

        ImageView btnBack = view.findViewById(R.id.btn_edit_category_back);
        TextView txtTitle = view.findViewById(R.id.txt_edit_category_title);

        edtName = view.findViewById(R.id.edt_category_name);
        edtSlug = view.findViewById(R.id.edt_category_slug);
        edtIconName = view.findViewById(R.id.edt_category_icon_name);
        edtSortOrder = view.findViewById(R.id.edt_category_sort_order);
        edtImageUrl = view.findViewById(R.id.edt_category_image_url);
        imgPreview = view.findViewById(R.id.img_category_preview);

        cbIsActive = view.findViewById(R.id.cb_category_is_active);

        Button btnPickImage = view.findViewById(R.id.btn_pick_category_image);
        Button btnSave = view.findViewById(R.id.btn_save_category);

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        btnPickImage.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        if (categoryId != null) {
            txtTitle.setText("Editar Categoría");
            populateCategoryData();
        } else {
            txtTitle.setText("Crear Categoría");
        }

        btnSave.setOnClickListener(v -> saveCategory());

        return view;
    }

    private void populateCategoryData() {
        ProductRepository.getInstance().fetchCategories(new ProductRepository.CategoryListCallback() {
            @Override
            public void onSuccess(List<Category> categories) {
                if (!isAdded()) return;
                for (Category c : categories) {
                    if (c.getId().equals(categoryId)) {
                        edtName.setText(c.getName());
                        edtSlug.setText(c.getSlug());
                        edtIconName.setText(c.getIconName());
                        edtImageUrl.setText(c.getImageUrl());
                        
                        if (c.getImageUrl() != null && !c.getImageUrl().isEmpty()) {
                            imgPreview.setVisibility(View.VISIBLE);
                            Glide.with(AdminCategoryEditFragment.this).load(c.getImageUrl()).into(imgPreview);
                        }
                        break;
                    }
                }
            }

            @Override
            public void onError(String error) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Error al cargar datos: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveCategory() {
        String name = edtName.getText().toString().trim();
        String slug = edtSlug.getText().toString().trim();

        if (name.isEmpty() || slug.isEmpty()) {
            Toast.makeText(getContext(), "Por favor, completa los campos obligatorios (Nombre, Slug)", Toast.LENGTH_SHORT).show();
            return;
        }

        int sortOrder = 0;
        String sortOrderStr = edtSortOrder.getText().toString().trim();
        if (!sortOrderStr.isEmpty()) {
            try {
                sortOrder = Integer.parseInt(sortOrderStr);
            } catch (NumberFormatException ignored) {}
        }

        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("slug", slug);
        body.put("icon_name", edtIconName.getText().toString().trim());
        body.put("sort_order", sortOrder);
        body.put("image_url", edtImageUrl.getText().toString().trim());
        body.put("is_active", cbIsActive.isChecked());

        if (categoryId != null) {
            ProductRepository.getInstance().updateCategory(categoryId, body, new ProductRepository.CategoryCallback() {
                @Override
                public void onSuccess(Category category) {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Categoría actualizada con éxito", Toast.LENGTH_SHORT).show();
                        getParentFragmentManager().popBackStack();
                    }
                }

                @Override
                public void onError(String error) {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Error al actualizar: " + error, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            ProductRepository.getInstance().createCategory(body, new ProductRepository.CategoryCallback() {
                @Override
                public void onSuccess(Category category) {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Categoría creada con éxito", Toast.LENGTH_SHORT).show();
                        getParentFragmentManager().popBackStack();
                    }
                }

                @Override
                public void onError(String error) {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Error al crear: " + error, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}