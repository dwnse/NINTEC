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
import com.example.nintec.models.Product;
import com.example.nintec.repositories.ProductRepository;

import java.util.HashMap;
import java.util.Map;

public class AdminProductEditFragment extends Fragment {

    private static final String ARG_PRODUCT_ID = "product_id";

    private String productId;
    private EditText edtName, edtSlug, edtDescription, edtPrice, edtOldPrice, edtSku, edtBrand, edtCategoryId, edtImageUrl;
    private ImageView imgPreview;
    private CheckBox cbIsNew, cbIsFeatured, cbIsActive;

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null && getContext() != null) {
                    imgPreview.setVisibility(View.VISIBLE);
                    Glide.with(this).load(uri).into(imgPreview);
                    // Mock upload by setting a default placeholder URL if empty
                    if (edtImageUrl.getText().toString().trim().isEmpty()) {
                        edtImageUrl.setText("https://images.unsplash.com/photo-1531297484001-80022131f5a1?w=500");
                    }
                }
            }
    );

    public static AdminProductEditFragment newInstance(@Nullable String productId) {
        AdminProductEditFragment fragment = new AdminProductEditFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PRODUCT_ID, productId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            productId = getArguments().getString(ARG_PRODUCT_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_product_edit, container, false);

        ImageView btnBack = view.findViewById(R.id.btn_edit_product_back);
        TextView txtTitle = view.findViewById(R.id.txt_edit_product_title);

        edtName = view.findViewById(R.id.edt_product_name);
        edtSlug = view.findViewById(R.id.edt_product_slug);
        edtDescription = view.findViewById(R.id.edt_product_description);
        edtPrice = view.findViewById(R.id.edt_product_price);
        edtOldPrice = view.findViewById(R.id.edt_product_old_price);
        edtSku = view.findViewById(R.id.edt_product_sku);
        edtBrand = view.findViewById(R.id.edt_product_brand);
        edtCategoryId = view.findViewById(R.id.edt_product_category_id);
        edtImageUrl = view.findViewById(R.id.edt_product_image_url);
        imgPreview = view.findViewById(R.id.img_product_preview);

        cbIsNew = view.findViewById(R.id.cb_product_is_new);
        cbIsFeatured = view.findViewById(R.id.cb_product_is_featured);
        cbIsActive = view.findViewById(R.id.cb_product_is_active);

        Button btnPickImage = view.findViewById(R.id.btn_pick_product_image);
        Button btnSave = view.findViewById(R.id.btn_save_product);

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        btnPickImage.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        if (productId != null) {
            txtTitle.setText("Editar Producto");
            populateProductData();
        } else {
            txtTitle.setText("Crear Producto");
        }

        btnSave.setOnClickListener(v -> saveProduct());

        return view;
    }

    private void populateProductData() {
        Product p = ProductRepository.getInstance().getProductById(productId);
        if (p != null) {
            edtName.setText(p.getName());
            edtSlug.setText(p.getSlug());
            edtDescription.setText(ProductRepository.getInstance().getDescriptionById(productId));
            edtPrice.setText(String.valueOf(p.getPriceValue()));
            if (p.getOldPriceValue() != null) {
                edtOldPrice.setText(String.valueOf(p.getOldPriceValue()));
            }
            edtBrand.setText(p.getBrand());
            edtCategoryId.setText(p.getCategoryId());
            edtImageUrl.setText(p.getImageUrl());
            cbIsNew.setChecked(p.isNew());
            
            // Check if there's an image URL to preview
            if (p.getImageUrl() != null && !p.getImageUrl().isEmpty()) {
                imgPreview.setVisibility(View.VISIBLE);
                Glide.with(this).load(p.getImageUrl()).into(imgPreview);
            }
        }
    }

    private void saveProduct() {
        String name = edtName.getText().toString().trim();
        String slug = edtSlug.getText().toString().trim();
        String priceStr = edtPrice.getText().toString().trim();

        if (name.isEmpty() || slug.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(getContext(), "Por favor, completa los campos obligatorios (Nombre, Slug, Precio)", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Precio inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("slug", slug);
        body.put("description", edtDescription.getText().toString().trim());
        body.put("price", price);

        String oldPriceStr = edtOldPrice.getText().toString().trim();
        if (!oldPriceStr.isEmpty()) {
            try {
                body.put("old_price", Double.parseDouble(oldPriceStr));
            } catch (NumberFormatException ignored) {}
        } else {
            body.put("old_price", null);
        }

        body.put("sku", edtSku.getText().toString().trim());
        body.put("brand", edtBrand.getText().toString().trim());
        
        String catId = edtCategoryId.getText().toString().trim();
        if (!catId.isEmpty()) {
            body.put("category_id", catId);
        }

        body.put("image_url", edtImageUrl.getText().toString().trim());
        body.put("is_new", cbIsNew.isChecked());
        body.put("is_featured", cbIsFeatured.isChecked());
        body.put("is_active", cbIsActive.isChecked());

        if (productId != null) {
            ProductRepository.getInstance().updateProduct(productId, body, new ProductRepository.ProductCallback() {
                @Override
                public void onSuccess(Product product) {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Producto actualizado con éxito", Toast.LENGTH_SHORT).show();
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
            ProductRepository.getInstance().createProduct(body, new ProductRepository.ProductCallback() {
                @Override
                public void onSuccess(Product product) {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Producto creado con éxito", Toast.LENGTH_SHORT).show();
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