package com.example.nintec.models;

public class Category {
    private String id;
    private String name;
    private String slug;
    private String iconName;
    private String imageUrl;

    public Category(String id, String name, String slug, String iconName, String imageUrl) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.iconName = iconName;
        this.imageUrl = imageUrl;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public String getIconName() { return iconName; }
    public String getImageUrl() { return imageUrl; }
}
