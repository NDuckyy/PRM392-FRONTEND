package com.example.prm392_frontend.models;

import com.google.gson.annotations.SerializedName;

public class ProductResponse {
    @SerializedName("id")
    private int id;

    @SerializedName("productName")
    private String productName;

    @SerializedName("briefDescription")
    private String briefDescription;

    @SerializedName("fullDescription")
    private String fullDescription;

    @SerializedName("technicalSpecifications")
    private String technicalSpecifications;

    @SerializedName("price")
    private double price;

    @SerializedName("imageURL")
    private String imageURL;

    @SerializedName("categoryID")
    private CategoryResponse categoryID;

    @SerializedName("provider")
    private Provider provider;

    @SerializedName("stockQuantity")
    private Integer stockQuantity;

    // Getters
    public int getId() {
        return id;
    }

    public String getProductName() {
        return productName;
    }

    public String getBriefDescription() {
        return briefDescription;
    }

    public String getFullDescription() {
        return fullDescription;
    }

    public String getTechnicalSpecifications() {
        return technicalSpecifications;
    }

    public double getPrice() {
        return price;
    }

    public String getImageURL() {
        return imageURL;
    }

    public CategoryResponse getCategoryID() {
        return categoryID;
    }

    public Provider getProvider() {
        return provider;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setBriefDescription(String briefDescription) {
        this.briefDescription = briefDescription;
    }

    public void setFullDescription(String fullDescription) {
        this.fullDescription = fullDescription;
    }

    public void setTechnicalSpecifications(String technicalSpecifications) {
        this.technicalSpecifications = technicalSpecifications;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public void setCategoryID(CategoryResponse categoryID) {
        this.categoryID = categoryID;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

}
