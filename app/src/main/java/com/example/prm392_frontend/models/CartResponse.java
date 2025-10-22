package com.example.prm392_frontend.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CartResponse {
    @SerializedName("id")
    private int id;

    @SerializedName("userId")
    private int userId;

    @SerializedName("totalPrice")
    private double totalPrice;

    @SerializedName("cartItemResponses")
    private List<CartItemResponse> cartItemResponses;


    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public List<CartItemResponse> getCartItemResponses() {
        return cartItemResponses;
    }}
