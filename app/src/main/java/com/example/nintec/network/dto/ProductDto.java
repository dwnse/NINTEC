package com.example.nintec.network.dto;

import com.google.gson.annotations.SerializedName;

public class ProductDto {
    @SerializedName("id")
    public String id;

    @SerializedName("name")
    public String name;

    @SerializedName("slug")
    public String slug;

    @SerializedName("description")
    public String description;

    @SerializedName("price")
    public double price;

    @SerializedName("old_price")
    public Double oldPrice;

    @SerializedName("is_new")
    public boolean isNew;

    @SerializedName("is_featured")
    public boolean isFeatured;

    @SerializedName("is_active")
    public boolean isActive;

    @SerializedName("sku")
    public String sku;

    @SerializedName("brand")
    public String brand;

    @SerializedName("category_id")
    public String categoryId;

    // Embedded via PostgREST select
    @SerializedName("categories")
    public CategoryDto category;

    // Embedded via PostgREST select
    @SerializedName("product_images")
    public java.util.List<ProductImageDto> images;

    public static class ProductImageDto {
        @SerializedName("id")
        public String id;

        @SerializedName("image_url")
        public String imageUrl;

        @SerializedName("is_primary")
        public boolean isPrimary;

        @SerializedName("sort_order")
        public int sortOrder;
    }

    public com.example.nintec.models.Product toProduct() {
        String catName = category != null && category.name != null ? category.name : "General";
        String primaryImg = null;
        if (images != null && !images.isEmpty()) {
            for (ProductImageDto img : images) {
                if (img.isPrimary) {
                    primaryImg = img.imageUrl;
                    break;
                }
            }
            if (primaryImg == null) {
                primaryImg = images.get(0).imageUrl;
            }
        }
        com.example.nintec.models.Product p = new com.example.nintec.models.Product(
                id, name, catName, price, oldPrice, primaryImg, isNew, 10
        );
        p.setDescription(description);
        p.setBrand(brand);
        p.setSlug(slug);
        p.setCategoryId(categoryId);
        return p;
    }
}
