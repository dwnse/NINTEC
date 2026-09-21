package com.example.nintec.models;

public class User {
    private String id;
    private String name;
    private String username;
    private String email;
    private int avatarResource;

    public User(String id, String name, String username, String email, int avatarResource) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.email = email;
        this.avatarResource = avatarResource;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public int getAvatarResource() { return avatarResource; }
}