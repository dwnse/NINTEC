package com.example.nintec.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nintec.R;
import com.example.nintec.adapters.OrderItemAdapter;
import com.example.nintec.models.Order;
import com.example.nintec.repositories.OrderRepository;

public class OrderDetailFragment extends Fragment {

    private static final String ARG_ORDER_ID = "order_id";
    private Order currentOrder;

    public static OrderDetailFragment newInstance(String orderId) {
        OrderDetailFragment fragment = new OrderDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ORDER_ID, orderId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_detail, container, false);

        if (getArguments() != null) {
            String orderId = getArguments().getString(ARG_ORDER_ID);
            currentOrder = OrderRepository.getInstance().getOrderById(orderId);
        }

        ImageView btnBack = view.findViewById(R.id.btn_order_detail_back);
        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        if (currentOrder != null) {
            hydrateDetails(view);
        }

        return view;
    }

    private void hydrateDetails(View view) {
        TextView tvId = view.findViewById(R.id.tv_detail_order_id);
        TextView tvStatus = view.findViewById(R.id.tv_detail_status);
        TextView tvDate = view.findViewById(R.id.tv_detail_date);
        TextView tvPayment = view.findViewById(R.id.tv_detail_payment);
        TextView tvTotal = view.findViewById(R.id.tv_detail_total);
        RecyclerView rvItems = view.findViewById(R.id.rv_order_detail_items);

        tvId.setText(getString(R.string.order_id_format, currentOrder.getId()));
        tvDate.setText(getString(R.string.order_date_detail, currentOrder.getCreatedAt()));
        tvPayment.setText(getString(R.string.order_payment_method, currentOrder.getPaymentMethod()));
        tvTotal.setText(currentOrder.getTotal());

        switch (currentOrder.getStatus()) {
            case COMPLETED:
                tvStatus.setText(R.string.order_status_completed);
                tvStatus.setBackgroundResource(R.drawable.bg_badge_success);
                break;
            case PENDING:
                tvStatus.setText(R.string.order_status_pending);
                tvStatus.setBackgroundResource(R.drawable.bg_badge_new);
                break;
            case REJECTED:
                tvStatus.setText(R.string.order_status_rejected);
                tvStatus.setBackgroundResource(R.drawable.bg_badge_error);
                break;
        }

        OrderItemAdapter adapter = new OrderItemAdapter(currentOrder.getItems());
        rvItems.setLayoutManager(new LinearLayoutManager(getContext()));
        rvItems.setAdapter(adapter);
    }
}