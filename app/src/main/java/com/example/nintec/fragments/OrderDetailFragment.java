package com.example.nintec.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nintec.R;
import com.example.nintec.adapters.OrderItemAdapter;
import com.example.nintec.models.Order;
import com.example.nintec.models.OrderStatus;
import com.example.nintec.models.Product;
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
        // Header card
        TextView tvId = view.findViewById(R.id.tv_detail_order_id);
        TextView tvStatus = view.findViewById(R.id.tv_detail_status);
        TextView tvDate = view.findViewById(R.id.tv_detail_date);

        // Payment
        TextView tvPayment = view.findViewById(R.id.tv_detail_payment);
        TextView tvPaymentDetails = view.findViewById(R.id.tv_detail_payment_details);

        // Total breakdown
        TextView tvSubtotal = view.findViewById(R.id.tv_detail_subtotal);
        TextView tvTotal = view.findViewById(R.id.tv_detail_total);
        RelativeLayout layoutDiscount = view.findViewById(R.id.layout_discount_row);
        TextView tvDiscount = view.findViewById(R.id.tv_detail_discount);

        // Branch
        CardView cardBranch = view.findViewById(R.id.card_branch);
        TextView tvBranchName = view.findViewById(R.id.tv_detail_branch_name);
        TextView tvBranchAddress = view.findViewById(R.id.tv_detail_branch_address);

        // Timeline
        View[] dots = {
                view.findViewById(R.id.dot_step_0),
                view.findViewById(R.id.dot_step_1),
                view.findViewById(R.id.dot_step_2),
                view.findViewById(R.id.dot_step_3)
        };
        TextView[] stepLabels = {
                view.findViewById(R.id.tv_step_0),
                view.findViewById(R.id.tv_step_1),
                view.findViewById(R.id.tv_step_2),
                view.findViewById(R.id.tv_step_3)
        };

        // Notes
        CardView cardNotes = view.findViewById(R.id.card_notes);
        TextView tvNotes = view.findViewById(R.id.tv_detail_notes);

        // Items
        RecyclerView rvItems = view.findViewById(R.id.rv_order_detail_items);

        // ── Populate ──

        tvId.setText(getString(R.string.order_id_format, currentOrder.getOrderNumber()));
        tvDate.setText(currentOrder.getCreatedAt());
        tvPayment.setText(currentOrder.getPaymentMethod());
        tvTotal.setText(currentOrder.getTotal());

        // Subtotal
        double subtotal = currentOrder.getSubtotal() > 0 ? currentOrder.getSubtotal() : currentOrder.getTotalValue();
        tvSubtotal.setText(Product.formatPrice(subtotal));

        // Discount
        if (currentOrder.getDiscount() > 0) {
            layoutDiscount.setVisibility(View.VISIBLE);
            tvDiscount.setText("-" + Product.formatPrice(currentOrder.getDiscount()));
        }

        // Payment details
        if (currentOrder.getPaymentDetails() != null && !currentOrder.getPaymentDetails().isEmpty()) {
            tvPaymentDetails.setVisibility(View.VISIBLE);
            tvPaymentDetails.setText(currentOrder.getPaymentDetails());
        }

        // Status badge
        OrderStatus status = currentOrder.getStatus();
        tvStatus.setText(status.getDisplayName());
        switch (status) {
            case COMPLETED:
            case DELIVERED:
                tvStatus.setBackgroundResource(R.drawable.bg_badge_success);
                tvStatus.setTextColor(getResources().getColor(R.color.success, null));
                break;
            case CANCELLED:
            case REJECTED:
                tvStatus.setBackgroundResource(R.drawable.bg_badge_error);
                tvStatus.setTextColor(getResources().getColor(R.color.error, null));
                break;
            default:
                tvStatus.setBackgroundResource(R.drawable.bg_badge_new);
                tvStatus.setTextColor(getResources().getColor(R.color.nintec_blue, null));
                break;
        }

        // Timeline stepper
        int stepIndex = status.getStepIndex();
        CardView cardTimeline = view.findViewById(R.id.card_timeline);
        if (stepIndex == -1) {
            // Cancelled - hide timeline
            cardTimeline.setVisibility(View.GONE);
        } else {
            for (int i = 0; i < dots.length; i++) {
                if (i <= stepIndex) {
                    dots[i].setBackgroundResource(R.drawable.bg_badge_success);
                    stepLabels[i].setTextColor(getResources().getColor(R.color.text_primary, null));
                } else {
                    dots[i].setBackgroundResource(R.drawable.bg_button_secondary);
                    stepLabels[i].setTextColor(getResources().getColor(R.color.text_hint, null));
                }
            }
        }

        // Branch info
        if (currentOrder.getBranchName() != null && !currentOrder.getBranchName().isEmpty()) {
            cardBranch.setVisibility(View.VISIBLE);
            tvBranchName.setText(currentOrder.getBranchName());
            String addressText = "";
            if (currentOrder.getBranchCity() != null) addressText += currentOrder.getBranchCity();
            if (currentOrder.getBranchAddress() != null) {
                if (!addressText.isEmpty()) addressText += " - ";
                addressText += currentOrder.getBranchAddress();
            }
            tvBranchAddress.setText(addressText);
        }

        // Notes
        if (currentOrder.getNotes() != null && !currentOrder.getNotes().isEmpty()) {
            cardNotes.setVisibility(View.VISIBLE);
            tvNotes.setText(currentOrder.getNotes());
        }

        // Products
        OrderItemAdapter adapter = new OrderItemAdapter(currentOrder.getItems());
        rvItems.setLayoutManager(new LinearLayoutManager(getContext()));
        rvItems.setAdapter(adapter);
    }
}