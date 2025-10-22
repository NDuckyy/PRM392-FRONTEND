package com.example.app.models;

import java.io.Serializable;

public class User implements Serializable {
    private Integer id;
    private String username;
    private String email;
    private String phoneNumber;
    private String address;
    private String role;

    public User() { }

    public User(Integer id, String username, String email, String phoneNumber, String address, String role) {
        this.id = id; this.username = username; this.email = email;
        this.phoneNumber = phoneNumber; this.address = address; this.role = role;
    }

    public Integer getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getAddress() { return address; }
    public String getRole() { return role; }

    public void setId(Integer id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setAddress(String address) { this.address = address; }
    public void setRole(String role) { this.role = role; }
}
