package com.example.nintec.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.nintec.R;

public class PaymentMethodFragment extends Fragment {

    private RadioGroup rgPayment;
    private Button btnContinue;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_payment_method, container, false);

        ImageView btnBack = view.findViewById(R.id.btn_payment_back);
        rgPayment = view.findViewById(R.id.rg_payment_methods);
        btnContinue = view.findViewById(R.id.btn_payment_continue);

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        btnContinue.setOnClickListener(v -> {
            int selectedId = rgPayment.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(getContext(), "Por favor selecciona un método de pago", Toast.LENGTH_SHORT).show();
            } else {
                String method = "";
                if (selectedId == R.id.rb_visa) method = "Visa / Mastercard";
                else if (selectedId == R.id.rb_paypal) method = "PayPal";
                else if (selectedId == R.id.rb_qr) method = "QR";

                openConfirmation(method);
            }
        });

        return view;
    }

    private void openConfirmation(String method) {
        OrderConfirmationFragment confirmationFragment = OrderConfirmationFragment.newInstance(method);
        // hideBottomNavigation(); // Ensure it stays visible if expected
        getParentFragmentManager().beginTransaction()
                .add(R.id.fragment_container, confirmationFragment, "CONFIRM")
                .addToBackStack("CONFIRM_TRANS")
                .commit();
    }
}