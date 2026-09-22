package com.example.nintec.models;

public class Product {
    private String id;
    private String name;
    private String category;
    private double price;
    private Double oldPrice;
    private String imageUrl;
    private boolean isNew;
    private int stock;
    private String description;
    private String brand;
    private String slug;
    private String categoryId;

    private int imageResource = 0;

    // Constructor for data from Supabase (URL)
    public Product(String id, String name, String category, double price, Double oldPrice,
                   String imageUrl, boolean isNew, int stock) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.oldPrice = oldPrice;
        this.imageUrl = imageUrl;
        this.isNew = isNew;
        this.stock = stock;
    }

    // Constructor for local resources (fallback/mock)
    public Product(String id, String name, String category, double price, Double oldPrice,
                   int imageResource, boolean isNew, int stock) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.oldPrice = oldPrice;
        this.imageResource = imageResource;
        this.isNew = isNew;
        this.stock = stock;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }

    /**
     * Returns the price formatted as "Bs X.XXX,XX" for display.
     */
    public String getPrice() {
        return formatPrice(price);
    }

    /**
     * Returns the raw numeric price.
     */
    public double getPriceValue() { return price; }

    /**
     * Returns the old price formatted as "Bs X.XXX,XX" for display, or null if no discount.
     */
    public String getOldPrice() {
        if (oldPrice == null || oldPrice <= 0) return null;
        return formatPrice(oldPrice);
    }

    public Double getOldPriceValue() { return oldPrice; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getImageResource() { return imageResource; }
    public void setImageResource(int imageResource) { this.imageResource = imageResource; }

    public boolean isNew() { return isNew; }
    public int getStock() { return stock; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    /**
     * Format a numeric price value to Bolivian format "Bs X.XXX,XX"
     */
    public static String formatPrice(double value) {
        try {
            java.text.DecimalFormatSymbols symbols = new java.text.DecimalFormatSymbols();
            symbols.setGroupingSeparator('.');
            symbols.setDecimalSeparator(',');
            java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0.00", symbols);
            return "Bs " + df.format(value);
        } catch (Exception e) {
            return String.format(java.util.Locale.US, "Bs %.2f", value);
        }
    }
}