package com.example.nintec.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nintec.R;
import com.example.nintec.adapters.BranchAdapter;
import com.example.nintec.models.Branch;
import com.example.nintec.repositories.BranchRepository;

import java.util.ArrayList;
import java.util.List;

public class BranchesFragment extends Fragment implements BranchAdapter.OnBranchClickListener {

    private EditText etSearch;
    private ImageView imgSearchClear, imgMapHeader;
    private TextView tvEmpty, tvMapPlaceholder;
    private RecyclerView rvBranches;
    private BranchAdapter branchAdapter;
    private List<Branch> masterBranchesList = new ArrayList<>();
    private List<Branch> displayedBranches = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_branches, container, false);

        etSearch = view.findViewById(R.id.et_branches_search);
        imgSearchClear = view.findViewById(R.id.img_branches_search_clear);
        imgMapHeader = view.findViewById(R.id.img_branches_map_static);
        tvMapPlaceholder = view.findViewById(R.id.tv_map_placeholder);
        rvBranches = view.findViewById(R.id.rv_branches);
        tvEmpty = view.findViewById(R.id.tv_branches_empty);

        if (imgSearchClear != null) {
            imgSearchClear.setOnClickListener(v -> etSearch.setText(""));
        }

        setupRecyclerView();
        setupSearchFilter();
        loadBranchesData();
        setupMapHeader();

        return view;
    }

    private void setupMapHeader() {
        // Use Geoapify Static Map to show a professional map of La Paz
        String lat = "-16.5000";
        String lon = "-68.1500";
        String apiKey = "e31d7c747bc3488ca678d605b4e32b56";
        String staticMapUrl = "https://maps.geoapify.com/v1/staticmap?style=osm-bright-smooth&width=600&height=400&center=lonlat:" + lon + "," + lat + "&zoom=13&apiKey=" + apiKey;

        if (imgMapHeader != null) {
            Glide.with(this)
                    .load(staticMapUrl)
                    .into(imgMapHeader);
            if (tvMapPlaceholder != null) tvMapPlaceholder.setVisibility(View.GONE);
        }
    }

    private void setupRecyclerView() {
        branchAdapter = new BranchAdapter(displayedBranches, this);
        rvBranches.setLayoutManager(new LinearLayoutManager(getContext()));
        rvBranches.setAdapter(branchAdapter);
    }

    private void loadBranchesData() {
        BranchRepository.getInstance().fetchBranches(new BranchRepository.BranchListCallback() {
            @Override
            public void onSuccess(List<Branch> branches) {
                if (isAdded()) {
                    masterBranchesList = new ArrayList<>(branches);
                    fetchExternalGeoapify();
                }
            }

            @Override
            public void onError(String error) {
                if (isAdded()) {
                    fetchExternalGeoapify(); // Try external anyway
                }
            }
        });
    }

    private void fetchExternalGeoapify() {
        BranchRepository.getInstance().fetchExternalBranches(new BranchRepository.BranchCallback() {
            @Override
            public void onSuccess(List<Branch> branches) {
                if (isAdded()) {
                    masterBranchesList.addAll(branches);
                    performSearch(etSearch.getText().toString());
                }
            }
            @Override
            public void onError(String message) {
                if (isAdded()) performSearch(etSearch.getText().toString());
            }
        });
    }

    private void setupSearchFilter() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString();
                if (imgSearchClear != null) {
                    imgSearchClear.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
                }
                performSearch(query);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void performSearch(String query) {
        if (query == null || query.isEmpty()) {
            updateDisplayList(masterBranchesList);
            return;
        }

        List<Branch> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        for (Branch b : masterBranchesList) {
            if (b.getName().toLowerCase().contains(lowerQuery) || b.getAddress().toLowerCase().contains(lowerQuery)) {
                results.add(b);
            }
        }
        updateDisplayList(results);
    }

    private void updateDisplayList(List<Branch> list) {
        displayedBranches.clear();
        displayedBranches.addAll(list);
        branchAdapter.updateList(displayedBranches);

        if (displayedBranches.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvBranches.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvBranches.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBranchClick(Branch branch) {
        Toast.makeText(getContext(), "Sucursal: " + branch.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onViewMapClick(Branch branch) {
        openExternalMap(branch);
    }

    private void openExternalMap(Branch branch) {
        String label = Uri.encode(branch.getName());
        Uri uri = Uri.parse("geo:" + branch.getLatitude() + "," + branch.getLongitude() +
                "?q=" + branch.getLatitude() + "," + branch.getLongitude() +
                "(" + label + ")");
        
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        
        try {
            startActivity(intent);
        } catch (Exception e) {
            // Fallback: Open in Browser (Google Maps Web)
            String webUrl = "https://www.google.com/maps/search/?api=1&query=" + branch.getLatitude() + "," + branch.getLongitude();
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webUrl));
            startActivity(webIntent);
        }
    }
}
