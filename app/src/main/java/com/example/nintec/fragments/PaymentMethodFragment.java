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
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.nintec.R;
import com.example.nintec.managers.CartManager;

import java.util.Locale;

public class PaymentMethodFragment extends Fragment {

    private Button tabCard, tabQr, tabPaypal;
    private LinearLayout layoutCard, layoutQr, layoutPaypal;
    private Button btnContinue;

    // Card fields
    private EditText etCardNumber, etCardExpiry, etCardCvv, etCardHolder;
    // QR fields
    private EditText etQrReference, etQrBank;
    // PayPal fields
    private EditText etPaypalEmail, etPaypalTransaction;
    // Notes
    private EditText etNotes;

    // Branch selector
    private Button btnBranchCentral, btnBranchCalacoto, btnBranchElAlto;
    private TextView tvBranchDesc;
    private String selectedBranchId = "c1000000-0000-0000-0000-000000000001";
    private String selectedBranchName = "Sucursal Central";
    private String selectedBranchCity = "La Paz";
    private String selectedBranchAddress = "Av. San Martín #123, Centro";

    private String selectedMethod = "card"; // "card", "qr", "paypal"

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_payment_method, container, false);

        // Header
        ImageView btnBack = view.findViewById(R.id.btn_payment_back);
        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        // Total display
        TextView tvTotal = view.findViewById(R.id.tv_payment_total);
        double total = CartManager.getInstance().getTotalAmount();
        tvTotal.setText(String.format(Locale.getDefault(), "Bs %,.2f", total));

        // Tabs
        tabCard = view.findViewById(R.id.tab_card);
        tabQr = view.findViewById(R.id.tab_qr);
        tabPaypal = view.findViewById(R.id.tab_paypal);

        // Form containers
        layoutCard = view.findViewById(R.id.layout_card_form);
        layoutQr = view.findViewById(R.id.layout_qr_form);
        layoutPaypal = view.findViewById(R.id.layout_paypal_form);

        // Card fields
        etCardNumber = view.findViewById(R.id.et_card_number);
        etCardExpiry = view.findViewById(R.id.et_card_expiry);
        etCardCvv = view.findViewById(R.id.et_card_cvv);
        etCardHolder = view.findViewById(R.id.et_card_holder);

        // QR fields
        etQrReference = view.findViewById(R.id.et_qr_reference);
        etQrBank = view.findViewById(R.id.et_qr_bank);

        // PayPal fields
        etPaypalEmail = view.findViewById(R.id.et_paypal_email);
        etPaypalTransaction = view.findViewById(R.id.et_paypal_transaction);

        // Notes
        etNotes = view.findViewById(R.id.et_payment_notes);

        // Branch views
        btnBranchCentral = view.findViewById(R.id.btn_branch_central);
        btnBranchCalacoto = view.findViewById(R.id.btn_branch_calacoto);
        btnBranchElAlto = view.findViewById(R.id.btn_branch_el_alto);
        tvBranchDesc = view.findViewById(R.id.tv_branch_description);

        btnBranchCentral.setOnClickListener(v -> selectBranch("central"));
        btnBranchCalacoto.setOnClickListener(v -> selectBranch("calacoto"));
        btnBranchElAlto.setOnClickListener(v -> selectBranch("el_alto"));

        // Continue button
        btnContinue = view.findViewById(R.id.btn_payment_continue);

        // Tab click handlers
        tabCard.setOnClickListener(v -> selectTab("card"));
        tabQr.setOnClickListener(v -> selectTab("qr"));
        tabPaypal.setOnClickListener(v -> selectTab("paypal"));

        // Card number formatting (auto-spaces)
        etCardNumber.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;
                String digits = s.toString().replaceAll("\\s", "");
                if (digits.length() > 16) digits = digits.substring(0, 16);
                StringBuilder formatted = new StringBuilder();
                for (int i = 0; i < digits.length(); i++) {
                    if (i > 0 && i % 4 == 0) formatted.append(' ');
                    formatted.append(digits.charAt(i));
                }
                s.replace(0, s.length(), formatted);
                isFormatting = false;
            }
        });

        // Expiry formatting (auto-slash)
        etCardExpiry.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;
                String digits = s.toString().replaceAll("/", "");
                if (digits.length() > 4) digits = digits.substring(0, 4);
                if (digits.length() >= 3) {
                    s.replace(0, s.length(), digits.substring(0, 2) + "/" + digits.substring(2));
                }
                isFormatting = false;
            }
        });

        btnContinue.setOnClickListener(v -> validateAndProceed());

        // Defaults
        selectTab("card");
        selectBranch("central");

        return view;
    }

    private void selectBranch(String branch) {
        btnBranchCentral.setBackgroundResource(R.drawable.bg_button_secondary);
        btnBranchCentral.setTextColor(getResources().getColor(R.color.text_primary, null));
        btnBranchCalacoto.setBackgroundResource(R.drawable.bg_button_secondary);
        btnBranchCalacoto.setTextColor(getResources().getColor(R.color.text_primary, null));
        btnBranchElAlto.setBackgroundResource(R.drawable.bg_button_secondary);
        btnBranchElAlto.setTextColor(getResources().getColor(R.color.text_primary, null));

        switch (branch) {
            case "calacoto":
                selectedBranchId = "c1000000-0000-0000-0000-000000000002";
                selectedBranchName = "NINTECLP Calacoto";
                selectedBranchCity = "La Paz";
                selectedBranchAddress = "Calle 21 de Calacoto, Edif. Arce";
                btnBranchCalacoto.setBackgroundResource(R.drawable.bg_button_primary);
                btnBranchCalacoto.setTextColor(getResources().getColor(R.color.white, null));
                if (tvBranchDesc != null) tvBranchDesc.setText("📍 " + selectedBranchAddress + " - " + selectedBranchCity);
                break;
            case "el_alto":
                selectedBranchId = "c1000000-0000-0000-0000-000000000003";
                selectedBranchName = "NINTECLP El Alto";
                selectedBranchCity = "El Alto";
                selectedBranchAddress = "Av. 6 de Marzo, C.C. El Ceibo";
                btnBranchElAlto.setBackgroundResource(R.drawable.bg_button_primary);
                btnBranchElAlto.setTextColor(getResources().getColor(R.color.white, null));
                if (tvBranchDesc != null) tvBranchDesc.setText("📍 " + selectedBranchAddress + " - " + selectedBranchCity);
                break;
            case "central":
            default:
                selectedBranchId = "c1000000-0000-0000-0000-000000000001";
                selectedBranchName = "Sucursal Central";
                selectedBranchCity = "La Paz";
                selectedBranchAddress = "Av. San Martín #123, Centro";
                btnBranchCentral.setBackgroundResource(R.drawable.bg_button_primary);
                btnBranchCentral.setTextColor(getResources().getColor(R.color.white, null));
                if (tvBranchDesc != null) tvBranchDesc.setText("📍 " + selectedBranchAddress + " - " + selectedBranchCity);
                break;
        }
    }

    private void selectTab(String tab) {
        selectedMethod = tab;

        // Reset tab backgrounds
        tabCard.setBackgroundResource(R.drawable.bg_button_secondary);
        tabCard.setTextColor(getResources().getColor(R.color.text_primary, null));
        tabQr.setBackgroundResource(R.drawable.bg_button_secondary);
        tabQr.setTextColor(getResources().getColor(R.color.text_primary, null));
        tabPaypal.setBackgroundResource(R.drawable.bg_button_secondary);
        tabPaypal.setTextColor(getResources().getColor(R.color.text_primary, null));

        // Hide all forms
        layoutCard.setVisibility(View.GONE);
        layoutQr.setVisibility(View.GONE);
        layoutPaypal.setVisibility(View.GONE);

        // Activate selected
        switch (tab) {
            case "card":
                tabCard.setBackgroundResource(R.drawable.bg_button_primary);
                tabCard.setTextColor(getResources().getColor(R.color.white, null));
                layoutCard.setVisibility(View.VISIBLE);
                break;
            case "qr":
                tabQr.setBackgroundResource(R.drawable.bg_button_primary);
                tabQr.setTextColor(getResources().getColor(R.color.white, null));
                layoutQr.setVisibility(View.VISIBLE);
                break;
            case "paypal":
                tabPaypal.setBackgroundResource(R.drawable.bg_button_primary);
                tabPaypal.setTextColor(getResources().getColor(R.color.white, null));
                layoutPaypal.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void validateAndProceed() {
        String method = "";
        String paymentDetails = "";

        switch (selectedMethod) {
            case "card":
                String cardNum = etCardNumber.getText().toString().trim();
                String expiry = etCardExpiry.getText().toString().trim();
                String cvv = etCardCvv.getText().toString().trim();
                String holder = etCardHolder.getText().toString().trim();

                if (cardNum.replaceAll("\\s", "").length() < 16) {
                    etCardNumber.setError("Ingresa los 16 dígitos");
                    etCardNumber.requestFocus();
                    return;
                }
                if (expiry.length() < 5) {
                    etCardExpiry.setError("Formato: MM/AA");
                    etCardExpiry.requestFocus();
                    return;
                }
                if (cvv.length() < 3) {
                    etCardCvv.setError("Mín. 3 dígitos");
                    etCardCvv.requestFocus();
                    return;
                }
                if (holder.isEmpty()) {
                    etCardHolder.setError("Ingresa el nombre");
                    etCardHolder.requestFocus();
                    return;
                }

                method = "Visa / Mastercard";
                String maskedCard = "****" + cardNum.replaceAll("\\s", "").substring(12);
                paymentDetails = holder + " | " + maskedCard + " | Exp: " + expiry;
                break;

            case "qr":
                String ref = etQrReference.getText().toString().trim();
                String bank = etQrBank.getText().toString().trim();

                if (ref.isEmpty()) {
                    etQrReference.setError("Ingresa la referencia del pago QR");
                    etQrReference.requestFocus();
                    return;
                }

                method = "QR";
                paymentDetails = "Ref: " + ref + (bank.isEmpty() ? "" : " | Banco: " + bank);
                break;

            case "paypal":
                String email = etPaypalEmail.getText().toString().trim();
                String transId = etPaypalTransaction.getText().toString().trim();

                if (email.isEmpty() || !email.contains("@")) {
                    etPaypalEmail.setError("Ingresa un correo válido");
                    etPaypalEmail.requestFocus();
                    return;
                }

                method = "PayPal";
                paymentDetails = email + (transId.isEmpty() ? "" : " | TX: " + transId);
                break;
        }

        String notes = etNotes.getText().toString().trim();
        openConfirmation(method, paymentDetails, notes);
    }

    private void openConfirmation(String method, String paymentDetails, String notes) {
        OrderConfirmationFragment confirmationFragment =
                OrderConfirmationFragment.newInstance(method, paymentDetails, notes,
                        selectedBranchId, selectedBranchName, selectedBranchCity, selectedBranchAddress);
        getParentFragmentManager().beginTransaction()
                .add(R.id.fragment_container, confirmationFragment, "CONFIRM")
                .addToBackStack("CONFIRM_TRANS")
                .commit();
    }
}