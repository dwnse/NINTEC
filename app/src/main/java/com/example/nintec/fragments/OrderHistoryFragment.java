package com.example.nintec.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nintec.MainActivity;
import com.example.nintec.R;
import com.example.nintec.adapters.OrderAdapter;
import com.example.nintec.models.Order;
import com.example.nintec.repositories.OrderRepository;
import java.util.List;

public class OrderHistoryFragment extends Fragment implements OrderAdapter.OnOrderClickListener {

    private RecyclerView rvHistory;
    private LinearLayout layoutEmpty;
    private OrderAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_history, container, false);

        ImageView btnBack = view.findViewById(R.id.btn_history_back);
        rvHistory = view.findViewById(R.id.rv_order_history);
        layoutEmpty = view.findViewById(R.id.layout_history_empty);
        Button btnExplore = view.findViewById(R.id.btn_history_explore);

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        
        btnExplore.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                MainActivity activity = (MainActivity) getActivity();
                // Navigate to Catalog
                activity.findViewById(R.id.nav_catalog).performClick();
            }
        });

        setupRecyclerView();

        return view;
    }

    private void setupRecyclerView() {
        List<Order> orders = OrderRepository.getInstance().getOrders();
        
        if (orders.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvHistory.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvHistory.setVisibility(View.VISIBLE);
            adapter = new OrderAdapter(orders, this);
            rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
            rvHistory.setAdapter(adapter);
        }

        OrderRepository.getInstance().fetchOrders(new OrderRepository.OrderListCallback() {
            @Override
            public void onSuccess(List<Order> freshOrders) {
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (freshOrders.isEmpty()) {
                            layoutEmpty.setVisibility(View.VISIBLE);
                            rvHistory.setVisibility(View.GONE);
                        } else {
                            layoutEmpty.setVisibility(View.GONE);
                            rvHistory.setVisibility(View.VISIBLE);
                            if (adapter == null) {
                                adapter = new OrderAdapter(freshOrders, OrderHistoryFragment.this);
                                rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
                                rvHistory.setAdapter(adapter);
                            } else {
                                adapter.updateList(freshOrders);
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {}
        });
    }

    @Override
    public void onOrderClick(Order order) {
        openOrderDetail(order.getId());
    }

    private void openOrderDetail(String orderId) {
        OrderDetailFragment detailFragment = OrderDetailFragment.newInstance(orderId);
        getParentFragmentManager().beginTransaction()
                .add(R.id.fragment_container, detailFragment, "ORDER_DETAIL")
                .addToBackStack("ORDER_DETAIL_TRANS")
                .commit();
    }
}