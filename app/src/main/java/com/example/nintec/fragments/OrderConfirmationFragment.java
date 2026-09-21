package com.example.nintec.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nintec.R;
import com.example.nintec.adapters.OrderItemAdapter;
import com.example.nintec.managers.CartManager;
import com.example.nintec.models.Order;
import com.example.nintec.models.OrderStatus;
import com.example.nintec.repositories.OrderRepository;
import java.util.Locale;

public class OrderConfirmationFragment extends Fragment {

    private static final String ARG_PAYMENT_METHOD = "payment_method";
    private String paymentMethod;

    public static OrderConfirmationFragment newInstance(String method) {
        OrderConfirmationFragment fragment = new OrderConfirmationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PAYMENT_METHOD, method);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_confirmation, container, false);

        if (getArguments() != null) {
            paymentMethod = getArguments().getString(ARG_PAYMENT_METHOD);
        }

        ImageView btnBack = view.findViewById(R.id.btn_confirm_back);
        RecyclerView rvItems = view.findViewById(R.id.rv_confirm_items);
        TextView tvPayment = view.findViewById(R.id.tv_confirm_payment_method);
        TextView tvTotal = view.findViewById(R.id.tv_confirm_total);
        Button btnConfirm = view.findViewById(R.id.btn_confirm_order);

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        tvPayment.setText("Método: " + paymentMethod);
        double total = CartManager.getInstance().getTotalAmount();
        tvTotal.setText(String.format(Locale.getDefault(), "Total a pagar: $%.2f", total));

        rvItems.setLayoutManager(new LinearLayoutManager(getContext()));
        rvItems.setAdapter(new OrderItemAdapter(CartManager.getInstance().getItems()));

        btnConfirm.setOnClickListener(v -> finalizeOrder());

        return view;
    }

    private void finalizeOrder() {
        // Create order
        String orderId = "NIN-" + (1000 + OrderRepository.getInstance().getOrders().size());
        Order newOrder = new Order(
                orderId,
                CartManager.getInstance().getItems(),
                String.format(Locale.getDefault(), "$%.2f", CartManager.getInstance().getTotalAmount()),
                paymentMethod,
                OrderStatus.COMPLETED,
                "25/09/2026"
        );

        OrderRepository.getInstance().addOrder(newOrder);
        CartManager.getInstance().clear();

        // Navigate to success
        OrderSuccessFragment successFragment = new OrderSuccessFragment();
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, successFragment, "SUCCESS")
                .commit();
    }
}