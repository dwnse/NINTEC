package com.example.nintec.repositories;

import com.example.nintec.R;
import com.example.nintec.models.Product;
import com.example.nintec.network.SupabaseClient;
import com.example.nintec.network.dto.ProductDto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductRepository {

    private static ProductRepository instance;
    private List<Product> products;
    private Map<String, Map<String, String>> specificationsMap;
    private Map<String, String> descriptionsMap;

    public interface ProductListCallback {
        void onSuccess(List<Product> products);
        void onError(String error);
    }

    public interface ProductCallback {
        void onSuccess(Product product);
        void onError(String error);
    }

    private ProductRepository() {
        products = new ArrayList<>();
        specificationsMap = new LinkedHashMap<>();
        descriptionsMap = new LinkedHashMap<>();
        initFallbackData();
    }

    public static synchronized ProductRepository getInstance() {
        if (instance == null) {
            instance = new ProductRepository();
        }
        return instance;
    }

    private void initFallbackData() {
        // Hydrate uniform master dataset catalog with proper types
        products.add(new Product("1", "MacBook Pro M3 Max", "Laptops", 2499.00, 2999.00, R.mipmap.ic_launcher_foreground, true, 5));
        products.add(new Product("2", "iPhone 15 Pro Titanium", "Celulares", 1099.00, 1199.00, R.mipmap.ic_launcher_foreground, true, 8));
        products.add(new Product("3", "Audífonos Sony WH-1000XM5", "Audio", 349.00, null, R.mipmap.ic_launcher_foreground, false, 12));
        products.add(new Product("4", "Teclado Mecánico Nintec RGB", "Accesorios", 89.00, 120.00, R.mipmap.ic_launcher_foreground, false, 20));
        products.add(new Product("5", "Laptop ASUS ROG Strix", "Laptops", 1899.00, null, R.mipmap.ic_launcher_foreground, false, 0));
        products.add(new Product("6", "Samsung Galaxy S24 Ultra", "Celulares", 1299.00, 1399.00, R.mipmap.ic_launcher_foreground, true, 4));
        products.add(new Product("7", "Mouse Gamer Inalámbrico", "Accesorios", 59.00, 75.00, R.mipmap.ic_launcher_foreground, false, 15));
        products.add(new Product("8", "Parlante JBL Flip 6", "Audio", 119.00, null, R.mipmap.ic_launcher_foreground, false, 10));

        // Descriptions setup
        descriptionsMap.put("1", "La MacBook Pro de 14 pulgadas con chip M3 Max vuela en flujos de trabajo extremos para programadores y diseñadores.");
        descriptionsMap.put("2", "Forjado en titanio, el iPhone 15 Pro estrena chip A17 Pro revolucionario y sistema de cámaras avanzado.");
        descriptionsMap.put("3", "Audífonos inalámbricos premium con cancelación de ruido inteligente líder en la industria de audio profesional.");
        descriptionsMap.put("4", "Teclado mecánico ultra-responsivo con switches brown de alta durabilidad y retroiluminación RGB dinámica.");
        descriptionsMap.put("5", "Rendimiento gaming extremo con procesador de última generación y refrigeración inteligente avanzada.");
        descriptionsMap.put("6", "El buque insignia de Samsung con cámara de 200MP, inteligencia artificial Galaxy AI y S Pen integrado.");
        descriptionsMap.put("7", "Mouse gamer con sensor óptico de alta precisión de hasta 16000 DPI y conexión libre de latencia.");
        descriptionsMap.put("8", "Parlante portátil resistente al agua IP67 con un sonido potente, nítido y graves profundos optimizados.");

        // Specifications builder
        Map<String, String> spec1 = new LinkedHashMap<>();
        spec1.put("Procesador", "Apple M3 Max 14-core");
        spec1.put("Memoria RAM", "36 GB Unified");
        spec1.put("Almacenamiento", "1 TB NVMe SSD");
        spec1.put("Pantalla", "14.2\" Liquid Retina XDR");
        specificationsMap.put("1", spec1);

        Map<String, String> spec2 = new LinkedHashMap<>();
        spec2.put("Procesador", "Apple A17 Pro");
        spec2.put("Almacenamiento", "256 GB");
        spec2.put("Pantalla", "6.1\" Super Retina XDR");
        spec2.put("Material", "Titanio de grado aeroespacial");
        specificationsMap.put("2", spec2);

        Map<String, String> spec3 = new LinkedHashMap<>();
        spec3.put("Conectividad", "Bluetooth 5.2 / Jack 3.5mm");
        spec3.put("Autonomía", "Hasta 30 horas continuas");
        spec3.put("Cancelación Ruido", "Active Noise Cancelling (ANC)");
        specificationsMap.put("3", spec3);

        Map<String, String> spec4 = new LinkedHashMap<>();
        spec4.put("Tipo Switch", "Mecánico Brown");
        spec4.put("Formato", "TKL (Tenkeyless 80%)");
        spec4.put("Iluminación", "RGB Custom por tecla");
        specificationsMap.put("4", spec4);
    }

    /**
     * Fetch products from Supabase REST API.
     */
    public void fetchProducts(ProductListCallback callback) {
        SupabaseClient.getInstance().getDataService()
                .getProducts("*,categories(*),product_images(*)", "eq.true", "sort_order.asc,name.asc", 100)
                .enqueue(new Callback<List<ProductDto>>() {
                    @Override
                    public void onResponse(Call<List<ProductDto>> call, Response<List<ProductDto>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Product> fetched = new ArrayList<>();
                            for (ProductDto dto : response.body()) {
                                Product p = dto.toProduct();
                                fetched.add(p);
                                if (dto.description != null) {
                                    descriptionsMap.put(p.getId(), dto.description);
                                }
                            }
                            if (!fetched.isEmpty()) {
                                products = fetched;
                            }
                            if (callback != null) callback.onSuccess(products);
                        } else {
                            if (callback != null) callback.onSuccess(products); // fallback to cached
                        }
                    }

                    @Override
                    public void onFailure(Call<List<ProductDto>> call, Throwable t) {
                        if (callback != null) callback.onSuccess(products); // fallback to cached
                    }
                });
    }

    /**
     * Fetch featured products from Supabase REST API.
     */
    public void fetchFeaturedProducts(ProductListCallback callback) {
        SupabaseClient.getInstance().getDataService()
                .getFeaturedProducts("*,categories(*),product_images(*)", "eq.true", "eq.true", "sort_order.asc", 20)
                .enqueue(new Callback<List<ProductDto>>() {
                    @Override
                    public void onResponse(Call<List<ProductDto>> call, Response<List<ProductDto>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            List<Product> featured = new ArrayList<>();
                            for (ProductDto dto : response.body()) {
                                featured.add(dto.toProduct());
                            }
                            if (callback != null) callback.onSuccess(featured);
                        } else {
                            if (callback != null) callback.onSuccess(products);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<ProductDto>> call, Throwable t) {
                        if (callback != null) callback.onSuccess(products);
                    }
                });
    }

    public List<Product> getProducts() {
        return products;
    }

    public Product getProductById(String id) {
        for (Product p : products) {
            if (p.getId().equals(id)) return p;
        }
        return null;
    }

    public String getDescriptionById(String id) {
        Product p = getProductById(id);
        if (p != null && p.getDescription() != null && !p.getDescription().isEmpty()) {
            return p.getDescription();
        }
        return descriptionsMap.containsKey(id) ? descriptionsMap.get(id) : "Sin descripción disponible.";
    }

    public Map<String, String> getSpecificationsById(String id) {
        if (specificationsMap.containsKey(id)) return specificationsMap.get(id);
        Map<String, String> defaultSpecs = new LinkedHashMap<>();
        defaultSpecs.put("Garantía", "12 meses oficial NINTECLP");
        defaultSpecs.put("Disponibilidad", "Inmediata");
        return defaultSpecs;
    }
}