package com.example.nintec.network;

import com.example.nintec.network.dto.BannerDto;
import com.example.nintec.network.dto.BranchDto;
import com.example.nintec.network.dto.CartItemDto;
import com.example.nintec.network.dto.CategoryDto;
import com.example.nintec.network.dto.OrderDto;
import com.example.nintec.network.dto.PaymentMethodDto;
import com.example.nintec.network.dto.ProductDto;
import com.example.nintec.network.dto.AppSettingDto;
import com.example.nintec.network.dto.ProfileDto;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Header;
import retrofit2.http.Query;

/**
 * Retrofit service interface for Supabase PostgREST data endpoints.
 */
public interface SupabaseDataService {

    // ─── Categories ───

    @GET("rest/v1/categories")
    Call<List<CategoryDto>> getCategories(
            @Query("is_active") String isActive,
            @Query("order") String order,
            @Query("select") String select
    );

    @POST("rest/v1/categories")
    Call<List<CategoryDto>> createCategory(@Body Map<String, Object> body, @Header("Prefer") String prefer);

    @PATCH("rest/v1/categories")
    Call<List<CategoryDto>> updateCategory(@Query("id") String idFilter, @Body Map<String, Object> update);

    @DELETE("rest/v1/categories")
    Call<Void> deleteCategory(@Query("id") String idFilter);

    // ─── Products ───

    @GET("rest/v1/products")
    Call<List<ProductDto>> getProducts(
            @Query("select") String select,
            @Query("is_active") String isActive,
            @Query("order") String order,
            @Query("limit") int limit
    );

    @POST("rest/v1/products")
    Call<List<ProductDto>> createProduct(@Body Map<String, Object> body, @Header("Prefer") String prefer);

    @PATCH("rest/v1/products")
    Call<List<ProductDto>> updateProduct(@Query("id") String idFilter, @Body Map<String, Object> update);

    @DELETE("rest/v1/products")
    Call<Void> deleteProduct(@Query("id") String idFilter);

    @GET("rest/v1/products")
    Call<List<ProductDto>> getProductsByCategory(
            @Query("select") String select,
            @Query("is_active") String isActive,
            @Query("category_id") String categoryId,
            @Query("order") String order
    );

    @GET("rest/v1/products")
    Call<List<ProductDto>> getProductById(
            @Query("select") String select,
            @Query("id") String idFilter
    );

    @GET("rest/v1/products")
    Call<List<ProductDto>> searchProducts(
            @Query("select") String select,
            @Query("is_active") String isActive,
            @Query("name") String nameFilter,
            @Query("order") String order
    );

    @GET("rest/v1/products")
    Call<List<ProductDto>> getFeaturedProducts(
            @Query("select") String select,
            @Query("is_active") String isActive,
            @Query("is_featured") String isFeatured,
            @Query("order") String order,
            @Query("limit") int limit
    );

    // ─── Product Specifications ───

    @GET("rest/v1/product_specifications")
    Call<List<JsonObject>> getProductSpecifications(
            @Query("product_id") String productIdFilter,
            @Query("order") String order,
            @Query("select") String select
    );

    // ─── Product Images ───

    @GET("rest/v1/product_images")
    Call<List<JsonObject>> getProductImages(
            @Query("product_id") String productIdFilter,
            @Query("order") String order,
            @Query("select") String select
    );

    // ─── Branch Inventory ───

    @GET("rest/v1/branch_inventory")
    Call<List<JsonObject>> getBranchInventory(
            @Query("product_id") String productIdFilter,
            @Query("select") String select
    );

    // ─── Branches ───

    @GET("rest/v1/branches")
    Call<List<BranchDto>> getBranches(
            @Query("is_active") String isActive,
            @Query("order") String order,
            @Query("select") String select
    );

    @GET("rest/v1/branches")
    Call<List<BranchDto>> searchBranches(
            @Query("is_active") String isActive,
            @Query("or") String orFilter,
            @Query("select") String select
    );

    // ─── Cart Items ───

    @GET("rest/v1/cart_items")
    Call<List<CartItemDto>> getCartItems(
            @Query("select") String select,
            @Query("user_id") String userIdFilter,
            @Query("order") String order
    );

    @POST("rest/v1/cart_items")
    Call<List<CartItemDto>> addCartItem(
            @Body CartItemDto item,
            @Header("Prefer") String prefer
    );

    @PATCH("rest/v1/cart_items")
    Call<List<CartItemDto>> updateCartItem(
            @Query("id") String idFilter,
            @Body Map<String, Object> update
    );

    @DELETE("rest/v1/cart_items")
    Call<Void> deleteCartItem(@Query("id") String idFilter);

    @DELETE("rest/v1/cart_items")
    Call<Void> clearCart(@Query("user_id") String userIdFilter);

    // ─── Orders ───

    @GET("rest/v1/orders")
    Call<List<OrderDto>> getOrders(
            @Query("select") String select,
            @Query("user_id") String userIdFilter,
            @Query("order") String order
    );

    @GET("rest/v1/orders")
    Call<List<OrderDto>> getOrderById(
            @Query("select") String select,
            @Query("id") String idFilter
    );

    // ─── Order Items ───

    @GET("rest/v1/order_items")
    Call<List<JsonObject>> getOrderItems(
            @Query("order_id") String orderIdFilter,
            @Query("select") String select
    );

    // ─── RPCs (Server Functions) ───

    @POST("rest/v1/rpc/create_order")
    Call<String> createOrder(@Body Map<String, Object> params);

    @POST("rest/v1/rpc/validate_coupon")
    Call<List<JsonObject>> validateCoupon(@Body Map<String, Object> params);

    @POST("rest/v1/rpc/get_app_config")
    Call<List<AppSettingDto>> getAppConfig();

    @POST("rest/v1/rpc/get_unread_notifications")
    Call<List<JsonObject>> getUnreadNotifications();

    // ─── Payment Methods ───

    @GET("rest/v1/payment_methods")
    Call<List<PaymentMethodDto>> getPaymentMethods(
            @Query("is_active") String isActive,
            @Query("order") String order,
            @Query("select") String select
    );

    // ─── Banners ───

    @GET("rest/v1/banners")
    Call<List<BannerDto>> getBanners(
            @Query("is_active") String isActive,
            @Query("order") String order,
            @Query("select") String select
    );

    // ─── App Settings ───

    @GET("rest/v1/app_settings")
    Call<List<AppSettingDto>> getAppSettings(
            @Query("is_public") String isPublic,
            @Query("select") String select
    );

    // ─── Profiles ───

    @GET("rest/v1/profiles")
    Call<List<ProfileDto>> getProfile(
            @Query("id") String idFilter,
            @Query("select") String select
    );

    @GET("rest/v1/profiles")
    Call<List<ProfileDto>> getProfileByEmail(
            @Query("email") String emailFilter,
            @Query("select") String select
    );

    @PATCH("rest/v1/profiles")
    Call<List<ProfileDto>> updateProfile(
            @Query("id") String idFilter,
            @Body Map<String, Object> update
    );

    @PATCH("rest/v1/profiles")
    Call<List<ProfileDto>> updateProfileByEmail(
            @Query("email") String emailFilter,
            @Body Map<String, Object> update
    );

    // ─── Direct Order Insert (REST) ───

    @POST("rest/v1/orders")
    Call<List<OrderDto>> insertOrder(
            @Body Map<String, Object> body,
            @Header("Prefer") String prefer
    );

    @POST("rest/v1/order_items")
    Call<Void> insertOrderItem(
            @Body Map<String, Object> body,
            @Header("Prefer") String prefer
    );
}

