package com.example.nintec.fragments;

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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nintec.MainActivity;
import com.example.nintec.R;
import com.example.nintec.adapters.CartAdapter;
import com.example.nintec.adapters.ProductAdapter;
import com.example.nintec.managers.CartManager;
import com.example.nintec.models.CartItem;
import com.example.nintec.models.Product;
import com.example.nintec.repositories.ProductRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartFragment extends Fragment implements CartAdapter.OnCartItemActionListener, ProductAdapter.OnProductClickListener {

    private RecyclerView rvCartItems, rvRecommendations;
    private LinearLayout layoutFilled, layoutEmpty, layoutBottomSummary;
    private TextView tvSubtotal, tvTotal;
    private Button btnEmptyShopping, btnProceed;

    private CartAdapter cartAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        ImageView btnBack = view.findViewById(R.id.btn_cart_back);
        rvCartItems = view.findViewById(R.id.rv_cart_items);
        rvRecommendations = view.findViewById(R.id.rv_cart_recommendations);
        layoutFilled = view.findViewById(R.id.layout_filled_cart);
        layoutEmpty = view.findViewById(R.id.layout_empty_cart);
        layoutBottomSummary = view.findViewById(R.id.layout_bottom_cart_summary);
        tvSubtotal = view.findViewById(R.id.tv_cart_subtotal);
        tvTotal = view.findViewById(R.id.tv_cart_total);
        btnEmptyShopping = view.findViewById(R.id.btn_cart_empty_shopping);
        btnProceed = view.findViewById(R.id.btn_cart_proceed);

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        
        btnEmptyShopping.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        btnProceed.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                MainActivity activity = (MainActivity) getActivity();
                PaymentMethodFragment paymentFragment = new PaymentMethodFragment();
                getParentFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, paymentFragment, "PAYMENT")
                        .addToBackStack("PAYMENT_TRANS")
                        .commit();
            }
        });

        setupRecyclerViews();
        updateUI();

        return view;
    }

    private void setupRecyclerViews() {
        cartAdapter = new CartAdapter(new ArrayList<>(CartManager.getInstance().getItems()), this);
        rvCartItems.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCartItems.setAdapter(cartAdapter);

        List<Product> recommended = new ArrayList<>();
        List<Product> all = ProductRepository.getInstance().getProducts();
        for (int i = 0; i < Math.min(2, all.size()); i++) {
            recommended.add(all.get(i));
        }
        ProductAdapter recommendationAdapter = new ProductAdapter(recommended, this);
        rvRecommendations.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvRecommendations.setAdapter(recommendationAdapter);
    }

    private void updateUI() {
        List<CartItem> items = CartManager.getInstance().getItems();
        
        if (items.isEmpty()) {
            layoutFilled.setVisibility(View.GONE);
            layoutBottomSummary.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            layoutFilled.setVisibility(View.VISIBLE);
            layoutBottomSummary.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
            
            cartAdapter.updateList(new ArrayList<>(items));
            refreshTotals();
        }
    }

    private void refreshTotals() {
        double totalNum = CartManager.getInstance().getTotalAmount();
        String formatted = String.format(Locale.getDefault(), "Bs %.2f", totalNum);
        tvSubtotal.setText(formatted);
        tvTotal.setText(formatted);
    }

    @Override
    public void onIncrease(CartItem item) {
        if (item.getQuantity() < item.getProduct().getStock()) {
            CartManager.getInstance().updateQuantity(item, item.getQuantity() + 1);
            updateUI();
        } else {
            String msg = getString(R.string.max_stock_reached, item.getProduct().getStock());
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDecrease(CartItem item) {
        if (item.getQuantity() > 1) {
            CartManager.getInstance().updateQuantity(item, item.getQuantity() - 1);
            updateUI();
        }
    }

    @Override
    public void onRemove(CartItem item) {
        CartManager.getInstance().removeItem(item);
        updateUI();
        Toast.makeText(getContext(), R.string.item_removed, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProductClick(CartItem item) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).openProductDetail(item.getProduct().getId());
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
        CartManager.getInstance().addProduct(product, 1);
        updateUI();
        String msg = getString(R.string.add_to_cart_success, product.getName());
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }
}