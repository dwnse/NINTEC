package com.example.nintec.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nintec.R;
import com.example.nintec.adapters.BranchAdapter;
import com.example.nintec.models.Branch;
import com.example.nintec.repositories.BranchRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class BranchesFragment extends Fragment implements BranchAdapter.OnBranchClickListener {

    private EditText etSearch;
    private ImageView imgSearchClear;
    private TextView tvEmpty;
    private RecyclerView rvBranches;
    private BranchAdapter branchAdapter;
    private List<Branch> masterBranchesList = new ArrayList<>();
    private List<Branch> displayedBranches = new ArrayList<>();

    private WebView webViewMap;
    private ProgressBar pbMapLoading;
    private FloatingActionButton btnMyLocation;
    private boolean isMapLoaded = false;

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fine = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
                Boolean coarse = result.get(Manifest.permission.ACCESS_COARSE_LOCATION);
                if ((fine != null && fine) || (coarse != null && coarse)) {
                    locateUserOnMap();
                } else {
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(getContext(), "Permiso de ubicación denegado. Se muestran las sucursales.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_branches, container, false);

        etSearch = view.findViewById(R.id.et_branches_search);
        imgSearchClear = view.findViewById(R.id.img_branches_search_clear);
        rvBranches = view.findViewById(R.id.rv_branches);
        tvEmpty = view.findViewById(R.id.tv_branches_empty);
        webViewMap = view.findViewById(R.id.webview_branches_map);
        pbMapLoading = view.findViewById(R.id.pb_map_loading);
        btnMyLocation = view.findViewById(R.id.btn_map_my_location);

        if (imgSearchClear != null) {
            imgSearchClear.setOnClickListener(v -> etSearch.setText(""));
        }

        setupRecyclerView();
        setupSearchFilter();
        setupInteractiveMap();
        loadBranchesData();

        btnMyLocation.setOnClickListener(v -> requestLocationAndLocate());

        // Ask for location permission when entering the screen
        requestLocationAndLocate();

        return view;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupInteractiveMap() {
        if (webViewMap == null) return;

        WebSettings settings = webViewMap.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setGeolocationEnabled(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(false);

        webViewMap.setWebChromeClient(new WebChromeClient());
        webViewMap.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                isMapLoaded = true;
                if (pbMapLoading != null) pbMapLoading.setVisibility(View.GONE);
                populateMarkersOnMap();
            }
        });

        String leafletHtml = "<!DOCTYPE html>" +
                "<html><head>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no'/>" +
                "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>" +
                "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>" +
                "<style>" +
                "body, html, #map { margin:0; padding:0; height:100%; width:100%; font-family:sans-serif; background:#e8f0fe; }" +
                ".nintec-pin { background:#1846D7; border:2px solid #ffffff; width:16px; height:16px; border-radius:50%; box-shadow:0 2px 6px rgba(0,0,0,0.35); }" +
                ".user-pin { background:#00C853; border:2px solid #ffffff; width:16px; height:16px; border-radius:50%; box-shadow:0 0 10px #00C853; animation:pulse 1.5s infinite; }" +
                "@keyframes pulse { 0% { box-shadow: 0 0 0 0 rgba(0,200,83,0.7); } 70% { box-shadow: 0 0 0 12px rgba(0,200,83,0); } 100% { box-shadow: 0 0 0 0 rgba(0,200,83,0); } }" +
                "</style>" +
                "</head><body>" +
                "<div id='map'></div>" +
                "<script>" +
                "var map = L.map('map', { zoomControl: false }).setView([-16.5000, -68.1500], 13);" +
                "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { maxZoom: 19, attribution: '© OpenStreetMap' }).addTo(map);" +
                "var branchMarkers = {};" +
                "var userMarker = null;" +
                "function addBranch(id, lat, lon, name, address) {" +
                "  var icon = L.divIcon({ className: 'nintec-pin', iconSize: [16, 16], iconAnchor: [8, 8] });" +
                "  var m = L.marker([lat, lon], { icon: icon }).addTo(map);" +
                "  m.bindPopup('<b>' + name + '</b><br><small>' + address + '</small>');" +
                "  branchMarkers[id] = m;" +
                "}" +
                "function focusBranch(lat, lon, id) {" +
                "  map.flyTo([lat, lon], 16, { animate: true, duration: 1.0 });" +
                "  if (branchMarkers[id]) { setTimeout(function() { branchMarkers[id].openPopup(); }, 700); }" +
                "}" +
                "function setUserLoc(lat, lon) {" +
                "  if (userMarker) { map.removeLayer(userMarker); }" +
                "  var icon = L.divIcon({ className: 'user-pin', iconSize: [16, 16], iconAnchor: [8, 8] });" +
                "  userMarker = L.marker([lat, lon], { icon: icon }).addTo(map).bindPopup('<b>Tu ubicación</b>').openPopup();" +
                "  map.flyTo([lat, lon], 15, { animate: true, duration: 1.0 });" +
                "}" +
                "</script></body></html>";

        webViewMap.loadDataWithBaseURL("https://ninteclp.bo", leafletHtml, "text/html", "UTF-8", null);
    }

    private void populateMarkersOnMap() {
        if (!isMapLoaded || webViewMap == null || masterBranchesList.isEmpty()) return;

        for (Branch b : masterBranchesList) {
            String safeName = b.getName() != null ? b.getName().replace("'", "\\'") : "Sucursal NINTEC";
            String safeAddress = b.getAddress() != null ? b.getAddress().replace("'", "\\'") : "";
            String js = "addBranch('" + b.getId() + "', " + b.getLatitude() + ", " + b.getLongitude() + ", '" + safeName + "', '" + safeAddress + "');";
            webViewMap.evaluateJavascript(js, null);
        }
    }

    private void requestLocationAndLocate() {
        Context context = getContext();
        if (context == null) return;

        boolean fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (fine || coarse) {
            locateUserOnMap();
        } else {
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void locateUserOnMap() {
        Context context = getContext();
        if (context == null || webViewMap == null) return;

        try {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (locationManager != null) {
                Location bestLocation = null;
                List<String> providers = locationManager.getProviders(true);
                for (String provider : providers) {
                    @SuppressLint("MissingPermission")
                    Location l = locationManager.getLastKnownLocation(provider);
                    if (l != null && (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy())) {
                        bestLocation = l;
                    }
                }

                if (bestLocation != null) {
                    double lat = bestLocation.getLatitude();
                    double lon = bestLocation.getLongitude();
                    String js = "setUserLoc(" + lat + ", " + lon + ");";
                    webViewMap.evaluateJavascript(js, null);
                    Toast.makeText(context, "Ubicación detectada", Toast.LENGTH_SHORT).show();
                } else {
                    // Fallback to central La Paz view if GPS hardware has not yet locked
                    webViewMap.evaluateJavascript("setUserLoc(-16.5000, -68.1500);", null);
                }
            }
        } catch (Exception e) {
            // Permission or provider fallback
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
                    fetchExternalGeoapify();
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
                    performSearch(etSearch != null ? etSearch.getText().toString() : "");
                    populateMarkersOnMap();
                }
            }

            @Override
            public void onError(String message) {
                if (isAdded()) {
                    performSearch(etSearch != null ? etSearch.getText().toString() : "");
                    populateMarkersOnMap();
                }
            }
        });
    }

    private void setupSearchFilter() {
        if (etSearch == null) return;
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
        String cleanQuery = query == null ? "" : query.trim().toLowerCase();
        displayedBranches.clear();

        if (cleanQuery.isEmpty()) {
            displayedBranches.addAll(masterBranchesList);
        } else {
            for (Branch b : masterBranchesList) {
                if ((b.getName() != null && b.getName().toLowerCase().contains(cleanQuery)) ||
                        (b.getAddress() != null && b.getAddress().toLowerCase().contains(cleanQuery)) ||
                        (b.getCity() != null && b.getCity().toLowerCase().contains(cleanQuery))) {
                    displayedBranches.add(b);
                }
            }
        }

        if (branchAdapter != null) {
            branchAdapter.updateList(displayedBranches);
        }

        if (tvEmpty != null) {
            tvEmpty.setVisibility(displayedBranches.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onBranchClick(Branch branch) {
        if (branch == null || webViewMap == null) return;
        String js = "focusBranch(" + branch.getLatitude() + ", " + branch.getLongitude() + ", '" + branch.getId() + "');";
        webViewMap.evaluateJavascript(js, null);
    }

    @Override
    public void onViewMapClick(Branch branch) {
        if (branch == null) return;
        // Focus in internal map first
        onBranchClick(branch);

        // Also offer opening directly in native Google Maps
        try {
            Uri gmmIntentUri = Uri.parse("geo:" + branch.getLatitude() + "," + branch.getLongitude() +
                    "?q=" + Uri.encode(branch.getName() + ", " + branch.getAddress()));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                Intent genericIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                startActivity(genericIntent);
            }
        } catch (Exception ignored) {}
    }
}
