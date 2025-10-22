package com.example.prm392_frontend.models;

public class CreateOrUpdateProductRequest {
    private String productName;
    private String briefDescription;
    private String fullDescription;
    private String technicalSpecifications;
    private Double price;
    private String imageURL;
    private Integer categoryId;

    public CreateOrUpdateProductRequest() {
    }

    public CreateOrUpdateProductRequest(String productName, String briefDescription, String fullDescription,
                                        String technicalSpecifications, Double price, String imageURL, Integer categoryId) {
        this.productName = productName;
        this.briefDescription = briefDescription;
        this.fullDescription = fullDescription;
        this.technicalSpecifications = technicalSpecifications;
        this.price = price;
        this.imageURL = imageURL;
        this.categoryId = categoryId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getBriefDescription() {
        return briefDescription;
    }

    public void setBriefDescription(String briefDescription) {
        this.briefDescription = briefDescription;
    }

    public String getFullDescription() {
        return fullDescription;
    }

    public void setFullDescription(String fullDescription) {
        this.fullDescription = fullDescription;
    }

    public String getTechnicalSpecifications() {
        return technicalSpecifications;
    }

    public void setTechnicalSpecifications(String technicalSpecifications) {
        this.technicalSpecifications = technicalSpecifications;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }
}
