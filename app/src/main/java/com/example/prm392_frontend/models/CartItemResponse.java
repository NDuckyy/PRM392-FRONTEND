package com.example.prm392_frontend.models;

import com.google.gson.annotations.SerializedName;

public class CartItemResponse {
    @SerializedName("id")
    private int id;

    @SerializedName("productId")
    private int productId;

    @SerializedName("productName")
    private String productName;

    @SerializedName("imageURL")
    private String imageUrl;

    @SerializedName("price")
    private double price;

    @SerializedName("quantity")
    private int quantity;


    public int getId() {
        return id;
    }

    public int getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }
}
