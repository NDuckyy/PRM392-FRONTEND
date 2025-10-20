package com.example.prm392_frontend.models;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal; // Sử dụng BigDecimal để xử lý tiền tệ chính xác

public class CartItemUpdateResponse {

    @SerializedName("message")
    private String message;

    @SerializedName("cartId")
    private Long cartId;

    @SerializedName("totalPrice")
    private BigDecimal totalPrice;

    // Constructors
    public CartItemUpdateResponse() {
        // Constructor rỗng cần thiết cho Gson
    }

    public CartItemUpdateResponse(String message, Long cartId, BigDecimal totalPrice) {
        this.message = message;
        this.cartId = cartId;
        this.totalPrice = totalPrice;
    }

    // Getters
    public String getMessage() {
        return message;
    }

    public Long getCartId() {
        return cartId;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }
}
