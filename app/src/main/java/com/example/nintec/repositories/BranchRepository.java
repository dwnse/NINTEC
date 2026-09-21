package com.example.nintec.repositories;

import com.example.nintec.models.Branch;
import java.util.ArrayList;
import java.util.List;

public class BranchRepository {
    private static BranchRepository instance;
    private List<Branch> branches;

    private BranchRepository() {
        branches = new ArrayList<>();
        initData();
    }

    public static synchronized BranchRepository getInstance() {
        if (instance == null) {
            instance = new BranchRepository();
        }
        return instance;
    }

    private void initData() {
        // Mock data for NINTECLP Branches
        branches.add(new Branch("1", "Sucursal Central", "Av. San Martín #123, Centro", -16.5000, -68.1500, "+591 2 1234567", "09:00 - 20:00"));
        branches.add(new Branch("2", "NINTECLP Sur", "Calle 21 de Calacoto, Edif. Arce", -16.5390, -68.0864, "+591 2 7654321", "10:00 - 21:00"));
        branches.add(new Branch("3", "NINTECLP El Alto", "Av. 6 de Marzo, C.C. El Ceibo", -16.5122, -68.1603, "+591 2 2334455", "09:00 - 19:00"));
        branches.add(new Branch("4", "Sucursal Miraflores", "Plaza Villarroel, Edif. Mirador", -16.4880, -68.1180, "+591 2 9988776", "08:30 - 18:30"));
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