package com.example.nintec.models;

public class PaymentMethod {
    private String id;
    private String name;
    private String code;
    private String description;
    private String iconName;
    private String imageUrl;
    private String instructions;
    private boolean requiresProof;

    public PaymentMethod(String id, String name, String code, String description, String iconName, String imageUrl, String instructions, boolean requiresProof) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.description = description;
        this.iconName = iconName;
        this.imageUrl = imageUrl;
        this.instructions = instructions;
        this.requiresProof = requiresProof;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getCode() { return code; }
    public String getDescription() { return description; }
    public String getIconName() { return iconName; }
    public String getImageUrl() { return imageUrl; }
    public String getInstructions() { return instructions; }
    public boolean isRequiresProof() { return requiresProof; }
}
