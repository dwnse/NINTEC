package com.example.nintec.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nintec.MainActivity;
import com.example.nintec.R;
import com.example.nintec.adapters.ProductAdapter;
import com.example.nintec.managers.CartManager;
import com.example.nintec.models.Product;
import com.example.nintec.repositories.ProductRepository;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements ProductAdapter.OnProductClickListener, CartManager.CartChangeListener {

    private ImageView imgCart;
    private TextView tvCartBadge;
    private LinearLayout containerCategories;
    private RecyclerView rvOffers;
    private ProductAdapter productAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        imgCart = view.findViewById(R.id.img_cart);
        tvCartBadge = view.findViewById(R.id.tv_cart_badge);
        containerCategories = view.findViewById(R.id.container_categories);
        rvOffers = view.findViewById(R.id.rv_offers);

        imgCart.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openCart();
            }
        });

        setupCategories();
        setupOffersRecyclerView();

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
            } else {
                tvCartBadge.setVisibility(View.GONE);
            }
        }
    }

    private void setupCategories() {
        if (containerCategories == null) return;
        containerCategories.removeAllViews();

        String[] categories = {"Laptops", "Celulares", "Audio", "Accesorios", "Soporte"};

        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (String cat : categories) {
            View catView = inflater.inflate(R.layout.item_category, containerCategories, false);
            TextView tvName = catView.findViewById(R.id.tv_category_name);
            tvName.setText(cat);

            catView.setOnClickListener(v -> {
                // Future category filter logic entry point
            });

            containerCategories.addView(catView);
        }
    }

    private void setupOffersRecyclerView() {
        List<Product> repoProducts = ProductRepository.getInstance().getProducts();
        List<Product> homeOffers = new ArrayList<>();
        
        for (int i = 0; i < Math.min(4, repoProducts.size()); i++) {
            homeOffers.add(repoProducts.get(i));
        }

        productAdapter = new ProductAdapter(homeOffers, this);
        rvOffers.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvOffers.setAdapter(productAdapter);
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