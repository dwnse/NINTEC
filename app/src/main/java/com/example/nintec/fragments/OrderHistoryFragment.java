package com.example.nintec.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nintec.MainActivity;
import com.example.nintec.R;
import com.example.nintec.adapters.OrderAdapter;
import com.example.nintec.models.Order;
import com.example.nintec.models.OrderStatus;
import com.example.nintec.repositories.OrderRepository;
import java.util.ArrayList;
import java.util.List;

public class OrderHistoryFragment extends Fragment implements OrderAdapter.OnOrderClickListener {

    private RecyclerView rvHistory;
    private LinearLayout layoutEmpty;
    private OrderAdapter adapter;
    private TextView tvOrderCount;
    private EditText etSearch;

    // Filter chips
    private TextView chipAll, chipPending, chipCompleted, chipCancelled;
    private TextView chipCentral, chipCalacoto, chipElAlto;

    // Current filter state
    private String activeStatusFilter = "all"; // all, pending, completed, cancelled
    private String activeBranchFilter = null; // null = no branch filter
    private String searchQuery = "";

    // All orders
    private List<Order> allOrders = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_history, container, false);

        ImageView btnBack = view.findViewById(R.id.btn_history_back);
        rvHistory = view.findViewById(R.id.rv_order_history);
        layoutEmpty = view.findViewById(R.id.layout_history_empty);
        tvOrderCount = view.findViewById(R.id.tv_order_count);
        etSearch = view.findViewById(R.id.et_search_orders);
        Button btnExplore = view.findViewById(R.id.btn_history_explore);

        // Filter chips
        chipAll = view.findViewById(R.id.chip_all);
        chipPending = view.findViewById(R.id.chip_pending);
        chipCompleted = view.findViewById(R.id.chip_completed);
        chipCancelled = view.findViewById(R.id.chip_cancelled);
        chipCentral = view.findViewById(R.id.chip_central);
        chipCalacoto = view.findViewById(R.id.chip_calacoto);
        chipElAlto = view.findViewById(R.id.chip_el_alto);

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        btnExplore.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).findViewById(R.id.nav_catalog).performClick();
            }
        });

        // Status filter chips
        chipAll.setOnClickListener(v -> setStatusFilter("all"));
        chipPending.setOnClickListener(v -> setStatusFilter("pending"));
        chipCompleted.setOnClickListener(v -> setStatusFilter("completed"));
        chipCancelled.setOnClickListener(v -> setStatusFilter("cancelled"));

        // Branch filter chips (toggle)
        chipCentral.setOnClickListener(v -> toggleBranchFilter("Central"));
        chipCalacoto.setOnClickListener(v -> toggleBranchFilter("Calacoto"));
        chipElAlto.setOnClickListener(v -> toggleBranchFilter("El Alto"));

        // Search
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().toLowerCase().trim();
                applyFilters();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        setupRecyclerView();
        return view;
    }

    private void setStatusFilter(String filter) {
        activeStatusFilter = filter;
        updateChipStyles();
        applyFilters();
    }

    private void toggleBranchFilter(String branch) {
        if (branch.equals(activeBranchFilter)) {
            activeBranchFilter = null; // Deselect
        } else {
            activeBranchFilter = branch;
        }
        updateChipStyles();
        applyFilters();
    }

    private void updateChipStyles() {
        // Status chips
        setChipActive(chipAll, activeStatusFilter.equals("all"));
        setChipActive(chipPending, activeStatusFilter.equals("pending"));
        setChipActive(chipCompleted, activeStatusFilter.equals("completed"));
        setChipActive(chipCancelled, activeStatusFilter.equals("cancelled"));

        // Branch chips
        setChipActive(chipCentral, "Central".equals(activeBranchFilter));
        setChipActive(chipCalacoto, "Calacoto".equals(activeBranchFilter));
        setChipActive(chipElAlto, "El Alto".equals(activeBranchFilter));
    }

    private void setChipActive(TextView chip, boolean active) {
        if (active) {
            chip.setBackgroundResource(R.drawable.bg_button_primary);
            chip.setTextColor(getResources().getColor(R.color.white, null));
        } else {
            chip.setBackgroundResource(R.drawable.bg_button_secondary);
            chip.setTextColor(getResources().getColor(R.color.text_primary, null));
        }
    }

    private void applyFilters() {
        List<Order> filtered = new ArrayList<>();

        for (Order order : allOrders) {
            // Status filter
            if (!activeStatusFilter.equals("all")) {
                switch (activeStatusFilter) {
                    case "pending":
                        if (order.getStatus() != OrderStatus.PENDING &&
                                order.getStatus() != OrderStatus.CONFIRMED &&
                                order.getStatus() != OrderStatus.PROCESSING) continue;
                        break;
                    case "completed":
                        if (order.getStatus() != OrderStatus.COMPLETED &&
                                order.getStatus() != OrderStatus.DELIVERED) continue;
                        break;
                    case "cancelled":
                        if (order.getStatus() != OrderStatus.CANCELLED &&
                                order.getStatus() != OrderStatus.REJECTED) continue;
                        break;
                }
            }

            // Branch filter
            if (activeBranchFilter != null) {
                String branchName = order.getBranchName();
                if (branchName == null || !branchName.toLowerCase().contains(activeBranchFilter.toLowerCase())) {
                    continue;
                }
            }

            // Search filter
            if (!searchQuery.isEmpty()) {
                boolean matches = false;
                if (order.getOrderNumber() != null &&
                        order.getOrderNumber().toLowerCase().contains(searchQuery)) matches = true;
                if (order.getItemsSummary().toLowerCase().contains(searchQuery)) matches = true;
                if (order.getPaymentMethod() != null &&
                        order.getPaymentMethod().toLowerCase().contains(searchQuery)) matches = true;
                if (!matches) continue;
            }

            filtered.add(order);
        }

        updateList(filtered);
    }

    private void updateList(List<Order> orders) {
        tvOrderCount.setText(orders.size() + " pedido" + (orders.size() != 1 ? "s" : ""));

        if (orders.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvHistory.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvHistory.setVisibility(View.VISIBLE);
            if (adapter == null) {
                adapter = new OrderAdapter(orders, this);
                rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
                rvHistory.setAdapter(adapter);
            } else {
                adapter.updateList(orders);
            }
        }
    }

    private void setupRecyclerView() {
        allOrders = OrderRepository.getInstance().getOrders();
        applyFilters();

        OrderRepository.getInstance().fetchOrders(new OrderRepository.OrderListCallback() {
            @Override
            public void onSuccess(List<Order> freshOrders) {
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        allOrders = freshOrders;
                        applyFilters();
                    });
                }
            }

            @Override
            public void onError(String error) {}
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        setupRecyclerView();
    }

    @Override
    public void onOrderClick(Order order) {
        OrderDetailFragment detailFragment = OrderDetailFragment.newInstance(order.getId());
        getParentFragmentManager().beginTransaction()
                .add(R.id.fragment_container, detailFragment, "ORDER_DETAIL")
                .addToBackStack("ORDER_DETAIL_TRANS")
                .commit();
    }
}