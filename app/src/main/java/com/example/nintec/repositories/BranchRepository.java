package com.example.nintec.repositories;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.nintec.models.Branch;
import com.example.nintec.network.SupabaseClient;
import com.example.nintec.network.dto.BranchDto;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BranchRepository {
    private static final String GEOAPIFY_KEY = "e31d7c747bc3488ca678d605b4e32b56";
    private static BranchRepository instance;
    private List<Branch> branches;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    public interface BranchListCallback {
        void onSuccess(List<Branch> branches);
        void onError(String error);
    }

    public interface BranchCallback {
        void onSuccess(List<Branch> branches);
        void onError(String message);
    }

    private BranchRepository() {
        branches = new ArrayList<>();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized BranchRepository getInstance() {
        if (instance == null) {
            instance = new BranchRepository();
        }
        return instance;
    }

    /**
     * Fetch branches from Supabase REST API.
     */
    public void fetchBranches(BranchListCallback callback) {
        // Fixed: removed 'sort_order' as it doesn't exist in branches table. Using 'name.asc'
        SupabaseClient.getInstance().getDataService()
                .getBranches("eq.true", "name.asc", "*")
                .enqueue(new Callback<List<BranchDto>>() {
                    @Override
                    public void onResponse(Call<List<BranchDto>> call, Response<List<BranchDto>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Branch> list = new ArrayList<>();
                            for (BranchDto dto : response.body()) {
                                list.add(dto.toBranch());
                            }
                            branches = list;
                            if (callback != null) callback.onSuccess(branches);
                        } else {
                            if (callback != null) callback.onError("Error " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<BranchDto>> call, Throwable t) {
                        if (callback != null) callback.onError("Error de red");
                    }
                });
    }

    /**
     * Fetch external branches from Geoapify API.
     */
    public void fetchExternalBranches(BranchCallback callback) {
        executorService.execute(() -> {
            try {
                // Using a more robust circle filter for La Paz area
                String urlString = "https://api.geoapify.com/v2/places?categories=commercial.electronics&filter=circle:-68.15,-16.5,10000&limit=20&apiKey=" + GEOAPIFY_KEY;
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    JSONArray features = jsonResponse.getJSONArray("features");
                    List<Branch> apiBranches = new ArrayList<>();

                    for (int i = 0; i < features.length(); i++) {
                        JSONObject feature = features.getJSONObject(i);
                        JSONObject properties = feature.getJSONObject("properties");
                        
                        // Handle geometry carefully
                        if (!feature.has("geometry")) continue;
                        JSONObject geometry = feature.getJSONObject("geometry");
                        if (!geometry.has("coordinates")) continue;
                        JSONArray coordinates = geometry.getJSONArray("coordinates");

                        String id = properties.optString("place_id", "API_" + i);
                        String name = properties.optString("name", properties.optString("formatted", "Tienda Tecnológica"));
                        String address = properties.optString("formatted", "Dirección no disponible");
                        double lon = coordinates.getDouble(0);
                        double lat = coordinates.getDouble(1);

                        apiBranches.add(new Branch(id, name, address, lat, lon, "Consulte en tienda", "Horario no disponible"));
                    }

                    mainHandler.post(() -> callback.onSuccess(apiBranches));
                } else {
                    mainHandler.post(() -> callback.onError("Geoapify Error " + responseCode));
                }
                conn.disconnect();

            } catch (Exception e) {
                Log.e("BranchRepository", "Geoapify API failed", e);
                mainHandler.post(() -> callback.onError("Error de conexión API externa"));
            }
        });
    }

    public List<Branch> getBranches() {
        return branches;
    }
}
