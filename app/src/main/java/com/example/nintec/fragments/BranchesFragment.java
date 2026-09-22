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
import com.example.nintec.R;
import com.example.nintec.adapters.BranchAdapter;
import com.example.nintec.models.Branch;
import com.example.nintec.repositories.BranchRepository;
import java.util.ArrayList;
import java.util.List;

public class BranchesFragment extends Fragment implements BranchAdapter.OnBranchClickListener {

    private EditText etSearch;
    private ImageView imgSearchClear;
    private RecyclerView rvBranches;
    private TextView tvEmpty;
    private BranchAdapter branchAdapter;
    private List<Branch> displayedBranches;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_branches, container, false);

        etSearch = view.findViewById(R.id.et_branches_search);
        imgSearchClear = view.findViewById(R.id.img_branches_search_clear);
        rvBranches = view.findViewById(R.id.rv_branches);
        tvEmpty = view.findViewById(R.id.tv_branches_empty);

        setupRecyclerView();
        setupSearchFilter();

        if (imgSearchClear != null) {
            imgSearchClear.setOnClickListener(v -> {
                if (etSearch != null) etSearch.setText("");
            });
        }

        return view;
    }

    private void setupRecyclerView() {
        displayedBranches = new ArrayList<>(BranchRepository.getInstance().getBranches());
        branchAdapter = new BranchAdapter(displayedBranches, this);
        rvBranches.setLayoutManager(new LinearLayoutManager(getContext()));
        rvBranches.setAdapter(branchAdapter);

        BranchRepository.getInstance().fetchBranches(new BranchRepository.BranchListCallback() {
            @Override
            public void onSuccess(List<Branch> branches) {
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        displayedBranches.clear();
                        displayedBranches.addAll(branches);
                        branchAdapter.updateList(displayedBranches);
                    });
                }
            }

            @Override
            public void onError(String error) {}
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
        List<Branch> results = BranchRepository.getInstance().searchBranches(query);
        displayedBranches.clear();
        displayedBranches.addAll(results);
        branchAdapter.updateList(displayedBranches);

        if (results.isEmpty()) {
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
        Uri uri = Uri.parse("geo:" + branch.getLatitude() + "," + branch.getLongitude() +
                "?q=" + branch.getLatitude() + "," + branch.getLongitude() +
                "(" + Uri.encode(branch.getName()) + ")");
        
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        
        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), R.string.map_app_not_found, Toast.LENGTH_SHORT).show();
        }
    }
}