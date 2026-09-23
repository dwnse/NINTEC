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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nintec.MainActivity;
import com.example.nintec.R;
import com.example.nintec.adapters.ProductAdapter;
import com.example.nintec.managers.CartManager;
import com.example.nintec.models.Banner;
import com.example.nintec.models.Category;
import com.example.nintec.models.Product;
import com.example.nintec.repositories.ProductRepository;

import java.util.List;

public class HomeFragment extends Fragment implements ProductAdapter.OnProductClickListener, CartManager.CartChangeListener {

    private ImageView imgCart, imgSearchClear;
    private TextView tvCartBadge;
    private EditText etSearch;
    private LinearLayout containerCategories;
    private RecyclerView rvOffers;
    private ProductAdapter productAdapter;

    // Banner views
    private TextView tvBannerTitle, tvBannerDesc, tvBannerSub;
    private ImageView imgBanner;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        imgCart = view.findViewById(R.id.img_cart);
        tvCartBadge = view.findViewById(R.id.tv_cart_badge);
        containerCategories = view.findViewById(R.id.container_categories);
        rvOffers = view.findViewById(R.id.rv_offers);
        etSearch = view.findViewById(R.id.et_home_search);
        imgSearchClear = view.findViewById(R.id.img_home_search_clear);

        // Banner views initialization
        tvBannerTitle = view.findViewById(R.id.tv_home_banner_title);
        tvBannerDesc = view.findViewById(R.id.tv_home_banner_desc);
        tvBannerSub = view.findViewById(R.id.tv_home_banner_sub);
        imgBanner = view.findViewById(R.id.img_home_banner);
        
        imgCart.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openCart();
            }
        });

        setupSearchLogic();
        loadHomeData();

        CartManager.getInstance().addListener(this);

        return view;
    }

    private void loadHomeData() {
        // 1. Fetch Banners
        ProductRepository.getInstance().fetchBanners(new ProductRepository.BannerListCallback() {
            @Override
            public void onSuccess(List<Banner> banners) {
                if (isAdded() && !banners.isEmpty()) {
                    updateBannerUI(banners.get(0));
                }
            }
            @Override
            public void onError(String error) {}
        });

        // 2. Fetch Categories
        ProductRepository.getInstance().fetchCategories(new ProductRepository.CategoryListCallback() {
            @Override
            public void onSuccess(List<Category> categories) {
                if (isAdded()) {
                    populateCategories(categories);
                }
            }
            @Override
            public void onError(String error) {}
        });

        // 3. Fetch Featured Products
        ProductRepository.getInstance().fetchFeaturedProducts(new ProductRepository.ProductListCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                if (isAdded()) {
                    setupOffersRecyclerView(products);
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

    private void updateBannerUI(Banner banner) {
        if (tvBannerTitle != null) tvBannerTitle.setText(banner.getTitle());
        if (tvBannerDesc != null) tvBannerDesc.setText(banner.getSubtitle());
        if (tvBannerSub != null) tvBannerSub.setVisibility(View.GONE); // Hide sub if not needed
        
        if (imgBanner != null && banner.getImageUrl() != null) {
            Glide.with(this).load(banner.getImageUrl()).into(imgBanner);
            imgBanner.setAlpha(1.0f); // Make it fully visible if it's a real image
        }
    }

    private void populateCategories(List<Category> categories) {
        if (containerCategories == null) return;
        containerCategories.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (Category cat : categories) {
            View catView = inflater.inflate(R.layout.item_category, containerCategories, false);
            TextView tvName = catView.findViewById(R.id.tv_category_name);
            ImageView imgIcon = catView.findViewById(R.id.img_category_icon);
            
            tvName.setText(cat.getName());
            
            if (cat.getImageUrl() != null && !cat.getImageUrl().isEmpty()) {
                Glide.with(this).load(cat.getImageUrl()).into(imgIcon);
            } else if (cat.getIconName() != null) {
                int resId = getResources().getIdentifier(cat.getIconName(), "drawable", getContext().getPackageName());
                if (resId != 0) imgIcon.setImageResource(resId);
            }

            catView.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    // TODO: Filter Catalog by category ID
                }
            });

            containerCategories.addView(catView);
        }
    }

    private void setupOffersRecyclerView(List<Product> products) {
        productAdapter = new ProductAdapter(products, this);
        rvOffers.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvOffers.setAdapter(productAdapter);
    }

    private void setupSearchLogic() {
        if (etSearch == null || imgSearchClear == null) return;

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                imgSearchClear.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        imgSearchClear.setOnClickListener(v -> etSearch.setText(""));
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
