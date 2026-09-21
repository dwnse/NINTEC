package com.example.nintec.models;

public class Product {
    private String id;
    private String name;
    private String category;
    private String price;
    private String oldPrice;
    private int imageResource;
    private boolean isNew;
    private int stock;

    public Product(String id, String name, String category, String price, String oldPrice, int imageResource, boolean isNew, int stock) {
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
    public String getPrice() { return price; }
    public String getOldPrice() { return oldPrice; }
    public int getImageResource() { return imageResource; }
    public boolean isNew() { return isNew; }
    public int getStock() { return stock; }
}