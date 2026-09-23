package com.example.nintec.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nintec.MainActivity;
import com.example.nintec.R;
import com.example.nintec.adapters.ProductAdapter;
import com.example.nintec.managers.CartManager;
import com.example.nintec.models.Category;
import com.example.nintec.models.Product;
import com.example.nintec.repositories.ProductRepository;

import java.util.ArrayList;
import java.util.List;

public class CatalogFragment extends Fragment implements ProductAdapter.OnProductClickListener, CartManager.CartChangeListener {

    private EditText etSearch;
    private ImageView imgSearchClear;
    private LinearLayout containerCategories;
    private RecyclerView rvProducts;
    private TextView tvEmptyState;
    private TextView tvCartBadge;

    private ProductAdapter productAdapter;
    private List<Product> allProductsList = new ArrayList<>();
    private List<Product> filteredProductsList = new ArrayList<>();
    private List<Category> categoriesList = new ArrayList<>();

    private String currentSelectedCategoryId = "all"; // Use ID instead of name
    private String currentSearchQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_catalog, container, false);

        ImageView imgCart = view.findViewById(R.id.img_catalog_cart);
        tvCartBadge = view.findViewById(R.id.tv_catalog_cart_badge);
        etSearch = view.findViewById(R.id.et_catalog_search);
        imgSearchClear = view.findViewById(R.id.img_catalog_search_clear);
        containerCategories = view.findViewById(R.id.container_catalog_categories);
        rvProducts = view.findViewById(R.id.rv_catalog_products);
        tvEmptyState = view.findViewById(R.id.tv_catalog_empty_state);

        imgCart.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openCart();
            }
        });

        if (imgSearchClear != null) {
            imgSearchClear.setOnClickListener(v -> etSearch.setText(""));
        }

        setupRecyclerView();
        setupSearchInputFilter();
        loadCatalogData();

        CartManager.getInstance().addListener(this);

        return view;
    }

    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(filteredProductsList, this);
        rvProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvProducts.setAdapter(productAdapter);
    }

    private void loadCatalogData() {
        // 1. Fetch Categories
        ProductRepository.getInstance().fetchCategories(new ProductRepository.CategoryListCallback() {
            @Override
            public void onSuccess(List<Category> categories) {
                if (isAdded()) {
                    categoriesList = categories;
                    setupCategoriesFilterRow();
                }
            }
            @Override
            public void onError(String error) {}
        });

        // 2. Fetch Products
        ProductRepository.getInstance().fetchProducts(new ProductRepository.ProductListCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                if (isAdded()) {
                    allProductsList = products;
                    applyCombinedFilters();
                }
            }
            @Override
            public void onError(String error) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupCategoriesFilterRow() {
        if (containerCategories == null) return;
        containerCategories.removeAllViews();
        
        // Add "All" option
        addCategoryView("all", "Todos");

        for (Category cat : categoriesList) {
            addCategoryView(cat.getId(), cat.getName());
        }
    }

    private void addCategoryView(String id, String name) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View itemView = inflater.inflate(R.layout.item_catalog_category, containerCategories, false);
        TextView tvCat = itemView.findViewById(R.id.tv_catalog_category_item);
        tvCat.setText(name);

        updateCategoryItemStyle(tvCat, id.equals(currentSelectedCategoryId));

        itemView.setOnClickListener(v -> {
            currentSelectedCategoryId = id;
            
            for (int i = 0; i < containerCategories.getChildCount(); i++) {
                View child = containerCategories.getChildAt(i);
                TextView childTv = child.findViewById(R.id.tv_catalog_category_item);
                // Tag or position? I'll use text match or better, a custom state
                // Since I don't want to overcomplicate, I'll just refresh the whole row or find by ID
            }
            // Simple approach: redraw row for styles
            setupCategoriesFilterRow();
            applyCombinedFilters();
        });

        containerCategories.addView(itemView);
    }

    private void updateCategoryItemStyle(TextView tv, boolean isSelected) {
        if (getContext() == null) return;
        if (isSelected) {
            tv.setBackgroundResource(R.drawable.bg_button_primary);
            tv.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        } else {
            tv.setBackgroundResource(R.drawable.bg_badge_new);
            tv.setTextColor(ContextCompat.getColor(getContext(), R.color.text_primary));
        }
    }

    private void setupSearchInputFilter() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim();
                if (imgSearchClear != null) {
                    imgSearchClear.setVisibility(currentSearchQuery.isEmpty() ? View.GONE : View.VISIBLE);
                }
                applyCombinedFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void applyCombinedFilters() {
        List<Product> matches = new ArrayList<>();

        for (Product product : allProductsList) {
            boolean categoryMatch = currentSelectedCategoryId.equals("all") || 
                                   (product.getCategoryId() != null && product.getCategoryId().equals(currentSelectedCategoryId));
            boolean searchMatch = currentSearchQuery.isEmpty() || 
                                 product.getName().toLowerCase().contains(currentSearchQuery.toLowerCase());

            if (categoryMatch && searchMatch) {
                matches.add(product);
            }
        }

        filteredProductsList.clear();
        filteredProductsList.addAll(matches);
        productAdapter.updateList(filteredProductsList);

        if (filteredProductsList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvProducts.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvProducts.setVisibility(View.VISIBLE);
        }
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
            } else {
                tvCartBadge.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onProductClick(Product product) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).openProductDetail(product.getId());
        }
    }

    @Override
    public void onBuyClick(Product product) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).openProductDetail(product.getId());
        }
    }
}
