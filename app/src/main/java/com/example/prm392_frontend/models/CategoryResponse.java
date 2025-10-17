package com.example.prm392_frontend.models;

import com.google.gson.annotations.SerializedName;

public class CategoryResponse {
    @SerializedName("id")
    private int id;

    @SerializedName("categoryName")
    private String categoryName;

    // Getters
    public int getId() {
        return id;
    }

    public String getCategoryName() {
        return categoryName;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
