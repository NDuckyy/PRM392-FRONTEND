package com.example.prm392_frontend.models;

public class RegisterProviderRequest {
    private String name;

    public RegisterProviderRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
