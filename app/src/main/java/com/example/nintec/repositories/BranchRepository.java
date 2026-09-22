package com.example.nintec.repositories;

import com.example.nintec.models.Branch;
import com.example.nintec.network.SupabaseClient;
import com.example.nintec.network.dto.BranchDto;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BranchRepository {
    private static BranchRepository instance;
    private List<Branch> branches;

    public interface BranchListCallback {
        void onSuccess(List<Branch> branches);
        void onError(String error);
    }

    private BranchRepository() {
        branches = new ArrayList<>();
        initFallbackData();
    }

    public static synchronized BranchRepository getInstance() {
        if (instance == null) {
            instance = new BranchRepository();
        }
        return instance;
    }

    private void initFallbackData() {
        branches.add(new Branch("1", "Sucursal Central", "Av. San Martín #123, Centro", -16.5000, -68.1500, "+591 2 1234567", "09:00 - 20:00"));
        branches.add(new Branch("2", "NINTECLP Sur", "Calle 21 de Calacoto, Edif. Arce", -16.5390, -68.0864, "+591 2 7654321", "10:00 - 21:00"));
        branches.add(new Branch("3", "NINTECLP El Alto", "Av. 6 de Marzo, C.C. El Ceibo", -16.5122, -68.1603, "+591 2 2334455", "09:00 - 19:00"));
        branches.add(new Branch("4", "Sucursal Miraflores", "Plaza Villarroel, Edif. Mirador", -16.4880, -68.1180, "+591 2 9988776", "08:30 - 18:30"));
    }

    /**
     * Fetch branches from Supabase REST API.
     */
    public void fetchBranches(BranchListCallback callback) {
        SupabaseClient.getInstance().getDataService()
                .getBranches("eq.true", "sort_order.asc,name.asc", "*")
                .enqueue(new Callback<List<BranchDto>>() {
                    @Override
                    public void onResponse(Call<List<BranchDto>> call, Response<List<BranchDto>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            List<Branch> list = new ArrayList<>();
                            for (BranchDto dto : response.body()) {
                                list.add(dto.toBranch());
                            }
                            branches = list;
                            if (callback != null) callback.onSuccess(branches);
                        } else {
                            if (callback != null) callback.onSuccess(branches);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<BranchDto>> call, Throwable t) {
                        if (callback != null) callback.onSuccess(branches);
                    }
                });
    }

    public List<Branch> getBranches() {
        return branches;
    }

    public List<Branch> searchBranches(String query) {
        if (query == null || query.isEmpty()) return branches;
        List<Branch> filtered = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        for (Branch b : branches) {
            if (b.getName().toLowerCase().contains(lowerQuery) || b.getAddress().toLowerCase().contains(lowerQuery)) {
                filtered.add(b);
            }
        }
        return filtered;
    }
}