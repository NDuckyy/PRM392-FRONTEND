package com.example.prm392_frontend.models;

public class OrderRequest {
    private String billingAddress;
    private String paymentMethod;

    // Bắt buộc cần có constructor để khởi tạo
    public OrderRequest(String billingAddress, String paymentMethod) {
        this.billingAddress = billingAddress;
        this.paymentMethod = paymentMethod;
    }

    // (Getter và Setter nếu cần)
    public String getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(String billingAddress) {
        this.billingAddress = billingAddress;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
