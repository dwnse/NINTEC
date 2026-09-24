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

import com.bumptech.glide.Glide;
import com.example.nintec.MainActivity;
import com.example.nintec.R;
import com.example.nintec.managers.CartManager;
import com.example.nintec.models.Product;
import com.example.nintec.repositories.ProductRepository;
import java.util.Map;

public class ProductDetailFragment extends Fragment implements CartManager.CartChangeListener {

    private static final String ARG_PRODUCT_ID = "product_id";

    private ImageView btnBack, btnShare, imgProduct, btnCart;
    private TextView tvName, tvStockStatus, tvPrice, tvOldPrice, tvDescription, tvQtyCounter, btnQtyMinus, btnQtyPlus, tvCartBadge, tvBottomTotalPrice;
    private Button btnAddToCart, btnBuyNow;
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
        btnCart = view.findViewById(R.id.btn_detail_cart);
        tvCartBadge = view.findViewById(R.id.tv_detail_cart_badge);
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
        btnBuyNow = view.findViewById(R.id.btn_detail_buy_now);
        tvBottomTotalPrice = view.findViewById(R.id.tv_bottom_total_price);
        layoutSpecsContainer = view.findViewById(R.id.layout_detail_specs_container);

        if (getArguments() != null) {
            String productId = getArguments().getString(ARG_PRODUCT_ID);
            currentProduct = ProductRepository.getInstance().getProductById(productId);
        }

        hydrateProductDetails();
        setupActionListeners();

        CartManager.getInstance().addListener(this);

        return view;
    }

    @Override
    public void onDestroyView() {
        CartManager.getInstance().removeListener(this);
        super.onDestroyView();
    }

    @Override
    public void onCartChanged(int totalItemCount) {
        if (tvCartBadge != null) {
            if (totalItemCount > 0) {
                tvCartBadge.setText(String.valueOf(totalItemCount));
                tvCartBadge.setVisibility(View.VISIBLE);
                tvCartBadge.setScaleX(0.5f);
                tvCartBadge.setScaleY(0.5f);
                tvCartBadge.animate().scaleX(1.25f).scaleY(1.25f).setDuration(140)
                        .setInterpolator(new android.view.animation.OvershootInterpolator(2.5f))
                        .withEndAction(() -> tvCartBadge.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start())
                        .start();
            } else {
                tvCartBadge.setVisibility(View.GONE);
            }
        }
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

        try {
            if (currentProduct.getImageUrl() != null && !currentProduct.getImageUrl().isEmpty()) {
                Glide.with(this)
                        .load(currentProduct.getImageUrl())
                        .placeholder(R.mipmap.ic_launcher_foreground)
                        .error(R.mipmap.ic_launcher_foreground)
                        .into(imgProduct);
            } else {
                imgProduct.setImageResource(R.mipmap.ic_launcher_foreground);
            }
        } catch (Exception e) {
            imgProduct.setImageResource(R.mipmap.ic_launcher_foreground);
        }

        tvDescription.setText(ProductRepository.getInstance().getDescriptionById(currentProduct.getId()));

        if (currentProduct.getStock() <= 0) {
            tvStockStatus.setText("Agotado");
            tvStockStatus.setBackgroundResource(R.drawable.bg_badge_error);
            tvStockStatus.setTextColor(getResources().getColor(R.color.error));
            btnAddToCart.setEnabled(false);
            btnAddToCart.setText("Agotado");
            if (btnBuyNow != null) {
                btnBuyNow.setEnabled(false);
                btnBuyNow.setText("Agotado");
            }
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

        updateBottomTotal();

        // Fetch dynamic specifications
        ProductRepository.getInstance().fetchProductSpecifications(currentProduct.getId(), new ProductRepository.SpecificationCallback() {
            @Override
            public void onSuccess(Map<String, String> specs) {
                if (isAdded()) {
                    populateSpecifications(specs);
                }
            }

            @Override
            public void onError(String error) {
                // Fallback to default if any
            }
        });
    }

    private void updateBottomTotal() {
        if (currentProduct != null && tvBottomTotalPrice != null) {
            tvBottomTotalPrice.setText(Product.formatPrice(currentProduct.getPriceValue() * selectedQuantity));
        }
    }

    private void populateSpecifications(Map<String, String> specs) {
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

        btnCart.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openCart();
            }
        });

        btnQtyMinus.setOnClickListener(v -> {
            if (currentProduct.getStock() <= 0 || selectedQuantity <= 1) return;
            selectedQuantity--;
            tvQtyCounter.setText(String.valueOf(selectedQuantity));
            updateBottomTotal();
        });

        btnQtyPlus.setOnClickListener(v -> {
            if (currentProduct.getStock() <= 0 || selectedQuantity >= currentProduct.getStock()) {
                Toast.makeText(getContext(), "Máximo stock disponible alcanzado", Toast.LENGTH_SHORT).show();
                return;
            }
            selectedQuantity++;
            tvQtyCounter.setText(String.valueOf(selectedQuantity));
            updateBottomTotal();
        });

        btnAddToCart.setOnClickListener(v -> {
            if (currentProduct != null && selectedQuantity > 0) {
                // Interactive micro-animation feedback on button
                btnAddToCart.animate()
                        .scaleX(0.92f).scaleY(0.92f)
                        .setDuration(90)
                        .withEndAction(() -> {
                            btnAddToCart.animate().scaleX(1.0f).scaleY(1.0f).setDuration(110).start();
                        }).start();

                // Bounce animation on top cart icon
                if (btnCart != null) {
                    btnCart.animate().scaleX(1.3f).scaleY(1.3f).setDuration(140)
                            .setInterpolator(new android.view.animation.OvershootInterpolator(2.5f))
                            .withEndAction(() -> btnCart.animate().scaleX(1.0f).scaleY(1.0f).setDuration(110).start())
                            .start();
                }

                CartManager.getInstance().addProduct(currentProduct, selectedQuantity);
                Toast.makeText(getContext(), "¡" + currentProduct.getName() + " (" + selectedQuantity + ") agregado al carrito!", Toast.LENGTH_SHORT).show();
            }
        });

        if (btnBuyNow != null) {
            btnBuyNow.setOnClickListener(v -> {
                if (currentProduct != null && selectedQuantity > 0) {
                    btnBuyNow.animate()
                            .scaleX(0.92f).scaleY(0.92f)
                            .setDuration(90)
                            .withEndAction(() -> btnBuyNow.animate().scaleX(1.0f).scaleY(1.0f).setDuration(110).start())
                            .start();

                    // Direct purchase: Add to cart and immediately open payment / checkout screen
                    CartManager.getInstance().addProduct(currentProduct, selectedQuantity);
                    PaymentMethodFragment paymentFragment = new PaymentMethodFragment();
                    getParentFragmentManager().beginTransaction()
                            .add(R.id.fragment_container, paymentFragment, "PAYMENT")
                            .addToBackStack("PAYMENT_TRANS")
                            .commit();
                }
            });
        }
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
