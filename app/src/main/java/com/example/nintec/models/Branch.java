package com.example.nintec.models;

public class Branch {
    private String id;
    private String name;
    private String address;
    private double latitude;
    private double longitude;
    private String phone;
    private String openingHours;

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
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getPhone() { return phone; }
    public String getOpeningHours() { return openingHours; }
}