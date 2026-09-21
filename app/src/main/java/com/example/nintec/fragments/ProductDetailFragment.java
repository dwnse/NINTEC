package com.example.nintec.fragments;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.nintec.MainActivity;
import com.example.nintec.R;
import com.example.nintec.managers.CartManager;
import com.example.nintec.models.Product;
import com.example.nintec.repositories.ProductRepository;
import java.util.Map;

public class ProductDetailFragment extends Fragment {

    private static final String ARG_PRODUCT_ID = "product_id";

    private ImageView btnBack, btnShare, imgProduct;
    private TextView tvName, tvStockStatus, tvPrice, tvOldPrice, tvDescription, tvQtyCounter, btnQtyMinus, btnQtyPlus;
    private Button btnAddToCart;
    private LinearLayout layoutSpecsContainer;

    private Product currentProduct;
    private int selectedQuantity = 1;

    public static ProductDetailFragment newInstance(String productId) {
        ProductDetailFragment fragment = new ProductDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PRODUCT_ID, productId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_detail, container, false);

        btnBack = view.findViewById(R.id.btn_detail_back);
        btnShare = view.findViewById(R.id.btn_detail_share);
        imgProduct = view.findViewById(R.id.img_detail_product);
        tvName = view.findViewById(R.id.tv_detail_name);
        tvStockStatus = view.findViewById(R.id.tv_detail_stock_status);
        tvPrice = view.findViewById(R.id.tv_detail_price);
        tvOldPrice = view.findViewById(R.id.tv_detail_old_price);
        tvDescription = view.findViewById(R.id.tv_detail_description);
        tvQtyCounter = view.findViewById(R.id.tv_qty_counter);
        btnQtyMinus = view.findViewById(R.id.btn_qty_minus);
        btnQtyPlus = view.findViewById(R.id.btn_qty_plus);
        btnAddToCart = view.findViewById(R.id.btn_detail_add_to_cart);
        layoutSpecsContainer = view.findViewById(R.id.layout_detail_specs_container);

        if (getArguments() != null) {
            String productId = getArguments().getString(ARG_PRODUCT_ID);
            currentProduct = ProductRepository.getInstance().getProductById(productId);
        }

        hydrateProductDetails();
        setupActionListeners();

        return view;
    }

    private void hydrateProductDetails() {
        if (currentProduct == null) return;

        tvName.setText(currentProduct.getName());
        tvPrice.setText(currentProduct.getPrice());

        if (currentProduct.getOldPrice() != null) {
            tvOldPrice.setText(currentProduct.getOldPrice());
            tvOldPrice.setVisibility(View.VISIBLE);
            tvOldPrice.setPaintFlags(tvOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            tvOldPrice.setVisibility(View.GONE);
        }

        if (currentProduct.getImageResource() != 0) {
            imgProduct.setImageResource(currentProduct.getImageResource());
        }

        tvDescription.setText(ProductRepository.getInstance().getDescriptionById(currentProduct.getId()));

        if (currentProduct.getStock() <= 0) {
            tvStockStatus.setText("Agotado");
            tvStockStatus.setBackgroundResource(R.drawable.bg_badge_error);
            tvStockStatus.setTextColor(getResources().getColor(R.color.error));
            btnAddToCart.setEnabled(false);
            btnAddToCart.setText("Agotado");
            btnQtyPlus.setEnabled(false);
            btnQtyMinus.setEnabled(false);
            selectedQuantity = 0;
            tvQtyCounter.setText("0");
        } else if (currentProduct.getStock() <= 3) {
            tvStockStatus.setText("Pocas Unidades (" + currentProduct.getStock() + ")");
            tvStockStatus.setBackgroundResource(R.drawable.bg_badge_error);
            tvStockStatus.setTextColor(getResources().getColor(R.color.error));
        } else {
            tvStockStatus.setText("En Stock");
            tvStockStatus.setBackgroundResource(R.drawable.bg_badge_success);
            tvStockStatus.setTextColor(getResources().getColor(R.color.success));
        }

        Map<String, String> specs = ProductRepository.getInstance().getSpecificationsById(currentProduct.getId());
        layoutSpecsContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (Map.Entry<String, String> entry : specs.entrySet()) {
            View row = inflater.inflate(R.layout.item_catalog_category, layoutSpecsContainer, false);
            TextView tvSpec = row.findViewById(R.id.tv_catalog_category_item);
            tvSpec.setText(entry.getKey() + ": " + entry.getValue());
            tvSpec.setBackground(null);
            tvSpec.setPadding(0, 4, 0, 4);
            layoutSpecsContainer.addView(row);
        }
    }

    private void setupActionListeners() {
        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        btnShare.setOnClickListener(v -> handleShareProduct());

        btnQtyMinus.setOnClickListener(v -> {
            if (currentProduct.getStock() <= 0 || selectedQuantity <= 1) return;
            selectedQuantity--;
            tvQtyCounter.setText(String.valueOf(selectedQuantity));
        });

        btnQtyPlus.setOnClickListener(v -> {
            if (currentProduct.getStock() <= 0 || selectedQuantity >= currentProduct.getStock()) {
                Toast.makeText(getContext(), "Máximo stock disponible alcanzado", Toast.LENGTH_SHORT).show();
                return;
            }
            selectedQuantity++;
            tvQtyCounter.setText(String.valueOf(selectedQuantity));
        });

        btnAddToCart.setOnClickListener(v -> {
            if (currentProduct != null && selectedQuantity > 0) {
                CartManager.getInstance().addProduct(currentProduct, selectedQuantity);
                Toast.makeText(getContext(), "¡" + currentProduct.getName() + " (" + selectedQuantity + ") agregado al carrito!", Toast.LENGTH_SHORT).show();
                
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).openCart();
                }
            }
        });
    }

    private void handleShareProduct() {
        if (currentProduct == null) return;
        String shareText = "Revisa este producto en NINTECLP:\n" +
                currentProduct.getName() + "\nPrecio: " + currentProduct.getPrice();

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, "Compartir producto");
        startActivity(shareIntent);
    }
}