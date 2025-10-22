package com.example.prm392_frontend.models;

import java.util.Date;

public class Order {
    private String code;
    private Date createdAt;
    private String status;
    private long total;
    private int items;

    public Order(String code, Date createdAt, String status, long total, int items) {
        this.code = code; this.createdAt = createdAt; this.status = status;
        this.total = total; this.items = items;
    }

    public String getCode() { return code; }
    public Date getCreatedAt() { return createdAt; }
    public String getStatus() { return status; }
    public long getTotal() { return total; }
    public int getItems() { return items; }
}
