package com.example.nintec.models;

public class Branch {
    private String id;
    private String name;
    private String address;
    private String city;
    private double latitude;
    private double longitude;
    private String phone;
    private String imageUrl;
    private String openingHours;

    public Branch(String id, String name, String address, double latitude, double longitude, String phone) {
        this(id, name, address, latitude, longitude, phone, "09:00 - 19:00");
    }

    public Branch(String id, String name, String address, double latitude, double longitude, String phone, String openingHours) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.phone = phone;
        this.openingHours = openingHours;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getPhone() { return phone; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getOpeningHours() {
        return openingHours != null ? openingHours : "09:00 - 19:00";
    }
    public void setOpeningHours(String openingHours) {
        this.openingHours = openingHours;
    }
}