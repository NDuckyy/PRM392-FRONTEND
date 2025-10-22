package com.example.prm392_frontend.models;

import com.google.gson.annotations.SerializedName;

public class Provider {
    @SerializedName("id")
    private int id;

    @SerializedName("providerName")
    private String providerName;

    @SerializedName("user")
    private User user;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
