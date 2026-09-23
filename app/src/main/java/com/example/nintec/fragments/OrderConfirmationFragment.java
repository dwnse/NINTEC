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
import com.example.nintec.models.CartItem;
import com.example.nintec.models.Order;
import com.example.nintec.models.OrderStatus;
import com.example.nintec.repositories.OrderRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderConfirmationFragment extends Fragment {

    private static final String ARG_PAYMENT_METHOD = "payment_method";
    private static final String ARG_PAYMENT_DETAILS = "payment_details";
    private static final String ARG_NOTES = "notes";
    private static final String ARG_BRANCH_ID = "branch_id";
    private static final String ARG_BRANCH_NAME = "branch_name";
    private static final String ARG_BRANCH_CITY = "branch_city";
    private static final String ARG_BRANCH_ADDRESS = "branch_address";

    private String paymentMethod;
    private String paymentDetails;
    private String notes;
    private String branchId;
    private String branchName = "Sucursal Central";
    private String branchCity = "La Paz";
    private String branchAddress = "Av. San Martín #123, Centro";

    private Button btnConfirm;

    public static OrderConfirmationFragment newInstance(String method) {
        return newInstance(method, "", "");
    }

    public static OrderConfirmationFragment newInstance(String method, String details, String notes) {
        return newInstance(method, details, notes,
                "c1000000-0000-0000-0000-000000000001", "Sucursal Central", "La Paz", "Av. San Martín #123, Centro");
    }

    public static OrderConfirmationFragment newInstance(String method, String details, String notes,
                                                        String branchId, String branchName,
                                                        String branchCity, String branchAddress) {
        OrderConfirmationFragment fragment = new OrderConfirmationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PAYMENT_METHOD, method);
        args.putString(ARG_PAYMENT_DETAILS, details);
        args.putString(ARG_NOTES, notes);
        args.putString(ARG_BRANCH_ID, branchId);
        args.putString(ARG_BRANCH_NAME, branchName);
        args.putString(ARG_BRANCH_CITY, branchCity);
        args.putString(ARG_BRANCH_ADDRESS, branchAddress);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_confirmation, container, false);

        if (getArguments() != null) {
            paymentMethod = getArguments().getString(ARG_PAYMENT_METHOD, "");
            paymentDetails = getArguments().getString(ARG_PAYMENT_DETAILS, "");
            notes = getArguments().getString(ARG_NOTES, "");
            branchId = getArguments().getString(ARG_BRANCH_ID, "c1000000-0000-0000-0000-000000000001");
            if (getArguments().getString(ARG_BRANCH_NAME) != null) {
                branchName = getArguments().getString(ARG_BRANCH_NAME);
            }
            if (getArguments().getString(ARG_BRANCH_CITY) != null) {
                branchCity = getArguments().getString(ARG_BRANCH_CITY);
            }
            if (getArguments().getString(ARG_BRANCH_ADDRESS) != null) {
                branchAddress = getArguments().getString(ARG_BRANCH_ADDRESS);
            }
        }

        ImageView btnBack = view.findViewById(R.id.btn_confirm_back);
        RecyclerView rvItems = view.findViewById(R.id.rv_confirm_items);
        TextView tvPayment = view.findViewById(R.id.tv_confirm_payment_method);
        TextView tvNotes = view.findViewById(R.id.tv_confirm_notes);
        TextView tvBranchName = view.findViewById(R.id.tv_confirm_branch_name);
        TextView tvBranchAddress = view.findViewById(R.id.tv_confirm_branch_address);
        TextView tvTotal = view.findViewById(R.id.tv_confirm_total);
        btnConfirm = view.findViewById(R.id.btn_confirm_order);

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        // Branch display
        if (tvBranchName != null) {
            tvBranchName.setText("📍 " + branchName);
        }
        if (tvBranchAddress != null) {
            tvBranchAddress.setText(branchAddress + " - " + branchCity);
        }

        // Payment display
        String paymentDisplay = "Método: " + paymentMethod;
        if (paymentDetails != null && !paymentDetails.isEmpty()) {
            paymentDisplay += "\n" + paymentDetails;
        }
        tvPayment.setText(paymentDisplay);

        // Notes display
        if (notes != null && !notes.trim().isEmpty() && tvNotes != null) {
            tvNotes.setText("Nota: " + notes.trim());
            tvNotes.setVisibility(View.VISIBLE);
        }

        double total = CartManager.getInstance().getTotalAmount();
        tvTotal.setText(String.format(Locale.getDefault(), "Total a pagar: Bs %,.2f", total));

        rvItems.setLayoutManager(new LinearLayoutManager(getContext()));
        rvItems.setAdapter(new OrderItemAdapter(CartManager.getInstance().getItems()));

        btnConfirm.setOnClickListener(v -> finalizeOrder());

        return view;
    }

    private void finalizeOrder() {
        if (btnConfirm != null) {
            btnConfirm.setEnabled(false);
            btnConfirm.setText("Procesando...");
        }

        String today = new java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new java.util.Date());
        String orderNumber = OrderRepository.getInstance().generateOrderNumber();

        // Deep copy cart items before clearing
        List<CartItem> orderedItems = new ArrayList<>();
        for (CartItem item : CartManager.getInstance().getItems()) {
            orderedItems.add(new CartItem(item.getProduct(), item.getQuantity()));
        }

        double total = CartManager.getInstance().getTotalAmount();

        Order newOrder = new Order(
                orderNumber,
                orderNumber,
                orderedItems,
                total,
                paymentMethod,
                OrderStatus.PENDING,
                today
        );
        newOrder.setSubtotal(total);
        newOrder.setDiscount(0);
        if (paymentDetails != null) newOrder.setPaymentDetails(paymentDetails);
        if (notes != null && !notes.isEmpty()) newOrder.setNotes(notes);

        // Set branch information
        newOrder.setBranchId(branchId);
        newOrder.setBranchName(branchName);
        newOrder.setBranchCity(branchCity);
        newOrder.setBranchAddress(branchAddress);

        // Use createAndSyncOrder which persists locally AND syncs to Supabase
        OrderRepository.getInstance().createAndSyncOrder(newOrder, new OrderRepository.OrderSaveCallback() {
            @Override
            public void onSuccess(Order order) {
                // Order saved
            }

            @Override
            public void onError(String error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Pedido guardado localmente", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Clear cart
        CartManager.getInstance().clear();

        // Navigate to success
        OrderSuccessFragment successFragment = new OrderSuccessFragment();
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, successFragment, "SUCCESS")
                .commit();
    }
}