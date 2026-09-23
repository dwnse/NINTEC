package com.example.nintec.repositories;

import com.example.nintec.models.Product;
import com.example.nintec.models.Category;
import com.example.nintec.models.Banner;
import com.example.nintec.network.SupabaseClient;
import com.example.nintec.network.dto.ProductDto;
import com.example.nintec.network.dto.CategoryDto;
import com.example.nintec.network.dto.BannerDto;
import com.google.gson.JsonObject;

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
    private final Map<String, Map<String, String>> specificationsMap;
    private final Map<String, String> descriptionsMap;

    public interface ProductListCallback {
        void onSuccess(List<Product> products);
        void onError(String error);
    }

    public interface CategoryListCallback {
        void onSuccess(List<Category> categories);
        void onError(String error);
    }

    public interface BannerListCallback {
        void onSuccess(List<Banner> banners);
        void onError(String error);
    }

    public interface ProductCallback {
        void onSuccess(Product product);
        void onError(String error);
    }

    public interface CategoryCallback {
        void onSuccess(Category category);
        void onError(String error);
    }

    public interface SpecificationCallback {
        void onSuccess(Map<String, String> specs);
        void onError(String error);
    }

    private ProductRepository() {
        products = new ArrayList<>();
        specificationsMap = new LinkedHashMap<>();
        descriptionsMap = new LinkedHashMap<>();
    }

    public static synchronized ProductRepository getInstance() {
        if (instance == null) {
            instance = new ProductRepository();
        }
        return instance;
    }

    /**
     * Fetch products from Supabase REST API.
     */
    public void fetchProducts(ProductListCallback callback) {
        // Removed 'sort_order' as it doesn't exist in products table. Using 'name.asc'
        SupabaseClient.getInstance().getDataService()
                .getProducts("*,categories(*),product_images(*)", "eq.true", "name.asc", 100)
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
                            products = fetched;
                            if (callback != null) callback.onSuccess(products);
                        } else {
                            if (callback != null) callback.onError("Error " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<ProductDto>> call, Throwable t) {
                        if (callback != null) callback.onError("Error de red");
                    }
                });
    }

    /**
     * Fetch featured products from Supabase REST API.
     */
    public void fetchFeaturedProducts(ProductListCallback callback) {
        // Removed 'sort_order' as it doesn't exist in products table
        SupabaseClient.getInstance().getDataService()
                .getFeaturedProducts("*,categories(*),product_images(*)", "eq.true", "eq.true", "name.asc", 20)
                .enqueue(new Callback<List<ProductDto>>() {
                    @Override
                    public void onResponse(Call<List<ProductDto>> call, Response<List<ProductDto>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Product> featured = new ArrayList<>();
                            for (ProductDto dto : response.body()) {
                                featured.add(dto.toProduct());
                            }
                            if (callback != null) callback.onSuccess(featured);
                        } else {
                            if (callback != null) callback.onError("Error " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<ProductDto>> call, Throwable t) {
                        if (callback != null) callback.onError("Error de red");
                    }
                });
    }

    /**
     * Fetch categories from Supabase REST API.
     */
    public void fetchCategories(CategoryListCallback callback) {
        SupabaseClient.getInstance().getDataService()
                .getCategories("eq.true", "sort_order.asc", "*")
                .enqueue(new Callback<List<CategoryDto>>() {
                    @Override
                    public void onResponse(Call<List<CategoryDto>> call, Response<List<CategoryDto>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Category> categories = new ArrayList<>();
                            for (CategoryDto dto : response.body()) {
                                categories.add(new Category(dto.id, dto.name, dto.slug, dto.iconName, dto.imageUrl));
                            }
                            if (callback != null) callback.onSuccess(categories);
                        } else {
                            if (callback != null) callback.onError("Error " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<CategoryDto>> call, Throwable t) {
                        if (callback != null) callback.onError("Error de red");
                    }
                });
    }

    /**
     * Fetch banners from Supabase REST API.
     */
    public void fetchBanners(BannerListCallback callback) {
        SupabaseClient.getInstance().getDataService()
                .getBanners("eq.true", "sort_order.asc", "*")
                .enqueue(new Callback<List<BannerDto>>() {
                    @Override
                    public void onResponse(Call<List<BannerDto>> call, Response<List<BannerDto>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Banner> banners = new ArrayList<>();
                            for (BannerDto dto : response.body()) {
                                banners.add(dto.toBanner());
                            }
                            if (callback != null) callback.onSuccess(banners);
                        } else {
                            if (callback != null) callback.onError("Error " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<BannerDto>> call, Throwable t) {
                        if (callback != null) callback.onError("Error de red");
                    }
                });
    }

    /**
     * Fetch product specifications from Supabase REST API.
     */
    public void fetchProductSpecifications(String productId, SpecificationCallback callback) {
        SupabaseClient.getInstance().getDataService()
                .getProductSpecifications("eq." + productId, "sort_order.asc", "*")
                .enqueue(new Callback<List<JsonObject>>() {
                    @Override
                    public void onResponse(Call<List<JsonObject>> call, Response<List<JsonObject>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Map<String, String> specs = new LinkedHashMap<>();
                            for (JsonObject obj : response.body()) {
                                if (obj.has("spec_key") && obj.has("spec_value")) {
                                    specs.put(obj.get("spec_key").getAsString(), obj.get("spec_value").getAsString());
                                }
                            }
                            if (callback != null) callback.onSuccess(specs);
                        } else {
                            if (callback != null) callback.onError("Error " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<JsonObject>> call, Throwable t) {
                        if (callback != null) callback.onError("Error de red");
                    }
                });
    }

    public void createProduct(Map<String, Object> body, ProductCallback callback) {
        SupabaseClient.getInstance().getDataService()
                .createProduct(body, "return=representation")
                .enqueue(new Callback<List<ProductDto>>() {
                    @Override
                    public void onResponse(Call<List<ProductDto>> call, Response<List<ProductDto>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            if (callback != null) callback.onSuccess(response.body().get(0).toProduct());
                        } else {
                            if (callback != null) callback.onError("Error " + response.code());
                        }
                    }
                    @Override
                    public void onFailure(Call<List<ProductDto>> call, Throwable t) {
                        if (callback != null) callback.onError("Error de red");
                    }
                });
    }

    public void updateProduct(String id, Map<String, Object> update, ProductCallback callback) {
        SupabaseClient.getInstance().getDataService()
                .updateProduct("id=eq." + id, update)
                .enqueue(new Callback<List<ProductDto>>() {
                    @Override
                    public void onResponse(Call<List<ProductDto>> call, Response<List<ProductDto>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            if (callback != null) callback.onSuccess(response.body().get(0).toProduct());
                        } else {
                            if (callback != null) callback.onError("Error " + response.code());
                        }
                    }
                    @Override
                    public void onFailure(Call<List<ProductDto>> call, Throwable t) {
                        if (callback != null) callback.onError("Error de red");
                    }
                });
    }

    public void deleteProduct(String id, VoidCallback callback) {
        SupabaseClient.getInstance().getDataService()
                .deleteProduct("id=eq." + id)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            if (callback != null) callback.onSuccess();
                        } else {
                            if (callback != null) callback.onError("Error " + response.code());
                        }
                    }
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        if (callback != null) callback.onError("Error de red");
                    }
                });
    }

    public void createCategory(Map<String, Object> body, CategoryCallback callback) {
        SupabaseClient.getInstance().getDataService()
                .createCategory(body, "return=representation")
                .enqueue(new Callback<List<CategoryDto>>() {
                    @Override
                    public void onResponse(Call<List<CategoryDto>> call, Response<List<CategoryDto>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            CategoryDto dto = response.body().get(0);
                            if (callback != null) callback.onSuccess(new Category(dto.id, dto.name, dto.slug, dto.iconName, dto.imageUrl));
                        } else {
                            if (callback != null) callback.onError("Error " + response.code());
                        }
                    }
                    @Override
                    public void onFailure(Call<List<CategoryDto>> call, Throwable t) {
                        if (callback != null) callback.onError("Error de red");
                    }
                });
    }

    public void updateCategory(String id, Map<String, Object> update, CategoryCallback callback) {
        SupabaseClient.getInstance().getDataService()
                .updateCategory("id=eq." + id, update)
                .enqueue(new Callback<List<CategoryDto>>() {
                    @Override
                    public void onResponse(Call<List<CategoryDto>> call, Response<List<CategoryDto>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            CategoryDto dto = response.body().get(0);
                            if (callback != null) callback.onSuccess(new Category(dto.id, dto.name, dto.slug, dto.iconName, dto.imageUrl));
                        } else {
                            if (callback != null) callback.onError("Error " + response.code());
                        }
                    }
                    @Override
                    public void onFailure(Call<List<CategoryDto>> call, Throwable t) {
                        if (callback != null) callback.onError("Error de red");
                    }
                });
    }

    public void deleteCategory(String id, VoidCallback callback) {
        SupabaseClient.getInstance().getDataService()
                .deleteCategory("id=eq." + id)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            if (callback != null) callback.onSuccess();
                        } else {
                            if (callback != null) callback.onError("Error " + response.code());
                        }
                    }
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        if (callback != null) callback.onError("Error de red");
                    }
                });
    }

    public interface VoidCallback {
        void onSuccess();
        void onError(String error);
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
        return descriptionsMap.getOrDefault(id, "Sin descripción disponible.");
    }

    public Map<String, String> getSpecificationsById(String id) {
        if (specificationsMap.containsKey(id)) return specificationsMap.get(id);
        Map<String, String> defaultSpecs = new LinkedHashMap<>();
        defaultSpecs.put("Garantía", "12 meses oficial NINTECLP");
        defaultSpecs.put("Disponibilidad", "Inmediata");
        return defaultSpecs;
    }
}
