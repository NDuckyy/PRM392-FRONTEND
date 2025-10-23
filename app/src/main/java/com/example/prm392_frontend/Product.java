package com.example.prm392_frontend;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.prm392_frontend.models.CategoryResponse;

import java.util.ArrayList;
import java.util.List;

public class Product implements Parcelable {
    private int id;
    private String name;
    private String description;
    private double price;
    private String imageUrl;
    private List<String> imageUrls;
    private String category;
    private double rating;
    private String brand;
    private int popularity;
    private String specifications;

    // Additional fields for backend compatibility
    private String productName;
    private String briefDescription;
    private String fullDescription;
    private String technicalSpecifications;
    private CategoryResponse categoryID;
    private int providerId;
    private String providerName;

    public Product(int id, String name, String description, double price, String imageUrl,
                   List<String> imageUrls, String category, double rating, String brand,
                   int popularity, String specifications) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
        this.category = category;
        this.rating = rating;
        this.brand = brand;
        this.popularity = popularity;
        this.specifications = specifications;
    }

    protected Product(Parcel in) {
        id = in.readInt();
        name = in.readString();
        description = in.readString();
        price = in.readDouble();
        imageUrl = in.readString();
        imageUrls = in.createStringArrayList();
        category = in.readString();
        rating = in.readDouble();
        brand = in.readString();
        popularity = in.readInt();
        specifications = in.readString();
        providerId = in.readInt();
        providerName = in.readString();
    }

    public static final Creator<Product> CREATOR = new Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getCategory() {
        return category;
    }

    public double getRating() {
        return rating;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public String getBrand() {
        return brand;
    }

    public int getPopularity() {
        return popularity;
    }

    public String getSpecifications() {
        return specifications;
    }

    // Getters for backend-compatible fields
    public String getProductName() {
        return productName != null ? productName : name;
    }

    public String getBriefDescription() {
        return briefDescription;
    }

    public String getFullDescription() {
        return fullDescription;
    }

    public String getTechnicalSpecifications() {
        return technicalSpecifications != null ? technicalSpecifications : specifications;
    }

    public String getImageURL() {
        return imageUrl;
    }

    public CategoryResponse getCategoryID() {
        return categoryID;
    }

    // Setters for backend-compatible fields
    public void setProductName(String productName) {
        this.productName = productName;
        this.name = productName;
    }

    public void setBriefDescription(String briefDescription) {
        this.briefDescription = briefDescription;
    }

    public void setFullDescription(String fullDescription) {
        this.fullDescription = fullDescription;
        this.description = fullDescription;
    }

    public void setTechnicalSpecifications(String technicalSpecifications) {
        this.technicalSpecifications = technicalSpecifications;
        this.specifications = technicalSpecifications;
    }

    public void setCategoryID(CategoryResponse categoryID) {
        this.categoryID = categoryID;
        if (categoryID != null) {
            this.category = categoryID.getCategoryName();
        }
    }

    public int getProviderId() {
        return providerId;
    }

    public void setProviderId(int providerId) {
        this.providerId = providerId;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeDouble(price);
        dest.writeString(imageUrl);
        dest.writeStringList(imageUrls);
        dest.writeString(category);
        dest.writeDouble(rating);
        dest.writeString(brand);
        dest.writeInt(popularity);
        dest.writeString(specifications);
        dest.writeInt(providerId);
        dest.writeString(providerName);
    }
}
