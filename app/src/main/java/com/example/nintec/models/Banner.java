package com.example.nintec.models;

public class Banner {
    private String id;
    private String title;
    private String subtitle;
    private String imageUrl;
    private String linkUrl;
    private String targetType;
    private String targetId;
    private int sortOrder;

    public Banner(String id, String title, String subtitle, String imageUrl, String linkUrl, String targetType, String targetId, int sortOrder) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.imageUrl = imageUrl;
        this.linkUrl = linkUrl;
        this.targetType = targetType;
        this.targetId = targetId;
        this.sortOrder = sortOrder;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public String getImageUrl() { return imageUrl; }
    public String getLinkUrl() { return linkUrl; }
    public String getTargetType() { return targetType; }
    public String getTargetId() { return targetId; }
    public int getSortOrder() { return sortOrder; }
}
