package com.example.prm392_frontend.models;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class OrderResponse {
    @SerializedName("orderId")
    private int orderId;

    @SerializedName("userId")
    private int userId;

    @SerializedName("totalPrice")
    private double totalPrice;

    @SerializedName("paymentMethod")
    private String paymentMethod;

    @SerializedName("billingAddress")
    private String billingAddress;

    @SerializedName("orderStatus")
    private String orderStatus;

    @SerializedName("orderDate")
    private Date orderDate;

    // Trường quan trọng nhất cho luồng thanh toán VNPAY
    @SerializedName("paymentUrl")
    private String paymentUrl;


    public OrderResponse(int orderId, int userId, double totalPrice, String paymentMethod, String billingAddress, String orderStatus, Date orderDate, String paymentUrl) {
        this.orderId = orderId;
        this.userId = userId;
        this.totalPrice = totalPrice;
        this.paymentMethod = paymentMethod;
        this.billingAddress = billingAddress;
        this.orderStatus = orderStatus;
        this.orderDate = orderDate;
        this.paymentUrl = paymentUrl;
    }


    // Getters
    public int getOrderId() {
        return orderId;
    }

    public int getUserId() {
        return userId;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getBillingAddress() {
        return billingAddress;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public String getPaymentUrl() {
        return paymentUrl;
    }
}
