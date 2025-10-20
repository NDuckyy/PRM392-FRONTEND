package com.example.prm392_frontend.models;

import com.google.gson.annotations.SerializedName;

public class CartItemUpdateRequest {

    @SerializedName("quantity")
    private Integer quantity;

    // Constructors
    public CartItemUpdateRequest() {
        // Constructor rỗng
    }

    public CartItemUpdateRequest(Integer quantity) {
        this.quantity = quantity;
    }

    // Getter and Setter
    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
